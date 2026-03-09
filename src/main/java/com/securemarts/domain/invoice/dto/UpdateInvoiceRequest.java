package com.securemarts.domain.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Request to update a draft invoice (items, due date, notes)")
public class UpdateInvoiceRequest {

    @Schema(description = "Due date")
    private LocalDate dueDate;

    @Schema(description = "Notes")
    private String notes;

    @Valid
    @Schema(description = "Line items (replaces existing)")
    private List<InvoiceItemRequest> items;
}
