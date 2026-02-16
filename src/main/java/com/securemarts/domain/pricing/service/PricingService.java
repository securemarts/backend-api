package com.securemarts.domain.pricing.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.pricing.dto.PriceRuleRequest;
import com.securemarts.domain.pricing.dto.PriceRuleResponse;
import com.securemarts.domain.pricing.entity.DiscountCode;
import com.securemarts.domain.pricing.entity.PriceRule;
import com.securemarts.domain.pricing.repository.DiscountCodeRepository;
import com.securemarts.domain.pricing.repository.PriceRuleRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.SubscriptionLimitsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PriceRuleRepository priceRuleRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final StoreRepository storeRepository;
    private final SubscriptionLimitsService subscriptionLimitsService;

    @Transactional(readOnly = true)
    public List<PriceRuleResponse> listPriceRules(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", String.valueOf(storeId));
        }
        return priceRuleRepository.findByStoreId(storeId).stream()
                .map(PriceRuleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PriceRuleResponse getPriceRule(Long storeId, String priceRulePublicId) {
        PriceRule rule = priceRuleRepository.findByPublicIdAndStoreId(priceRulePublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceRule", priceRulePublicId));
        return PriceRuleResponse.from(rule);
    }

    @Transactional
    public PriceRuleResponse createPriceRule(Long storeId, PriceRuleRequest request) {
        var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        var limits = subscriptionLimitsService.getLimitsForBusiness(store.getBusiness());
        if (priceRuleRepository.countByStoreId(storeId) >= limits.getMaxPriceRules()) {
            throw new BusinessRuleException("Price rule limit reached for your plan (" + limits.getMaxPriceRules() + "). Upgrade to add more.");
        }
        PriceRule rule = new PriceRule();
        rule.setStoreId(storeId);
        rule.setTitle(request.getTitle().trim());
        rule.setValueType(request.getValueType());
        rule.setValueAmount(request.getValueAmount());
        rule.setValuePercent(request.getValuePercent());
        rule.setStartsAt(request.getStartsAt());
        rule.setEndsAt(request.getEndsAt());
        rule.setUsageLimit(request.getUsageLimit());
        rule.setUsageCount(0);
        rule = priceRuleRepository.save(rule);
        return PriceRuleResponse.from(rule);
    }

    @Transactional
    public PriceRuleResponse updatePriceRule(Long storeId, String priceRulePublicId, PriceRuleRequest request) {
        PriceRule rule = priceRuleRepository.findByPublicIdAndStoreId(priceRulePublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceRule", priceRulePublicId));
        rule.setTitle(request.getTitle().trim());
        rule.setValueType(request.getValueType());
        rule.setValueAmount(request.getValueAmount());
        rule.setValuePercent(request.getValuePercent());
        rule.setStartsAt(request.getStartsAt());
        rule.setEndsAt(request.getEndsAt());
        rule.setUsageLimit(request.getUsageLimit());
        priceRuleRepository.save(rule);
        return PriceRuleResponse.from(rule);
    }

    @Transactional
    public DiscountCode addDiscountCode(Long storeId, String priceRulePublicId, com.securemarts.domain.pricing.dto.DiscountCodeRequest request) {
        var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        var limits = subscriptionLimitsService.getLimitsForBusiness(store.getBusiness());
        if (discountCodeRepository.countByStoreId(storeId) >= limits.getMaxDiscountCodes()) {
            throw new BusinessRuleException("Discount code limit reached for your plan (" + limits.getMaxDiscountCodes() + "). Upgrade to add more.");
        }
        PriceRule rule = priceRuleRepository.findByPublicIdAndStoreId(priceRulePublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceRule", priceRulePublicId));
        String code = request.getCode().trim().toUpperCase();
        if (discountCodeRepository.findByPriceRuleIdAndCode(rule.getId(), code).isPresent()) {
            throw new BusinessRuleException("Discount code already exists: " + code);
        }
        DiscountCode dc = new DiscountCode();
        dc.setPriceRule(rule);
        dc.setCode(code);
        dc.setUsageLimit(request.getUsageLimit());
        dc.setUsageCount(0);
        return discountCodeRepository.save(dc);
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal applyDiscount(String storePublicId, String code, java.math.BigDecimal subtotal) {
        Long storeId = storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElse(null);
        if (storeId == null || code == null || code.isBlank()) return subtotal;
        return discountCodeRepository.findByCodeAndStoreId(code.trim(), storeId)
                .filter(DiscountCode::isValid)
                .map(dc -> {
                    PriceRule rule = dc.getPriceRule();
                    if ("FIXED_AMOUNT".equals(rule.getValueType()) && rule.getValueAmount() != null) {
                        return subtotal.subtract(rule.getValueAmount()).max(java.math.BigDecimal.ZERO);
                    }
                    if ("PERCENTAGE".equals(rule.getValueType()) && rule.getValuePercent() != null) {
                        return subtotal.subtract(subtotal.multiply(rule.getValuePercent().divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP))).max(java.math.BigDecimal.ZERO);
                    }
                    return subtotal;
                })
                .orElse(subtotal);
    }
}
