package com.securemarts.domain.onboarding.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.auth.entity.Role;
import com.securemarts.domain.auth.repository.RoleRepository;
import com.securemarts.domain.auth.repository.UserRepository;
import com.securemarts.domain.catalog.service.FileStorageService;
import com.securemarts.domain.catalog.service.StoreBootstrapService;
import com.securemarts.mail.EmailService;
import com.securemarts.domain.onboarding.dto.*;
import com.securemarts.domain.onboarding.entity.*;
import com.securemarts.domain.onboarding.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final BusinessRepository businessRepository;
    private final StoreRepository storeRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final ComplianceDocumentRepository complianceDocumentRepository;
    private final BankAccountRepository bankAccountRepository;
    private final StoreProfileRepository storeProfileRepository;
    private final UserRepository userRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final MerchantRoleRepository merchantRoleRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionLimitsService subscriptionLimitsService;
    private final BusinessTypeRepository businessTypeRepository;
    private final FileStorageService fileStorageService;
    private final StoreBootstrapService storeBootstrapService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public boolean isSlugTaken(String slug) {
        if (slug == null || slug.isBlank()) return true;
        return storeRepository.existsByDomainSlug(slug.toLowerCase().trim());
    }

    @Transactional
    public BusinessResponse createBusiness(String userPublicId, CreateBusinessRequest request, MultipartFile logo,
                                           String storeName, String domainSlug, String defaultCurrency) throws IOException {
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        if (businessOwnerRepository.findByUserId(user.getId()).stream().anyMatch(bo -> bo.isPrimaryOwner())) {
            throw new BusinessRuleException("User already owns a business");
        }
        if (request.getLegalName() == null || request.getLegalName().isBlank()) {
            throw new BusinessRuleException("legalName is required");
        }
        if (request.getBusinessTypePublicId() == null || request.getBusinessTypePublicId().isBlank()) {
            throw new BusinessRuleException("businessTypePublicId is required");
        }
        if (logo == null || logo.isEmpty()) {
            throw new BusinessRuleException("logo is required");
        }
        String contentType = logo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessRuleException("Logo file must be an image (PNG, JPEG, or WebP)");
        }
        if (storeName == null || storeName.isBlank()) {
            throw new BusinessRuleException("storeName is required");
        }
        if (domainSlug == null || domainSlug.isBlank()) {
            throw new BusinessRuleException("domainSlug is required");
        }
        String normalizedSlug = domainSlug.toLowerCase().trim();
        if (!normalizedSlug.matches("^[a-z0-9][a-z0-9-]*[a-z0-9]$")) {
            throw new BusinessRuleException("domainSlug must be lowercase alphanumeric with hyphens, starting and ending with an alphanumeric character");
        }
        if (storeRepository.existsByDomainSlug(normalizedSlug)) {
            throw new BusinessRuleException("Domain slug already taken");
        }

        String typePublicId = request.getBusinessTypePublicId().trim();
        BusinessType type = businessTypeRepository.findByPublicId(typePublicId)
                .orElseThrow(() -> new BusinessRuleException("Invalid businessTypePublicId: " + typePublicId));

        Business business = new Business();
        business.setLegalName(request.getLegalName().trim());
        business.setTradeName(request.getTradeName() != null ? request.getTradeName().trim() : null);
        business.setCacNumber(request.getCacNumber() != null ? request.getCacNumber().trim() : null);
        business.setBusinessType(type);
        business.setVerificationStatus(Business.VerificationStatus.PENDING);
        business.setSubscriptionPlan(Business.SubscriptionPlan.BASIC);
        business.setSubscriptionStatus(Business.SubscriptionStatus.NONE);
        business.setTrialEndsAt(null);
        business = businessRepository.save(business);

        BusinessOwner owner = new BusinessOwner();
        owner.setBusiness(business);
        owner.setUserId(user.getId());
        owner.setPrimaryOwner(true);
        businessOwnerRepository.save(owner);

        Store store = new Store();
        store.setBusiness(business);
        store.setName(storeName.trim());
        store.setDomainSlug(normalizedSlug);
        store.setDefaultCurrency(defaultCurrency != null && !defaultCurrency.isBlank() ? defaultCurrency : "NGN");
        store.setBusinessType(type);
        store.setActive(false);
        store = storeRepository.save(store);

        StoreProfile profile = new StoreProfile();
        profile.setStore(store);
        profile.setCountry("NG");

        String logoUrl = fileStorageService.storeStoreLogo(store.getPublicId(), logo);
        if (logoUrl != null) {
            profile.setLogoUrl(logoUrl);
            business.setLogoUrl(logoUrl);
            businessRepository.save(business);
        }
        storeProfileRepository.save(profile);
        store.setProfile(profile);

        business.getStores().add(store);

        storeBootstrapService.bootstrapDefaults(store.getId());

        try {
            emailService.sendWelcomeOnboard(user.getEmail(), user.getFirstName());
            log.info("Welcome onboard email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome onboard email to {} – {}", user.getEmail(), e.getMessage(), e);
        }
        return BusinessResponse.from(business);
    }

    @Transactional
    public StoreResponse createStore(String userPublicId, String businessPublicId,
                                     CreateStoreRequest request, MultipartFile logo) throws IOException {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        var limits = subscriptionLimitsService.getLimitsForBusiness(business);
        int storeCount = (int) storeRepository.countByBusinessId(business.getId());
        if (storeCount >= limits.getMaxStores()) {
            throw new BusinessRuleException("Store limit reached for your plan (" + limits.getMaxStores() + "). Upgrade to add more stores.");
        }
        if (logo == null || logo.isEmpty()) {
            throw new BusinessRuleException("logo is required");
        }
        String contentType = logo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessRuleException("Logo file must be an image (PNG, JPEG, or WebP)");
        }
        if (request.getBusinessTypePublicId() == null || request.getBusinessTypePublicId().isBlank()) {
            throw new BusinessRuleException("businessTypePublicId is required");
        }
        String normalizedSlug = request.getDomainSlug().toLowerCase().trim();
        if (storeRepository.existsByDomainSlug(normalizedSlug)) {
            throw new BusinessRuleException("Domain slug already taken");
        }
        String typePublicId = request.getBusinessTypePublicId().trim();
        BusinessType type = businessTypeRepository.findByPublicId(typePublicId)
                .orElseThrow(() -> new BusinessRuleException("Invalid businessTypePublicId: " + typePublicId));

        Store store = new Store();
        store.setBusiness(business);
        store.setName(request.getName().trim());
        store.setDomainSlug(normalizedSlug);
        store.setDefaultCurrency(request.getDefaultCurrency() != null ? request.getDefaultCurrency() : "NGN");
        store.setBusinessType(type);
        store.setActive(false);
        store = storeRepository.save(store);

        StoreProfile profile = new StoreProfile();
        profile.setStore(store);
        profile.setCountry("NG");
        String logoUrl = fileStorageService.storeStoreLogo(store.getPublicId(), logo);
        if (logoUrl != null) {
            profile.setLogoUrl(logoUrl);
        }
        storeProfileRepository.save(profile);
        store.setProfile(profile);

        storeBootstrapService.bootstrapDefaults(store.getId());

        return StoreResponse.from(store);
    }

    @Transactional
    public ComplianceDocument uploadComplianceDocument(String userPublicId, String businessPublicId,
                                                        UploadComplianceDocumentRequest request) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        ComplianceDocument doc = new ComplianceDocument();
        doc.setBusiness(business);
        doc.setDocumentType(request.getDocumentType());
        doc.setFileUrl(request.getFileUrl());
        doc.setFileName(request.getFileName());
        doc.setMimeType(request.getMimeType());
        doc.setStatus(ComplianceDocument.DocumentStatus.PENDING);
        return complianceDocumentRepository.save(doc);
    }

    @Transactional
    public BankAccount addBankAccount(String userPublicId, String storePublicId, AddBankAccountRequest request) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        ensureUserOwnsStore(userPublicId, store.getId());
        if (request.isPayoutDefault()) {
            bankAccountRepository.findByStoreIdAndPayoutDefaultTrue(store.getId())
                    .ifPresent(ba -> {
                        ba.setPayoutDefault(false);
                        bankAccountRepository.save(ba);
                    });
        }
        BankAccount account = new BankAccount();
        account.setStore(store);
        account.setBankCode(request.getBankCode());
        account.setBankName(request.getBankName());
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountName(request.getAccountName());
        account.setPayoutDefault(request.isPayoutDefault());
        return bankAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BusinessResponse getBusiness(String userPublicId, String businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        return BusinessResponse.from(business);
    }

    @Transactional(readOnly = true)
    public StoreResponse getStore(String userPublicId, String storePublicId) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        ensureUserHasStoreAccess(userPublicId, store.getId());
        return StoreResponse.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> listStoresForUser(String userPublicId) {
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        List<Long> ownerBusinessIds = businessOwnerRepository.findByUserId(user.getId()).stream()
                .map(bo -> bo.getBusiness().getId())
                .toList();
        List<Long> memberBusinessIds = businessMemberRepository.findByUserId(user.getId()).stream()
                .filter(m -> m.getStatus() == BusinessMember.MemberStatus.ACTIVE)
                .map(m -> m.getBusiness().getId())
                .toList();
        List<Long> businessIds = java.util.stream.Stream.concat(ownerBusinessIds.stream(), memberBusinessIds.stream()).distinct().toList();
        return storeRepository.findByBusinessIdIn(businessIds).stream()
                .map(StoreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(String userPublicId) {
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));

        var userSummary = OnboardingStatusResponse.UserSummary.builder()
                .publicId(user.getPublicId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        List<BusinessOwner> ownerships = businessOwnerRepository.findByUserId(user.getId());
        BusinessOwner primaryOwnership = ownerships.stream()
                .filter(BusinessOwner::isPrimaryOwner)
                .findFirst().orElse(null);

        if (primaryOwnership == null) {
            return OnboardingStatusResponse.builder()
                    .user(userSummary)
                    .stores(List.of())
                    .steps(OnboardingStatusResponse.Steps.builder().build())
                    .currentStep("BUSINESS_CREATED")
                    .completed(false)
                    .build();
        }

        Business business = primaryOwnership.getBusiness();
        var businessSummary = OnboardingStatusResponse.BusinessSummary.builder()
                .publicId(business.getPublicId())
                .legalName(business.getLegalName())
                .verificationStatus(business.getVerificationStatus().name())
                .subscriptionPlan(business.getSubscriptionPlan().name())
                .build();

        List<Store> stores = storeRepository.findByBusinessId(business.getId());
        boolean anyBankAccount = false;
        boolean anyActive = false;
        List<OnboardingStatusResponse.StoreSummary> storeSummaries = new java.util.ArrayList<>();
        for (Store store : stores) {
            boolean hasBankAccount = !bankAccountRepository.findByStoreId(store.getId()).isEmpty();
            if (hasBankAccount) anyBankAccount = true;
            if (store.isActive()) anyActive = true;
            storeSummaries.add(OnboardingStatusResponse.StoreSummary.builder()
                    .publicId(store.getPublicId())
                    .name(store.getName())
                    .domainSlug(store.getDomainSlug())
                    .active(store.isActive())
                    .hasBankAccount(hasBankAccount)
                    .build());
        }

        boolean documentsUploaded = !complianceDocumentRepository.findByBusinessId(business.getId()).isEmpty();
        boolean submitted = business.getVerificationStatus() != Business.VerificationStatus.PENDING;

        var steps = OnboardingStatusResponse.Steps.builder()
                .businessCreated(true)
                .storeCreated(!stores.isEmpty())
                .documentsUploaded(documentsUploaded)
                .bankAccountAdded(anyBankAccount)
                .submittedForVerification(submitted)
                .storeActivated(anyActive)
                .build();

        String currentStep = null;
        if (!steps.isStoreCreated()) currentStep = "STORE_CREATED";
        else if (!steps.isDocumentsUploaded()) currentStep = "DOCUMENTS_UPLOADED";
        else if (!steps.isBankAccountAdded()) currentStep = "BANK_ACCOUNT_ADDED";
        else if (!steps.isSubmittedForVerification()) currentStep = "SUBMITTED_FOR_VERIFICATION";
        else if (!steps.isStoreActivated()) currentStep = "STORE_ACTIVATED";

        boolean completed = steps.isBusinessCreated() && steps.isStoreCreated()
                && steps.isDocumentsUploaded() && steps.isBankAccountAdded()
                && steps.isSubmittedForVerification();

        return OnboardingStatusResponse.builder()
                .user(userSummary)
                .business(businessSummary)
                .stores(storeSummaries)
                .steps(steps)
                .currentStep(currentStep)
                .completed(completed)
                .build();
    }

    @Transactional
    public void submitForVerification(String userPublicId, String businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        if (business.getVerificationStatus() != Business.VerificationStatus.PENDING) {
            throw new BusinessRuleException("Business already submitted or verified");
        }
        business.setVerificationStatus(Business.VerificationStatus.UNDER_REVIEW);
        businessRepository.save(business);
    }

    @Transactional
    public void activateStore(String userPublicId, String storePublicId) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        ensureUserOwnsStore(userPublicId, store.getId());
        if (store.getBusiness().getVerificationStatus() != Business.VerificationStatus.APPROVED) {
            throw new BusinessRuleException("Business must be verified before activating store");
        }
        store.setActive(true);
        storeRepository.save(store);
    }

    @Transactional(readOnly = true)
    public List<BusinessMemberResponse> listMembers(String userPublicId, String businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        return businessMemberRepository.findByBusinessIdOrderByCreatedAtDesc(business.getId()).stream()
                .map(m -> toMemberResponse(m))
                .toList();
    }

    @Transactional
    public BusinessMemberResponse inviteMember(String userPublicId, String businessPublicId, InviteMemberRequest request) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        var limits = subscriptionLimitsService.getLimitsForBusiness(business);
        if (businessMemberRepository.countByBusinessId(business.getId()) >= limits.getMaxStaff()) {
            throw new BusinessRuleException("Staff limit reached for your plan (" + limits.getMaxStaff() + "). Upgrade to add more staff.");
        }
        String email = request.getEmail().toLowerCase().trim();
        if (businessMemberRepository.findByBusinessIdAndEmail(business.getId(), email).isPresent()) {
            throw new BusinessRuleException("Member with this email already exists or has been invited");
        }
        Set<MerchantRole> roles = resolveMerchantRoles(List.of(request.getRole()));
        BusinessMember member = new BusinessMember();
        member.setBusiness(business);
        member.setEmail(email);
        member.setMerchantRoles(roles);
        member.setStatus(BusinessMember.MemberStatus.INVITED);
        member.setInviteToken(UUID.randomUUID().toString().replace("-", ""));
        member.setInvitedAt(Instant.now());
        member = businessMemberRepository.save(member);
        return toMemberResponse(member);
    }

    @Transactional
    public BusinessMemberResponse addMember(String userPublicId, String businessPublicId, AddMemberRequest request) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        var user = userRepository.findByPublicId(request.getUserPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserPublicId()));
        if (businessOwnerRepository.findByBusinessIdAndUserId(business.getId(), user.getId()).isPresent()) {
            throw new BusinessRuleException("User is already an owner of this business");
        }
        BusinessMember existing = businessMemberRepository.findByBusinessIdAndUserId(business.getId(), user.getId()).orElse(null);
        if (existing == null) {
            var limits = subscriptionLimitsService.getLimitsForBusiness(business);
            if (businessMemberRepository.countByBusinessId(business.getId()) >= limits.getMaxStaff()) {
                throw new BusinessRuleException("Staff limit reached for your plan (" + limits.getMaxStaff() + "). Upgrade to add more staff.");
            }
        }
        if (existing != null && existing.getStatus() == BusinessMember.MemberStatus.ACTIVE) {
            throw new BusinessRuleException("User is already a member");
        }
        Set<MerchantRole> roles = resolveMerchantRoles(List.of(request.getRole()));
        if (existing != null) {
            existing.setUserId(user.getId());
            existing.setEmail(user.getEmail());
            existing.setMerchantRoles(roles);
            existing.setStatus(BusinessMember.MemberStatus.ACTIVE);
            existing.setJoinedAt(Instant.now());
            existing = businessMemberRepository.save(existing);
            assignMerchantStaffRoleIfNeeded(user);
            return toMemberResponse(existing);
        }
        if (businessMemberRepository.findByBusinessIdAndEmail(business.getId(), user.getEmail()).isPresent()) {
            throw new BusinessRuleException("Member with this email already exists or has been invited");
        }
        BusinessMember member = new BusinessMember();
        member.setBusiness(business);
        member.setUserId(user.getId());
        member.setEmail(user.getEmail());
        member.setMerchantRoles(roles);
        member.setStatus(BusinessMember.MemberStatus.ACTIVE);
        member.setJoinedAt(Instant.now());
        member = businessMemberRepository.save(member);
        assignMerchantStaffRoleIfNeeded(user);
        return toMemberResponse(member);
    }

    @Transactional
    public BusinessMemberResponse updateMember(String userPublicId, String businessPublicId, String memberPublicId, UpdateMemberRequest request) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        BusinessMember member = businessMemberRepository.findByPublicId(memberPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessMember", memberPublicId));
        if (!member.getBusiness().getId().equals(business.getId())) {
            throw new ResourceNotFoundException("BusinessMember", memberPublicId);
        }
        boolean updated = false;
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            member.setMerchantRoles(resolveMerchantRoles(request.getRoles()));
            updated = true;
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            BusinessMember.MemberStatus newStatus = parseMemberStatus(request.getStatus());
            member.setStatus(newStatus);
            updated = true;
        }
        if (!updated) {
            throw new BusinessRuleException("Provide at least one of: roles, status");
        }
        member = businessMemberRepository.save(member);
        return toMemberResponse(member);
    }

    private BusinessMember.MemberStatus parseMemberStatus(String status) {
        try {
            return BusinessMember.MemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid status. Use INVITED, ACTIVE, or DEACTIVATED");
        }
    }

    @Transactional
    public void removeMember(String userPublicId, String businessPublicId, String memberPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        BusinessMember member = businessMemberRepository.findByPublicId(memberPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessMember", memberPublicId));
        if (!member.getBusiness().getId().equals(business.getId())) {
            throw new ResourceNotFoundException("BusinessMember", memberPublicId);
        }
        businessMemberRepository.delete(member);
    }

    private BusinessMemberResponse toMemberResponse(BusinessMember m) {
        String userPublicId = m.getUserId() != null
                ? userRepository.findById(m.getUserId()).map(com.securemarts.domain.auth.entity.User::getPublicId).orElse(null)
                : null;
        return BusinessMemberResponse.builder()
                .publicId(m.getPublicId())
                .email(m.getEmail())
                .roles(m.getRoleCodes() != null ? List.copyOf(m.getRoleCodes()) : List.of())
                .status(m.getStatus() != null ? m.getStatus().name() : null)
                .userPublicId(userPublicId)
                .invitedAt(m.getInvitedAt())
                .joinedAt(m.getJoinedAt())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private void assignMerchantStaffRoleIfNeeded(com.securemarts.domain.auth.entity.User user) {
        if (user.getRoles().stream().anyMatch(r -> "MERCHANT_STAFF".equals(r.getCode()))) {
            return;
        }
        roleRepository.findByCode("MERCHANT_STAFF").ifPresent(role -> {
            user.getRoles().add(role);
            userRepository.save(user);
        });
    }

    private Set<MerchantRole> resolveMerchantRoles(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Set.of();
        }
        Set<String> codes = roleCodes.stream().map(String::toUpperCase).collect(Collectors.toSet());
        List<MerchantRole> found = merchantRoleRepository.findByCodeIn(codes);
        if (found.size() != codes.size()) {
            var foundCodes = found.stream().map(MerchantRole::getCode).collect(Collectors.toSet());
            var missing = codes.stream().filter(c -> !foundCodes.contains(c)).findFirst();
            throw new BusinessRuleException("Invalid or unknown role: " + missing.orElse(""));
        }
        return new HashSet<>(found);
    }

    private void ensureUserOwnsBusiness(String userPublicId, Long businessId) {
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        if (businessOwnerRepository.findByBusinessIdAndUserId(businessId, user.getId()).isEmpty()) {
            throw new BusinessRuleException("User does not own this business");
        }
    }

    private void ensureUserOwnsStore(String userPublicId, Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        ensureUserOwnsBusiness(userPublicId, store.getBusiness().getId());
    }

    private void ensureUserHasStoreAccess(String userPublicId, Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow();
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        Long businessId = store.getBusiness().getId();
        if (businessOwnerRepository.findByBusinessIdAndUserId(businessId, user.getId()).isPresent()) {
            return;
        }
        if (businessMemberRepository.findByUserId(user.getId()).stream()
                .anyMatch(m -> m.getBusiness().getId().equals(businessId) && m.getStatus() == BusinessMember.MemberStatus.ACTIVE)) {
            return;
        }
        throw new BusinessRuleException("You do not have access to this store");
    }
}
