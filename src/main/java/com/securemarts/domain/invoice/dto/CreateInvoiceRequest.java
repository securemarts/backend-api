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
    @Schema(description = "Store customer public ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeCustomerPublicId;

    @Schema(description = "Due date")
    private LocalDate dueDate;

    @Schema(description = "Notes")
    private String notes;

    @Valid
    @NotNull
    @Schema(description = "Line items", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<InvoiceItemRequest> items;
}
