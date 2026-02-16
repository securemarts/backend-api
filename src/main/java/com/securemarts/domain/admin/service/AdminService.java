package com.securemarts.domain.admin.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.admin.dto.*;
import com.securemarts.domain.admin.entity.Admin;
import com.securemarts.domain.admin.entity.AdminInvite;
import com.securemarts.domain.admin.entity.PlatformRole;
import com.securemarts.domain.admin.repository.AdminInviteRepository;
import com.securemarts.domain.admin.repository.AdminRepository;
import com.securemarts.domain.admin.repository.PlatformRoleRepository;
import com.securemarts.domain.onboarding.dto.BusinessResponse;
import com.securemarts.domain.onboarding.entity.Business;
import com.securemarts.domain.onboarding.entity.SubscriptionHistory;
import com.securemarts.domain.onboarding.repository.BusinessRepository;
import com.securemarts.domain.onboarding.repository.SubscriptionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int INVITE_EXPIRY_DAYS = 7;

    private final AdminRepository adminRepository;
    private final AdminInviteRepository adminInviteRepository;
    private final PlatformRoleRepository platformRoleRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<BusinessResponse> listBusinesses(String status, Pageable pageable) {
        Page<Business> page = status != null && !status.isBlank()
                ? businessRepository.findByVerificationStatus(Business.VerificationStatus.valueOf(status.toUpperCase()), pageable)
                : businessRepository.findAll(pageable);
        return PageResponse.of(page.map(BusinessResponse::from));
    }

    @Transactional
    public BusinessResponse updateBusinessVerification(String businessPublicId, String adminPublicId, BusinessVerificationUpdateRequest request) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        Admin admin = adminRepository.findByPublicId(adminPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminPublicId));
        Business.VerificationStatus newStatus;
        try {
            newStatus = Business.VerificationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid status. Use APPROVED or REJECTED");
        }
        if (newStatus != Business.VerificationStatus.APPROVED && newStatus != Business.VerificationStatus.REJECTED) {
            throw new BusinessRuleException("Status must be APPROVED or REJECTED");
        }
        if (newStatus == Business.VerificationStatus.REJECTED && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new BusinessRuleException("Rejection reason is required when rejecting");
        }
        business.setVerificationStatus(newStatus);
        businessRepository.save(business);
        return BusinessResponse.from(business);
    }

    @Transactional
    public BusinessResponse updateBusinessSubscription(String businessPublicId, AdminSubscriptionUpdateRequest request) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        if (request.getPlan() != null && !request.getPlan().isBlank()) {
            try {
                business.setSubscriptionPlan(Business.SubscriptionPlan.valueOf(request.getPlan().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Invalid plan. Use BASIC, PRO, or ENTERPRISE");
            }
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                business.setSubscriptionStatus(Business.SubscriptionStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Invalid status. Use NONE, TRIALING, ACTIVE, PAST_DUE, or CANCELLED");
            }
        }
        if (request.getTrialEndsAt() != null) {
            business.setTrialEndsAt(request.getTrialEndsAt());
        }
        if (request.getCurrentPeriodEndsAt() != null) {
            business.setCurrentPeriodEndsAt(request.getCurrentPeriodEndsAt());
        }
        business = businessRepository.save(business);
        SubscriptionHistory h = new SubscriptionHistory();
        h.setBusinessId(business.getId());
        h.setPlan(business.getSubscriptionPlan().name());
        h.setEventType(SubscriptionHistory.EventType.UPDATED.name());
        h.setPeriodEnd(business.getCurrentPeriodEndsAt());
        h.setSource(SubscriptionHistory.Source.ADMIN.name());
        subscriptionHistoryRepository.save(h);
        return BusinessResponse.from(business);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminResponse> listAdmins(Pageable pageable) {
        Page<Admin> page = adminRepository.findAll(pageable);
        return PageResponse.of(page.map(AdminResponse::from));
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdmin(String adminPublicId) {
        Admin admin = adminRepository.findByPublicId(adminPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminPublicId));
        return AdminResponse.from(admin);
    }

    @Transactional
    public AdminResponse updateAdmin(String requesterAdminPublicId, String adminPublicId, UpdateAdminRequest request) {
        Admin requester = adminRepository.findByPublicId(requesterAdminPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", requesterAdminPublicId));
        Admin admin = adminRepository.findByPublicId(adminPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminPublicId));
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            admin.setFullName(request.getFullName().trim());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<PlatformRole> roles = resolvePlatformRoles(request.getRoles());
            admin.setPlatformRoles(roles);
        }
        if (request.getActive() != null) {
            admin.setActive(request.getActive());
        }
        admin = adminRepository.save(admin);
        return AdminResponse.from(admin);
    }

    @Transactional
    public void deleteAdmin(String requesterAdminPublicId, String adminPublicId) {
        Admin requester = adminRepository.findByPublicId(requesterAdminPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", requesterAdminPublicId));
        if (!requester.hasRole("SUPERUSER")) {
            throw new BusinessRuleException("Only superuser can delete admins");
        }
        if (requester.getPublicId().equals(adminPublicId)) {
            throw new BusinessRuleException("Cannot delete your own admin account");
        }
        Admin admin = adminRepository.findByPublicId(adminPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminPublicId));
        adminRepository.delete(admin);
    }

    @Transactional
    public AdminResponse createAdmin(String requesterAdminPublicId, CreateAdminRequest request) {
        Admin requester = adminRepository.findByPublicId(requesterAdminPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", requesterAdminPublicId));
        if (!requester.hasRole("SUPERUSER")) {
            throw new BusinessRuleException("Only superuser can create admins");
        }
        if (adminRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BusinessRuleException("Admin with this email already exists");
        }
        List<String> roleCodes = request.getRoles() != null && !request.getRoles().isEmpty()
                ? request.getRoles()
                : (request.getRole() != null && !request.getRole().isBlank()
                ? List.of(request.getRole().toUpperCase())
                : List.of(Admin.AdminRole.PLATFORM_ADMIN.name()));
        Set<PlatformRole> platformRoles = resolvePlatformRoles(roleCodes);
        Admin admin = new Admin();
        admin.setEmail(request.getEmail().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setFullName(request.getFullName().trim());
        admin.setPlatformRoles(platformRoles);
        admin.setActive(true);
        admin = adminRepository.save(admin);
        return AdminResponse.from(admin);
    }

    @Transactional
    public AdminInviteResponse inviteAdmin(String requesterAdminPublicId, InviteAdminRequest request) {
        Admin requester = adminRepository.findByPublicId(requesterAdminPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", requesterAdminPublicId));
        if (!requester.hasRole("SUPERUSER")) {
            throw new BusinessRuleException("Only superuser can invite admins");
        }
        String email = request.getEmail().toLowerCase().trim();
        if (adminRepository.existsByEmail(email)) {
            throw new BusinessRuleException("An admin with this email already exists");
        }
        if (adminInviteRepository.existsByEmailAndUsedAtIsNull(email)) {
            throw new BusinessRuleException("An invite for this email is already pending");
        }
        Admin.AdminRole role;
        try {
            role = Admin.AdminRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid role. Use SUPERUSER, PLATFORM_ADMIN, or SUPPORT");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        AdminInvite invite = new AdminInvite();
        invite.setEmail(email);
        invite.setFullName(request.getFullName().trim());
        invite.setRole(role);
        invite.setInviteToken(token);
        invite.setInvitedByAdminId(requester.getId());
        invite.setExpiresAt(Instant.now().plusSeconds(INVITE_EXPIRY_DAYS * 86400L));
        invite = adminInviteRepository.save(invite);
        return AdminInviteResponse.builder()
                .publicId(invite.getPublicId())
                .email(invite.getEmail())
                .inviteToken(invite.getInviteToken())
                .expiresAt(invite.getExpiresAt())
                .createdAt(invite.getCreatedAt())
                .completeSetupPath("/admin/auth/complete-setup")
                .build();
    }

    @Transactional
    public AdminResponse completeAdminSetup(CompleteAdminSetupRequest request) {
        AdminInvite invite = adminInviteRepository.findByInviteTokenAndEmail(request.getInviteToken(), request.getEmail().toLowerCase())
                .orElseThrow(() -> new BusinessRuleException("Invalid or expired invite token"));
        if (invite.getUsedAt() != null) {
            throw new BusinessRuleException("This invite has already been used");
        }
        if (Instant.now().isAfter(invite.getExpiresAt())) {
            throw new BusinessRuleException("This invite has expired");
        }
        if (adminRepository.existsByEmail(invite.getEmail())) {
            throw new BusinessRuleException("An admin with this email already exists");
        }
        PlatformRole inviteRole = platformRoleRepository.findByCode(invite.getRole().name())
                .orElseThrow(() -> new BusinessRuleException("Role " + invite.getRole().name() + " not found"));
        Admin admin = new Admin();
        admin.setEmail(invite.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setFullName(invite.getFullName());
        admin.setPlatformRoles(new HashSet<>(Set.of(inviteRole)));
        admin.setActive(true);
        admin = adminRepository.save(admin);
        invite.setUsedAt(Instant.now());
        adminInviteRepository.save(invite);
        return AdminResponse.from(admin);
    }

    private Set<PlatformRole> resolvePlatformRoles(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Set.of();
        }
        Set<String> codes = roleCodes.stream().map(String::toUpperCase).collect(Collectors.toSet());
        List<PlatformRole> found = platformRoleRepository.findByCodeIn(codes);
        if (found.size() != codes.size()) {
            var foundCodes = found.stream().map(PlatformRole::getCode).collect(Collectors.toSet());
            var missing = codes.stream().filter(c -> !foundCodes.contains(c)).findFirst();
            throw new BusinessRuleException("Invalid or unknown role: " + missing.orElse(""));
        }
        return new HashSet<>(found);
    }
}
