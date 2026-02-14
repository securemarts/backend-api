package com.shopper.domain.pricing.dto;

import com.shopper.domain.pricing.entity.PriceRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Price rule response")
public class PriceRuleResponse {

    private String publicId;
    private String title;
    private String valueType;
    private BigDecimal valueAmount;
    private BigDecimal valuePercent;
    private Instant startsAt;
    private Instant endsAt;
    private Integer usageLimit;
    private int usageCount;
    private boolean active;
    private List<String> discountCodes;

    public static PriceRuleResponse from(PriceRule r) {
        return PriceRuleResponse.builder()
                .publicId(r.getPublicId())
                .title(r.getTitle())
                .valueType(r.getValueType())
                .valueAmount(r.getValueAmount())
                .valuePercent(r.getValuePercent())
                .startsAt(r.getStartsAt())
                .endsAt(r.getEndsAt())
                .usageLimit(r.getUsageLimit())
                .usageCount(r.getUsageCount())
                .active(r.isActive())
                .discountCodes(r.getDiscountCodes() != null
                        ? r.getDiscountCodes().stream().map(com.shopper.domain.pricing.entity.DiscountCode::getCode).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
