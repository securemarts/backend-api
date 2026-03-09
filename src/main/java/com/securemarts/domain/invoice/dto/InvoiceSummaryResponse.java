package com.securemarts.domain.invoice.dto;

import com.securemarts.domain.invoice.entity.Invoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Invoice summary for list view")
public class InvoiceSummaryResponse {

    private String publicId;
    private String invoiceNumber;
    private String customerName;
    private Invoice.InvoiceStatus status;
    private BigDecimal totalAmount;
    private LocalDate dueDate;
    private Instant issuedAt;
    private Instant createdAt;
}
