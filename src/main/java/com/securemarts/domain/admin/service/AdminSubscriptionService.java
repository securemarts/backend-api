package com.securemarts.domain.admin.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.admin.dto.AdminSubscriptionUpdateRequest;
import com.securemarts.domain.admin.dto.SubscriptionDetailResponse;
import com.securemarts.domain.admin.dto.SubscriptionListResponse;
import com.securemarts.domain.onboarding.dto.PlanResponse;
import com.securemarts.domain.onboarding.entity.Business;
import com.securemarts.domain.onboarding.entity.Plan;
import com.securemarts.domain.onboarding.repository.BusinessRepository;
import com.securemarts.domain.onboarding.repository.PlanRepository;
import com.securemarts.domain.onboarding.repository.SubscriptionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {

    private final BusinessRepository businessRepository;
    private final PlanRepository planRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final AdminService adminService;

    @Transactional(readOnly = true)
    public PageResponse<SubscriptionListResponse> list(String plan, String status, String search, Pageable pageable) {
        Specification<Business> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (plan != null && !plan.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("subscriptionPlan"), Business.SubscriptionPlan.valueOf(plan.toUpperCase())));
                } catch (IllegalArgumentException ignored) {
                    // invalid plan filter ignored
                }
            }
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("subscriptionStatus"), Business.SubscriptionStatus.valueOf(status.toUpperCase())));
                } catch (IllegalArgumentException ignored) {
                    // invalid status filter ignored
                }
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("tradeName")), pattern),
                        cb.like(cb.lower(root.get("legalName")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<Business> page = businessRepository.findAll(spec, pageable);
        List<Long> businessIds = page.getContent().stream().map(Business::getId).toList();
        Map<Long, java.time.Instant> startDates = subscriptionHistoryRepository.findEarliestCreatedAtByBusinessIdIn(businessIds)
                .stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (java.time.Instant) row[1]));
        List<String> codes = page.getContent().stream().map(b -> b.getSubscriptionPlan().name()).distinct().toList();
        Map<String, Plan> planByCode = codes.isEmpty() ? Map.of() : planRepository.findByCodeIn(codes).stream().collect(Collectors.toMap(Plan::getCode, p -> p, (a, b) -> a));
        return PageResponse.of(page.map(b -> toListResponse(b, startDates.get(b.getId()), planByCode.get(b.getSubscriptionPlan().name()))));
    }

    @Transactional(readOnly = true)
    public SubscriptionDetailResponse getByBusinessPublicId(String businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        Plan plan = planRepository.findByCode(business.getSubscriptionPlan().name()).orElse(null);
        java.time.Instant startDate = subscriptionHistoryRepository.findEarliestCreatedAtByBusinessIdIn(List.of(business.getId()))
                .stream()
                .findFirst()
                .map(row -> (java.time.Instant) row[1])
                .orElse(null);
        return toDetailResponse(business, startDate, plan);
    }

    @Transactional
    public com.securemarts.domain.onboarding.dto.BusinessResponse updateSubscription(String businessPublicId, AdminSubscriptionUpdateRequest request) {
        return adminService.updateBusinessSubscription(businessPublicId, request);
    }

    private SubscriptionListResponse toListResponse(Business b, java.time.Instant startDate, Plan plan) {
        String merchantName = b.getTradeName() != null && !b.getTradeName().isBlank()
                ? b.getTradeName()
                : (b.getStores() != null && !b.getStores().isEmpty()
                ? b.getStores().get(0).getName()
                : b.getLegalName());
        String planName = plan != null ? plan.getName() : b.getSubscriptionPlan().name();
        String billingCycle = plan != null ? plan.getBillingCycle() : "MONTHLY";
        return SubscriptionListResponse.builder()
                .businessPublicId(b.getPublicId())
                .merchantName(merchantName)
                .plan(b.getSubscriptionPlan().name())
                .planName(planName)
                .billingCycle(billingCycle)
                .startDate(startDate)
                .renewalDate(b.getCurrentPeriodEndsAt())
                .status(b.getSubscriptionStatus().name())
                .build();
    }

    private SubscriptionDetailResponse toDetailResponse(Business b, java.time.Instant startDate, Plan plan) {
        String merchantName = b.getTradeName() != null && !b.getTradeName().isBlank()
                ? b.getTradeName()
                : (b.getStores() != null && !b.getStores().isEmpty()
                ? b.getStores().get(0).getName()
                : b.getLegalName());
        List<PlanResponse.PlanFeatureItem> features = plan != null && plan.getFeatures() != null
                ? plan.getFeatures().stream()
                .map(f -> new PlanResponse.PlanFeatureItem(f.getFeatureKey(), f.isEnabled(), f.getLimitValue()))
                .collect(Collectors.toList())
                : List.of();
        return SubscriptionDetailResponse.builder()
                .businessPublicId(b.getPublicId())
                .merchantName(merchantName)
                .plan(b.getSubscriptionPlan().name())
                .planName(plan != null ? plan.getName() : b.getSubscriptionPlan().name())
                .billingCycle(plan != null ? plan.getBillingCycle() : "MONTHLY")
                .price(plan != null ? plan.getPriceAmount() : null)
                .currency(plan != null ? plan.getCurrency() : "NGN")
                .status(b.getSubscriptionStatus().name())
                .startDate(startDate)
                .renewalDate(b.getCurrentPeriodEndsAt())
                .features(features)
                .build();
    }
}
