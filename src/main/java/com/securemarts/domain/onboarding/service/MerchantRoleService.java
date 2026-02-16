package com.securemarts.domain.onboarding.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.onboarding.dto.*;
import com.securemarts.domain.onboarding.entity.MerchantPermission;
import com.securemarts.domain.onboarding.entity.MerchantRole;
import com.securemarts.domain.onboarding.entity.MerchantRolePermission;
import com.securemarts.domain.onboarding.repository.MerchantPermissionRepository;
import com.securemarts.domain.onboarding.repository.MerchantRolePermissionRepository;
import com.securemarts.domain.onboarding.repository.MerchantRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantRoleService {

    private final MerchantRoleRepository merchantRoleRepository;
    private final MerchantPermissionRepository merchantPermissionRepository;
    private final MerchantRolePermissionRepository merchantRolePermissionRepository;

    @Transactional(readOnly = true)
    public List<MerchantRoleResponse> listRoles() {
        return merchantRoleRepository.findAllByOrderByCodeAsc().stream()
                .map(MerchantRoleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MerchantRoleResponse getRole(String rolePublicId) {
        MerchantRole role = merchantRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantRole", rolePublicId));
        return MerchantRoleResponse.from(role);
    }

    @Transactional
    public MerchantRoleResponse createRole(CreateMerchantRoleRequest request) {
        String code = request.getCode().toUpperCase().trim();
        if (merchantRoleRepository.existsByCode(code)) {
            throw new BusinessRuleException("Merchant role with code " + code + " already exists");
        }
        MerchantRole role = new MerchantRole();
        role.setCode(code);
        role.setName(request.getName() != null ? request.getName().trim() : null);
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        role = merchantRoleRepository.save(role);
        return MerchantRoleResponse.from(role);
    }

    @Transactional
    public MerchantRoleResponse updateRole(String rolePublicId, UpdateMerchantRoleRequest request) {
        MerchantRole role = merchantRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantRole", rolePublicId));
        if (request.getName() != null) role.setName(request.getName().trim());
        if (request.getDescription() != null) role.setDescription(request.getDescription().trim());
        role = merchantRoleRepository.save(role);
        return MerchantRoleResponse.from(role);
    }

    @Transactional
    public void deleteRole(String rolePublicId) {
        MerchantRole role = merchantRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantRole", rolePublicId));
        merchantRoleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public List<String> getPermissionCodesForRole(String rolePublicId) {
        MerchantRole role = merchantRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantRole", rolePublicId));
        return merchantRolePermissionRepository.findByRoleId(role.getId()).stream()
                .map(MerchantRolePermission::getPermissionId)
                .map(merchantPermissionRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get().getCode())
                .toList();
    }

    @Transactional
    public List<String> assignPermissionsToRole(String rolePublicId, AssignMerchantPermissionsToRoleRequest request) {
        MerchantRole role = merchantRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantRole", rolePublicId));
        Set<String> codes = request.getPermissionCodes() != null
                ? request.getPermissionCodes().stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet())
                : Set.of();
        merchantRolePermissionRepository.deleteByRoleId(role.getId());
        for (String code : codes) {
            MerchantPermission perm = merchantPermissionRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("MerchantPermission", code));
            merchantRolePermissionRepository.save(new MerchantRolePermission(role.getId(), perm.getId()));
        }
        return getPermissionCodesForRole(rolePublicId);
    }
}
