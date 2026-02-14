package com.shopper.domain.onboarding.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.onboarding.entity.BusinessMember;
import com.shopper.domain.onboarding.entity.Store;
import com.shopper.domain.onboarding.repository.BusinessMemberRepository;
import com.shopper.domain.onboarding.repository.BusinessOwnerRepository;
import com.shopper.domain.onboarding.repository.MerchantPermissionRepository;
import com.shopper.domain.onboarding.repository.StoreRepository;
import com.shopper.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Resolves merchant (store) permissions for a user in a store context.
 * Owners have all permissions; staff have permissions from their business member role (MANAGER/CASHIER/STAFF).
 */
@Service
@RequiredArgsConstructor
public class MerchantPermissionService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final MerchantPermissionRepository merchantPermissionRepository;

    /**
     * Throws if the user does not have the required permission in the given store.
     * Use after resolving store (by path) and ensuring user has store access.
     */
    @Transactional(readOnly = true)
    public void ensureStorePermission(String userPublicId, Long storeId, String requiredPermission) {
        if (requiredPermission == null || requiredPermission.isBlank()) {
            return;
        }
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        Long businessId = store.getBusiness().getId();

        if (businessOwnerRepository.findByBusinessIdAndUserId(businessId, user.getId()).isPresent()) {
            return;
        }

        List<BusinessMember> members = businessMemberRepository.findByUserId(user.getId()).stream()
                .filter(m -> m.getBusiness().getId().equals(businessId) && m.getStatus() == BusinessMember.MemberStatus.ACTIVE)
                .toList();
        if (members.isEmpty()) {
            throw new BusinessRuleException("You do not have access to this store");
        }
        var roleCodes = members.get(0).getRoleCodes();
        if (roleCodes.isEmpty()) {
            throw new BusinessRuleException("You do not have permission: " + requiredPermission);
        }
        List<String> allowed = merchantPermissionRepository.findPermissionCodesByRoleIn(roleCodes);
        if (allowed == null || !allowed.contains(requiredPermission)) {
            throw new BusinessRuleException("You do not have permission: " + requiredPermission);
        }
    }

    @Transactional(readOnly = true)
    public void ensureStorePermissionByPublicId(String userPublicId, String storePublicId, String requiredPermission) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        ensureStorePermission(userPublicId, store.getId(), requiredPermission);
    }
}
