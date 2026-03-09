package com.securemarts.domain.invoice.dto;

import com.securemarts.domain.invoice.entity.Invoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Invoice response")
public class InvoiceResponse {

    private String publicId;
    private String invoiceNumber;
    private String storeCustomerPublicId;
    private String customerName;
    private Invoice.InvoiceStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDate dueDate;
    private Instant issuedAt;
    private String notes;
    private List<InvoiceItemResponse> items;
    private Instant createdAt;

    public static InvoiceResponse from(Invoice inv, String customerName, String customerPublicId) {
        return InvoiceResponse.builder()
                .publicId(inv.getPublicId())
                .invoiceNumber(inv.getInvoiceNumber())
                .storeCustomerPublicId(customerPublicId)
                .customerName(customerName)
                .status(inv.getStatus())
                .totalAmount(inv.getTotalAmount())
                .currency(inv.getCurrency())
                .dueDate(inv.getDueDate())
                .issuedAt(inv.getIssuedAt())
                .notes(inv.getNotes())
                .items(inv.getItems() != null ? inv.getItems().stream().map(InvoiceItemResponse::from).collect(Collectors.toList()) : List.of())
                .createdAt(inv.getCreatedAt())
                .build();
    }
}
