package com.shopper.domain.rider.controller;

import com.shopper.domain.rider.dto.RiderDocumentResponse;
import com.shopper.domain.rider.dto.RiderProfileResponse;
import com.shopper.domain.rider.service.RiderKycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/rider/kyc")
@RequiredArgsConstructor
@Tag(name = "Rider", description = "Rider auth (register, login, refresh, logout) and KYC (profile, documents)")
@SecurityRequirement(name = "bearerAuth")
public class RiderKycController {

    private final RiderKycService riderKycService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Current rider profile and verification status (PENDING, UNDER_REVIEW, APPROVED, REJECTED)")
    public ResponseEntity<RiderProfileResponse> getMyProfile(@AuthenticationPrincipal String riderPublicId) {
        return ResponseEntity.ok(riderKycService.getMyProfile(riderPublicId));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload KYC document", description = "Upload ID, proof of address, etc. documentType e.g. ID_CARD, PROOF_OF_ADDRESS. Moves verification to UNDER_REVIEW.")
    public ResponseEntity<RiderDocumentResponse> uploadDocument(
            @AuthenticationPrincipal String riderPublicId,
            @RequestParam String documentType,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(riderKycService.uploadDocument(riderPublicId, documentType, file));
    }

    @GetMapping("/documents")
    @Operation(summary = "List my KYC documents")
    public ResponseEntity<List<RiderDocumentResponse>> listMyDocuments(@AuthenticationPrincipal String riderPublicId) {
        return ResponseEntity.ok(riderKycService.listMyDocuments(riderPublicId));
    }
}
