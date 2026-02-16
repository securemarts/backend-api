package com.securemarts.domain.onboarding.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.onboarding.dto.*;
import com.securemarts.domain.onboarding.entity.MerchantPermission;
import com.securemarts.domain.onboarding.repository.MerchantPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantPermissionManagementService {

    private final MerchantPermissionRepository merchantPermissionRepository;

    @Transactional(readOnly = true)
    public List<MerchantPermissionResponse> listPermissions() {
        return merchantPermissionRepository.findAll().stream()
                .map(MerchantPermissionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MerchantPermissionResponse getPermission(String permissionPublicId) {
        MerchantPermission p = merchantPermissionRepository.findByPublicId(permissionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantPermission", permissionPublicId));
        return MerchantPermissionResponse.from(p);
    }

    @Transactional
    public MerchantPermissionResponse createPermission(CreateMerchantPermissionRequest request) {
        String code = request.getCode().trim();
        if (merchantPermissionRepository.existsByCode(code)) {
            throw new BusinessRuleException("Merchant permission with code " + code + " already exists");
        }
        MerchantPermission p = new MerchantPermission();
        p.setCode(code);
        p.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        p = merchantPermissionRepository.save(p);
        return MerchantPermissionResponse.from(p);
    }

    @Transactional
    public MerchantPermissionResponse updatePermission(String permissionPublicId, UpdateMerchantPermissionRequest request) {
        MerchantPermission p = merchantPermissionRepository.findByPublicId(permissionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantPermission", permissionPublicId));
        if (request.getDescription() != null) p.setDescription(request.getDescription().trim());
        p = merchantPermissionRepository.save(p);
        return MerchantPermissionResponse.from(p);
    }

    @Transactional
    public void deletePermission(String permissionPublicId) {
        MerchantPermission p = merchantPermissionRepository.findByPublicId(permissionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantPermission", permissionPublicId));
        merchantPermissionRepository.delete(p);
    }
}
