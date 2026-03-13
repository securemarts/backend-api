package com.securemarts.domain.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Request to create a draft invoice")
public class CreateInvoiceRequest {

    @NotBlank
    @Schema(description = "Store customer public ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String storeCustomerPublicId;

    @Schema(description = "Due date", example = "2026-04-15")
    private LocalDate dueDate;

    @Schema(description = "Notes", example = "Payment due within 30 days")
    private String notes;

    @Valid
    @NotNull
    @Schema(description = "Line items", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<InvoiceItemRequest> items;
}
