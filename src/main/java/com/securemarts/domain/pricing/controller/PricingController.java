package com.securemarts.domain.pricing.controller;

import com.securemarts.domain.pricing.dto.DiscountCodeRequest;
import com.securemarts.domain.pricing.dto.PriceRuleRequest;
import com.securemarts.domain.pricing.dto.PriceRuleResponse;
import com.securemarts.domain.pricing.entity.DiscountCode;
import com.securemarts.domain.pricing.service.PricingService;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stores/{storePublicId}/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing & Promotions", description = "Price rules, discount codes, scheduled promos")
@SecurityRequirement(name = "bearerAuth")
public class PricingController {

    private final PricingService pricingService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    @GetMapping("/rules")
    @Operation(summary = "List price rules")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<List<PriceRuleResponse>> listRules(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "pricing:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(pricingService.listPriceRules(storeId));
    }

    @GetMapping("/rules/{priceRulePublicId}")
    @Operation(summary = "Get price rule")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PriceRuleResponse> getRule(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String priceRulePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "pricing:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(pricingService.getPriceRule(storeId, priceRulePublicId));
    }

    @PostMapping("/rules")
    @Operation(summary = "Create price rule")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PriceRuleResponse> createRule(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Valid @RequestBody PriceRuleRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "pricing:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingService.createPriceRule(storeId, request));
    }

    @PutMapping("/rules/{priceRulePublicId}")
    @Operation(summary = "Update price rule")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PriceRuleResponse> updateRule(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String priceRulePublicId,
            @Valid @RequestBody PriceRuleRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "pricing:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(pricingService.updatePriceRule(storeId, priceRulePublicId, request));
    }

    @PostMapping("/rules/{priceRulePublicId}/codes")
    @Operation(summary = "Add discount code to rule")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<DiscountCodeDto> addDiscountCode(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String priceRulePublicId,
            @Valid @RequestBody DiscountCodeRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "pricing:write");
        Long storeId = resolveStoreId(storePublicId);
        DiscountCode dc = pricingService.addDiscountCode(storeId, priceRulePublicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DiscountCodeDto.from(dc));
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply discount code (returns discounted amount)", description = "Used at checkout to get discounted total")
    public ResponseEntity<Map<String, Object>> applyCode(
            @PathVariable String storePublicId,
            @RequestBody Map<String, String> body) {
        String code = body != null ? body.get("code") : null;
        java.math.BigDecimal subtotal = body != null && body.containsKey("subtotal")
                ? new java.math.BigDecimal(body.get("subtotal"))
                : java.math.BigDecimal.ZERO;
        java.math.BigDecimal discounted = pricingService.applyDiscount(storePublicId, code, subtotal);
        return ResponseEntity.ok(Map.of("subtotal", subtotal, "discountedTotal", discounted, "code", code != null ? code : ""));
    }

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId));
    }

    @lombok.Data
    @lombok.Builder
    public static class DiscountCodeDto {
        private String publicId;
        private String code;
        private Integer usageLimit;
        private int usageCount;

        public static DiscountCodeDto from(DiscountCode dc) {
            return DiscountCodeDto.builder()
                    .publicId(dc.getPublicId())
                    .code(dc.getCode())
                    .usageLimit(dc.getUsageLimit())
                    .usageCount(dc.getUsageCount())
                    .build();
        }
    }
}
