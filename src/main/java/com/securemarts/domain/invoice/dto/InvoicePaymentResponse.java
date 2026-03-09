package com.securemarts.domain.invoice.dto;

import com.securemarts.domain.invoice.entity.InvoicePayment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Invoice payment response")
public class InvoicePaymentResponse {

    private String publicId;
    private BigDecimal amount;
    private String currency;
    private InvoicePayment.PaymentMethod paymentMethod;
    private String reference;
    private Instant paidAt;
    private Instant createdAt;

    public static InvoicePaymentResponse from(InvoicePayment p) {
        return InvoicePaymentResponse.builder()
                .publicId(p.getPublicId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .paymentMethod(p.getPaymentMethod())
                .reference(p.getReference())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
