package com.securemarts.domain.invoice.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.invoice.dto.*;
import com.securemarts.domain.invoice.entity.Invoice;
import com.securemarts.domain.invoice.service.InvoiceService;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/stores/{storePublicId}/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Create, issue, list, and manage invoices for store customers")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(Store::getId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId));
    }

    @PostMapping
    @Operation(summary = "Create draft invoice")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InvoiceResponse> create(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Valid @RequestBody CreateInvoiceRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(invoiceService.create(storeId, request));
    }

    @GetMapping
    @Operation(summary = "List invoices", description = "Paginated, optional filters by status, customer, date range")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PageResponse<InvoiceSummaryResponse>> list(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(schema = @Schema(allowableValues = {"DRAFT", "ISSUED", "PARTIALLY_PAID", "PAID", "OVERDUE", "CANCELLED"})) @RequestParam(required = false) String status,
            @RequestParam(required = false) String storeCustomerPublicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:read");
        Long storeId = resolveStoreId(storePublicId);
        Invoice.InvoiceStatus statusEnum = status != null ? Invoice.InvoiceStatus.valueOf(status) : null;
        Long storeCustomerId = null;
        if (storeCustomerPublicId != null && !storeCustomerPublicId.isBlank()) {
            storeCustomerId = invoiceService.resolveCustomerId(storeId, storeCustomerPublicId);
        }
        Instant fromInst = from != null ? from.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant toInst = to != null ? to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        return ResponseEntity.ok(PageResponse.of(invoiceService.list(storeId, statusEnum, storeCustomerId, fromInst, toInst, pageable)));
    }

    @GetMapping("/{invoicePublicId}")
    @Operation(summary = "Get invoice")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InvoiceResponse> get(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Invoice public ID") @PathVariable String invoicePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(invoiceService.get(storeId, invoicePublicId));
    }

    @PatchMapping("/{invoicePublicId}")
    @Operation(summary = "Update draft invoice")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InvoiceResponse> update(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Invoice public ID") @PathVariable String invoicePublicId,
            @RequestBody UpdateInvoiceRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(invoiceService.update(storeId, invoicePublicId, request));
    }

    @PostMapping("/{invoicePublicId}/issue")
    @Operation(summary = "Issue invoice", description = "Transition draft to issued")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InvoiceResponse> issue(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Invoice public ID") @PathVariable String invoicePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(invoiceService.issue(storeId, invoicePublicId));
    }

    @PostMapping("/{invoicePublicId}/cancel")
    @Operation(summary = "Cancel invoice", description = "Only draft or issued with zero payments")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InvoiceResponse> cancel(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Invoice public ID") @PathVariable String invoicePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(invoiceService.cancel(storeId, invoicePublicId));
    }

    @PostMapping("/{invoicePublicId}/payments")
    @Operation(summary = "Record payment against invoice")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InvoicePaymentResponse> recordPayment(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Invoice public ID") @PathVariable String invoicePublicId,
            @Valid @RequestBody RecordPaymentRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(invoiceService.recordPayment(storeId, invoicePublicId, request));
    }

    @GetMapping("/{invoicePublicId}/payments")
    @Operation(summary = "List payments for an invoice")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<java.util.List<InvoicePaymentResponse>> listPayments(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Invoice public ID") @PathVariable String invoicePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(invoiceService.listPayments(storeId, invoicePublicId));
    }
}
