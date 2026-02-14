package com.shopper.domain.onboarding.controller;

import com.shopper.domain.catalog.service.FileStorageService;
import com.shopper.common.dto.ApiResponse;
import com.shopper.domain.onboarding.dto.*;
import com.shopper.domain.onboarding.entity.BankAccount;
import com.shopper.domain.onboarding.entity.ComplianceDocument;
import com.shopper.domain.onboarding.service.MerchantRoleService;
import com.shopper.domain.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
@Tag(name = "Business & Store Onboarding", description = "Create business, upload docs, create store, add bank account")
@SecurityRequirement(name = "bearerAuth")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final FileStorageService fileStorageService;
    private final MerchantRoleService merchantRoleService;

    @PostMapping("/businesses")
    @Operation(summary = "Create business", description = "Step 1: Create business after user verification")
    public ResponseEntity<BusinessResponse> createBusiness(
            @AuthenticationPrincipal String userPublicId,
            @Valid @RequestBody CreateBusinessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onboardingService.createBusiness(userPublicId, request));
    }

    @GetMapping("/businesses/{businessPublicId}")
    @Operation(summary = "Get business")
    public ResponseEntity<BusinessResponse> getBusiness(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String businessPublicId) {
        return ResponseEntity.ok(onboardingService.getBusiness(userPublicId, businessPublicId));
    }

    @PostMapping("/businesses/{businessPublicId}/stores")
    @Operation(summary = "Create store", description = "Create store under business")
    public ResponseEntity<StoreResponse> createStore(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String businessPublicId,
            @Valid @RequestBody CreateStoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onboardingService.createStore(userPublicId, businessPublicId, request));
    }

    @PostMapping(value = "/businesses/{businessPublicId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload compliance document", description = "Upload CAC, TIN, ID etc. as multipart form: documentType (required), file (required).")
    public ResponseEntity<ComplianceDocumentDto> uploadDocument(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Business public ID") @PathVariable String businessPublicId,
            @Parameter(description = "Document type", example = "CAC_CERTIFICATE", required = true) @RequestParam String documentType,
            @Parameter(description = "Document file (PDF, image, etc.)", required = true) @RequestPart("file") MultipartFile file) throws java.io.IOException {
        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException("documentType is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        String fileUrl = fileStorageService.storeBusinessDocument(businessPublicId, file);
        if (fileUrl == null) {
            throw new IllegalArgumentException("Failed to store file");
        }
        UploadComplianceDocumentRequest request = new UploadComplianceDocumentRequest();
        request.setDocumentType(documentType);
        request.setFileUrl(fileUrl);
        request.setFileName(file.getOriginalFilename());
        request.setMimeType(file.getContentType());
        ComplianceDocument doc = onboardingService.uploadComplianceDocument(
                userPublicId, businessPublicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ComplianceDocumentDto.from(doc));
    }

    @PostMapping("/businesses/{businessPublicId}/submit")
    @Operation(summary = "Submit for verification", description = "Submit business for admin approval")
    public ResponseEntity<?> submitForVerification(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String businessPublicId) {
        onboardingService.submitForVerification(userPublicId, businessPublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/stores/{storePublicId}/bank-accounts")
    @Operation(summary = "Add bank account", description = "Add payout bank account for store")
    public ResponseEntity<BankAccountDto> addBankAccount(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Valid @RequestBody AddBankAccountRequest request) {
        BankAccount account = onboardingService.addBankAccount(userPublicId, storePublicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BankAccountDto.from(account));
    }

    @GetMapping("/stores/{storePublicId}")
    @Operation(summary = "Get store")
    public ResponseEntity<StoreResponse> getStore(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId) {
        return ResponseEntity.ok(onboardingService.getStore(userPublicId, storePublicId));
    }

    @PostMapping("/stores/{storePublicId}/activate")
    @Operation(summary = "Activate store", description = "Activate store after business is approved")
    public ResponseEntity<?> activateStore(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId) {
        onboardingService.activateStore(userPublicId, storePublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me/stores")
    @Operation(summary = "List my stores", description = "List stores for current user (owner or staff)")
    public ResponseEntity<List<StoreResponse>> listMyStores(@AuthenticationPrincipal String userPublicId) {
        return ResponseEntity.ok(onboardingService.listStoresForUser(userPublicId));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('MERCHANT_OWNER')")
    @Operation(summary = "List merchant roles", description = "Available roles for assigning to staff (MANAGER, CASHIER, STAFF). Owner only.")
    public ResponseEntity<List<MerchantRoleResponse>> listRoles(@AuthenticationPrincipal String userPublicId) {
        return ResponseEntity.ok(merchantRoleService.listRoles());
    }

    @lombok.Data
    @lombok.Builder
    public static class ComplianceDocumentDto {
        private String publicId;
        private String documentType;
        private String status;
        public static ComplianceDocumentDto from(ComplianceDocument d) {
            return ComplianceDocumentDto.builder()
                    .publicId(d.getPublicId())
                    .documentType(d.getDocumentType())
                    .status(d.getStatus().name())
                    .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class BankAccountDto {
        private String publicId;
        private String bankName;
        private String accountNumber;
        private String accountName;
        private boolean payoutDefault;
        public static BankAccountDto from(BankAccount a) {
            return BankAccountDto.builder()
                    .publicId(a.getPublicId())
                    .bankName(a.getBankName())
                    .accountNumber(a.getAccountNumber())
                    .accountName(a.getAccountName())
                    .payoutDefault(a.isPayoutDefault())
                    .build();
        }
    }
}
