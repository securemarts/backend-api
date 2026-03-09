package com.securemarts.domain.invoice.dto;

import com.securemarts.domain.invoice.entity.InvoiceItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Invoice line item response")
public class InvoiceItemResponse {

    private Long id;
    private String variantPublicId;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public static InvoiceItemResponse from(InvoiceItem item) {
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .variantPublicId(item.getProductVariant() != null ? item.getProductVariant().getPublicId() : null)
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
