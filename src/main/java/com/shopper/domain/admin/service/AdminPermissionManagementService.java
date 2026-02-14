package com.shopper.domain.admin.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.admin.dto.*;
import com.shopper.domain.admin.entity.AdminPermission;
import com.shopper.domain.admin.repository.AdminPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPermissionManagementService {

    private final AdminPermissionRepository adminPermissionRepository;

    @Transactional(readOnly = true)
    public List<AdminPermissionResponse> listPermissions() {
        return adminPermissionRepository.findAll().stream()
                .map(AdminPermissionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminPermissionResponse getPermission(String permissionPublicId) {
        AdminPermission p = adminPermissionRepository.findByPublicId(permissionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", permissionPublicId));
        return AdminPermissionResponse.from(p);
    }

    @Transactional
    public AdminPermissionResponse createPermission(CreateAdminPermissionRequest request) {
        String code = request.getCode().trim();
        if (adminPermissionRepository.existsByCode(code)) {
            throw new BusinessRuleException("Permission with code " + code + " already exists");
        }
        AdminPermission p = new AdminPermission();
        p.setCode(code);
        p.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        p = adminPermissionRepository.save(p);
        return AdminPermissionResponse.from(p);
    }

    @Transactional
    public AdminPermissionResponse updatePermission(String permissionPublicId, UpdateAdminPermissionRequest request) {
        AdminPermission p = adminPermissionRepository.findByPublicId(permissionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", permissionPublicId));
        if (request.getDescription() != null) {
            p.setDescription(request.getDescription().trim());
        }
        p = adminPermissionRepository.save(p);
        return AdminPermissionResponse.from(p);
    }

    @Transactional
    public void deletePermission(String permissionPublicId) {
        AdminPermission p = adminPermissionRepository.findByPublicId(permissionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", permissionPublicId));
        adminPermissionRepository.delete(p);
    }
}
