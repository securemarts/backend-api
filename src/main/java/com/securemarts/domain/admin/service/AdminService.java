package com.securemarts.domain.admin.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.admin.dto.*;
import com.securemarts.domain.admin.entity.Admin;
import com.securemarts.domain.auth.entity.User;
import com.securemarts.domain.auth.repository.UserRepository;
import com.securemarts.domain.admin.entity.AdminInvite;
import com.securemarts.domain.admin.entity.PlatformRole;
import com.securemarts.domain.admin.repository.AdminInviteRepository;
import com.securemarts.domain.admin.repository.AdminRepository;
import com.securemarts.domain.admin.repository.PlatformRoleRepository;
import com.securemarts.domain.onboarding.dto.BusinessResponse;
import com.securemarts.domain.onboarding.entity.Business;
import com.securemarts.domain.onboarding.entity.BusinessMember;
import com.securemarts.domain.onboarding.entity.BusinessOwner;
import com.securemarts.domain.onboarding.entity.SubscriptionHistory;
import com.securemarts.domain.onboarding.repository.BusinessOwnerRepository;
import com.securemarts.domain.onboarding.repository.BusinessMemberRepository;
import com.securemarts.domain.onboarding.repository.BusinessRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.repository.SubscriptionHistoryRepository;
import com.securemarts.domain.order.dto.OrderResponse;
import com.securemarts.domain.order.entity.Order;
import com.securemarts.domain.order.repository.OrderRepository;
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
    private final StoreRepository storeRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminBusinessDetailResponse getBusinessByPublicId(String businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        List<com.securemarts.domain.onboarding.entity.Store> stores = storeRepository.findByBusinessId(business.getId());
        int ownerCount = businessOwnerRepository.findByBusinessId(business.getId()).size();
        long memberCount = businessMemberRepository.countByBusinessId(business.getId());
        List<Long> storeIds = stores.stream().map(com.securemarts.domain.onboarding.entity.Store::getId).toList();
        long orderCount = storeIds.isEmpty() ? 0L : orderRepository.countByStoreIdIn(storeIds);

        List<BusinessResponse.StoreSummary> storeSummaries = stores.stream()
                .map(BusinessResponse.StoreSummary::from)
                .collect(Collectors.toList());

        return AdminBusinessDetailResponse.builder()
                .publicId(business.getPublicId())
                .legalName(business.getLegalName())
                .tradeName(business.getTradeName())
                .cacNumber(business.getCacNumber())
                .logoUrl(business.getLogoUrl())
                .businessTypePublicId(business.getBusinessType() != null ? business.getBusinessType().getPublicId() : null)
                .verificationStatus(business.getVerificationStatus() != null ? business.getVerificationStatus().name() : "PENDING")
                .createdAt(business.getCreatedAt())
                .subscriptionPlan(business.getSubscriptionPlan() != null ? business.getSubscriptionPlan().name() : null)
                .subscriptionStatus(business.getSubscriptionStatus() != null ? business.getSubscriptionStatus().name() : null)
                .trialEndsAt(business.getTrialEndsAt())
                .currentPeriodEndsAt(business.getCurrentPeriodEndsAt())
                .storeCount(stores.size())
                .ownerCount(ownerCount)
                .memberCount((int) memberCount)
                .orderCount(orderCount)
                .stores(storeSummaries)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminBusinessUserSummary> listBusinessUsers(String businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        Long businessId = business.getId();

        List<AdminBusinessUserSummary> result = new java.util.ArrayList<>();

        List<BusinessOwner> owners = businessOwnerRepository.findByBusinessId(businessId);
        List<Long> ownerUserIds = owners.stream().map(BusinessOwner::getUserId).distinct().toList();
        java.util.Map<Long, User> userMap = ownerUserIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : userRepository.findAllById(ownerUserIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        for (BusinessOwner bo : owners) {
            User u = userMap.get(bo.getUserId());
            result.add(AdminBusinessUserSummary.builder()
                    .userPublicId(u != null ? u.getPublicId() : null)
                    .email(u != null ? u.getEmail() : null)
                    .firstName(u != null ? u.getFirstName() : null)
                    .lastName(u != null ? u.getLastName() : null)
                    .role("OWNER")
                    .primaryOwner(bo.isPrimaryOwner())
                    .memberStatus(null)
                    .build());
        }

        List<BusinessMember> members = businessMemberRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        for (BusinessMember m : members) {
            User u = m.getUserId() != null ? userRepository.findById(m.getUserId()).orElse(null) : null;
            result.add(AdminBusinessUserSummary.builder()
                    .userPublicId(u != null ? u.getPublicId() : null)
                    .email(m.getEmail())
                    .firstName(u != null ? u.getFirstName() : null)
                    .lastName(u != null ? u.getLastName() : null)
                    .role("MEMBER")
                    .primaryOwner(null)
                    .memberStatus(m.getStatus() != null ? m.getStatus().name() : null)
                    .build());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersForBusiness(String businessPublicId, Pageable pageable) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        List<Long> storeIds = storeRepository.findByBusinessId(business.getId()).stream()
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .toList();
        if (storeIds.isEmpty()) {
            return PageResponse.of(new org.springframework.data.domain.PageImpl<Order>(List.of(), pageable, 0L).map(OrderResponse::from));
        }
        Page<Order> page = orderRepository.findByStoreIdIn(storeIds, pageable);
        return PageResponse.of(page.map(OrderResponse::from));
    }

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
