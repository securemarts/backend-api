package com.securemarts.domain.onboarding.controller;

import com.securemarts.common.dto.ApiResponse;
import com.securemarts.domain.onboarding.dto.*;
import com.securemarts.domain.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/onboarding/businesses/{businessPublicId}/members")
@RequiredArgsConstructor
@Tag(name = "Business - Member Management", description = "Invite, add, list, update roles/status, and remove staff. Business owner only.")
@SecurityRequirement(name = "bearerAuth")
public class BusinessMemberController {

    private final OnboardingService onboardingService;

    @GetMapping
    @PreAuthorize("hasRole('MERCHANT_OWNER')")
    @Operation(summary = "List members", description = "List all staff (invited, active, deactivated) for this business.")
    public ResponseEntity<List<BusinessMemberResponse>> listMembers(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Business public ID") @PathVariable String businessPublicId) {
        return ResponseEntity.ok(onboardingService.listMembers(userPublicId, businessPublicId));
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('MERCHANT_OWNER')")
    @Operation(summary = "Invite by email", description = "Invite a person by email; they join when they register or accept. Single role.")
    public ResponseEntity<BusinessMemberResponse> inviteMember(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Business public ID") @PathVariable String businessPublicId,
            @Valid @RequestBody InviteMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onboardingService.inviteMember(userPublicId, businessPublicId, request));
    }

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT_OWNER')")
    @Operation(summary = "Add existing user", description = "Add an existing platform user (by public ID) as active staff with one role.")
    public ResponseEntity<BusinessMemberResponse> addMember(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Business public ID") @PathVariable String businessPublicId,
            @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onboardingService.addMember(userPublicId, businessPublicId, request));
    }

    @PatchMapping("/{memberPublicId}")
    @PreAuthorize("hasRole('MERCHANT_OWNER')")
    @Operation(summary = "Update member", description = "Update roles and/or status. Set status to DEACTIVATED to revoke access (member stays in list); ACTIVE to restore.")
    public ResponseEntity<BusinessMemberResponse> updateMember(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Business public ID") @PathVariable String businessPublicId,
            @Parameter(description = "Member public ID") @PathVariable String memberPublicId,
            @Valid @RequestBody UpdateMemberRequest request) {
        return ResponseEntity.ok(onboardingService.updateMember(userPublicId, businessPublicId, memberPublicId, request));
    }

    @DeleteMapping("/{memberPublicId}")
    @PreAuthorize("hasRole('MERCHANT_OWNER')")
    @Operation(summary = "Remove member", description = "Permanently remove the member from the business. For temporary revoke, use PATCH with status DEACTIVATED.")
    public ResponseEntity<?> removeMember(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Business public ID") @PathVariable String businessPublicId,
            @Parameter(description = "Member public ID") @PathVariable String memberPublicId) {
        onboardingService.removeMember(userPublicId, businessPublicId, memberPublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
