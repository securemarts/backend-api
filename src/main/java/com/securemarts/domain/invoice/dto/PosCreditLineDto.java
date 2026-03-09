package com.securemarts.domain.invoice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PosCreditLineDto {
    private String variantPublicId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
