package com.shopper.domain.onboarding.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.onboarding.entity.BusinessMember;
import com.shopper.domain.onboarding.entity.Store;
import com.shopper.domain.onboarding.repository.BusinessMemberRepository;
import com.shopper.domain.onboarding.repository.BusinessOwnerRepository;
import com.shopper.domain.onboarding.repository.StoreRepository;
import com.shopper.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreAccessService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final BusinessMemberRepository businessMemberRepository;

    /** Ensures the user (owner or active staff) can access the store. Throws if not. */
    @Transactional(readOnly = true)
    public void ensureUserCanAccessStore(String userPublicId, String storePublicId) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
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
