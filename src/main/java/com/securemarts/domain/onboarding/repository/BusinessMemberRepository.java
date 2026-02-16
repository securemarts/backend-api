package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.BusinessMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {

    Optional<BusinessMember> findByPublicId(String publicId);

    List<BusinessMember> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    long countByBusinessId(Long businessId);

    Optional<BusinessMember> findByBusinessIdAndEmail(Long businessId, String email);

    Optional<BusinessMember> findByBusinessIdAndUserId(Long businessId, Long userId);

    List<BusinessMember> findByUserId(Long userId);

    Optional<BusinessMember> findByInviteToken(String inviteToken);
}
