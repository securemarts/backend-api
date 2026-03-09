package com.securemarts.domain.admin.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.onboarding.dto.CreatePlanRequest;
import com.securemarts.domain.onboarding.dto.PlanResponse;
import com.securemarts.domain.onboarding.dto.UpdatePlanRequest;
import com.securemarts.domain.onboarding.entity.Business;
import com.securemarts.domain.onboarding.entity.Plan;
import com.securemarts.domain.onboarding.entity.PlanFeature;
import com.securemarts.domain.onboarding.entity.SubscriptionPlanLimit;
import com.securemarts.domain.onboarding.repository.BusinessRepository;
import com.securemarts.domain.onboarding.repository.PlanRepository;
import com.securemarts.domain.onboarding.repository.SubscriptionPlanLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPlanService {

    private final PlanRepository planRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionPlanLimitRepository subscriptionPlanLimitRepository;

    @Transactional(readOnly = true)
    public PageResponse<PlanResponse> list(String search, String status, Pageable pageable) {
        Specification<Plan> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("code")), pattern)
                ));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<Plan> page = planRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(p -> {
            long count = countActiveSubscribers(p.getCode());
            return PlanResponse.from(p, count, null);
        }));
    }

    @Transactional(readOnly = true)
    public PlanResponse getByPublicId(String planPublicId) {
        Plan plan = planRepository.findByPublicId(planPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planPublicId));
        long count = countActiveSubscribers(plan.getCode());
        List<PlanResponse.PlanFeatureItem> features = plan.getFeatures() != null
                ? plan.getFeatures().stream()
                .map(f -> new PlanResponse.PlanFeatureItem(f.getFeatureKey(), f.isEnabled(), f.getLimitValue()))
                .collect(Collectors.toList())
                : List.of();
        return PlanResponse.from(plan, count, features);
    }

    @Transactional
    public PlanResponse create(CreatePlanRequest request) {
        if (planRepository.existsByCode(request.getCode())) {
            throw new BusinessRuleException("Plan code already exists: " + request.getCode());
        }
        Plan plan = new Plan();
        plan.setName(request.getName());
        plan.setCode(request.getCode().toUpperCase());
        plan.setDescription(request.getDescription());
        plan.setBillingCycle(request.getBillingCycle() != null ? request.getBillingCycle() : "MONTHLY");
        plan.setCurrency(request.getCurrency() != null ? request.getCurrency() : "NGN");
        plan.setPriceAmount(request.getPriceAmount() != null ? request.getPriceAmount() : java.math.BigDecimal.ZERO);
        plan.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        plan = planRepository.save(plan);
        if (request.getFeatures() != null) {
            for (CreatePlanRequest.PlanFeatureInput f : request.getFeatures()) {
                PlanFeature pf = new PlanFeature();
                pf.setPlan(plan);
                pf.setFeatureKey(f.getFeatureKey());
                pf.setEnabled(f.isEnabled());
                pf.setLimitValue(f.getLimitValue());
                plan.getFeatures().add(pf);
            }
            planRepository.save(plan);
        }
        ensureSubscriptionPlanLimitsRow(plan.getCode());
        return getByPublicId(plan.getPublicId());
    }

    @Transactional
    public PlanResponse update(String planPublicId, UpdatePlanRequest request) {
        Plan plan = planRepository.findByPublicId(planPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planPublicId));
        if (request.getName() != null) plan.setName(request.getName());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getBillingCycle() != null) plan.setBillingCycle(request.getBillingCycle());
        if (request.getCurrency() != null) plan.setCurrency(request.getCurrency());
        if (request.getPriceAmount() != null) plan.setPriceAmount(request.getPriceAmount());
        if (request.getStatus() != null) plan.setStatus(request.getStatus());
        if (request.getFeatures() != null) {
            plan.getFeatures().clear();
            for (CreatePlanRequest.PlanFeatureInput f : request.getFeatures()) {
                PlanFeature pf = new PlanFeature();
                pf.setPlan(plan);
                pf.setFeatureKey(f.getFeatureKey());
                pf.setEnabled(f.isEnabled());
                pf.setLimitValue(f.getLimitValue());
                plan.getFeatures().add(pf);
            }
        }
        planRepository.save(plan);
        return getByPublicId(plan.getPublicId());
    }

    @Transactional
    public void delete(String planPublicId) {
        Plan plan = planRepository.findByPublicId(planPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planPublicId));
        long count = countActiveSubscribers(plan.getCode());
        if (count > 0) {
            throw new BusinessRuleException("Cannot delete plan with active subscribers. Move or cancel them first.");
        }
        planRepository.delete(plan);
    }

    private long countActiveSubscribers(String planCode) {
        try {
            Business.SubscriptionPlan enumVal = Business.SubscriptionPlan.valueOf(planCode);
            return businessRepository.countBySubscriptionPlan(enumVal);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    private void ensureSubscriptionPlanLimitsRow(String code) {
        if (subscriptionPlanLimitRepository.findByPlan(code).isEmpty()) {
            SubscriptionPlanLimit limit = new SubscriptionPlanLimit();
            limit.setPlan(code);
            limit.setMaxStores(1);
            limit.setMaxLocationsPerStore(1);
            limit.setMaxProducts(50);
            limit.setMaxStaff(1);
            limit.setMaxPriceRules(1);
            limit.setMaxDiscountCodes(1);
            limit.setMaxPosRegisters(0);
            limit.setDeliveryEnabled(false);
            subscriptionPlanLimitRepository.save(limit);
        }
    }
}
