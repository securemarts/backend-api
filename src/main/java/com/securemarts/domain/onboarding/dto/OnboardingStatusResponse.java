package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Current onboarding progress for the authenticated merchant")
public class OnboardingStatusResponse {

    @Schema(description = "Authenticated user profile")
    private UserSummary user;

    @Schema(description = "Business details (null if no business created yet)")
    private BusinessSummary business;

    @Schema(description = "Stores under the business (empty if none)")
    private List<StoreSummary> stores;

    @Schema(description = "Completion status of each onboarding step")
    private Steps steps;

    @Schema(description = "The next step the merchant should complete, or null if all done",
            example = "DOCUMENTS_UPLOADED",
            allowableValues = {"BUSINESS_CREATED", "STORE_CREATED", "DOCUMENTS_UPLOADED",
                    "BANK_ACCOUNT_ADDED", "SUBMITTED_FOR_VERIFICATION", "STORE_ACTIVATED"})
    private String currentStep;

    @Schema(description = "True when all merchant-actionable steps are complete "
            + "(business, store, documents, bank account, submitted for verification). "
            + "Store activation depends on admin approval and does not gate this flag.",
            example = "false")
    private boolean completed;

    @Data
    @Builder
    @Schema(description = "Basic user profile")
    public static class UserSummary {
        @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        private String publicId;
        @Schema(example = "merchant@example.com")
        private String email;
        @Schema(example = "John")
        private String firstName;
        @Schema(example = "Doe")
        private String lastName;
    }

    @Data
    @Builder
    @Schema(description = "Business summary for onboarding status")
    public static class BusinessSummary {
        @Schema(example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
        private String publicId;
        @Schema(example = "Acme Ventures Ltd")
        private String legalName;
        @Schema(description = "PENDING, UNDER_REVIEW, APPROVED, or REJECTED", example = "PENDING")
        private String verificationStatus;
        @Schema(description = "BASIC, PRO, or ENTERPRISE", example = "BASIC")
        private String subscriptionPlan;
    }

    @Data
    @Builder
    @Schema(description = "Store summary with bank account indicator")
    public static class StoreSummary {
        @Schema(example = "c3d4e5f6-a7b8-9012-cdef-123456789012")
        private String publicId;
        @Schema(example = "Acme Main Store")
        private String name;
        @Schema(example = "acme-main")
        private String domainSlug;
        @Schema(example = "false")
        private boolean active;
        @Schema(description = "Whether this store has at least one bank account configured", example = "true")
        private boolean hasBankAccount;
    }

    @Data
    @Builder
    @Schema(description = "Individual onboarding step completion flags")
    public static class Steps {
        @Schema(description = "Business has been created", example = "true")
        private boolean businessCreated;
        @Schema(description = "At least one store exists", example = "true")
        private boolean storeCreated;
        @Schema(description = "At least one compliance document uploaded", example = "false")
        private boolean documentsUploaded;
        @Schema(description = "At least one bank account added on any store", example = "true")
        private boolean bankAccountAdded;
        @Schema(description = "Business submitted for verification (status is not PENDING)", example = "false")
        private boolean submittedForVerification;
        @Schema(description = "At least one store is active (requires admin approval first)", example = "false")
        private boolean storeActivated;
    }
}
