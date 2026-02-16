package com.securemarts.domain.admin.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.admin.dto.*;
import com.securemarts.domain.admin.entity.AdminPermission;
import com.securemarts.domain.admin.entity.AdminRolePermission;
import com.securemarts.domain.admin.entity.PlatformRole;
import com.securemarts.domain.admin.repository.AdminPermissionRepository;
import com.securemarts.domain.admin.repository.AdminRolePermissionRepository;
import com.securemarts.domain.admin.repository.PlatformRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformRoleService {

    private final PlatformRoleRepository platformRoleRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final AdminRolePermissionRepository adminRolePermissionRepository;

    @Transactional(readOnly = true)
    public List<PlatformRoleResponse> listRoles() {
        return platformRoleRepository.findAllByOrderByCodeAsc().stream()
                .map(PlatformRoleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformRoleResponse getRole(String rolePublicId) {
        PlatformRole role = platformRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", rolePublicId));
        return PlatformRoleResponse.from(role);
    }

    @Transactional
    public PlatformRoleResponse createRole(CreatePlatformRoleRequest request) {
        String code = request.getCode().toUpperCase().trim();
        if (platformRoleRepository.existsByCode(code)) {
            throw new BusinessRuleException("Role with code " + code + " already exists");
        }
        PlatformRole role = new PlatformRole();
        role.setCode(code);
        role.setName(request.getName() != null ? request.getName().trim() : null);
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        role = platformRoleRepository.save(role);
        return PlatformRoleResponse.from(role);
    }

    @Transactional
    public PlatformRoleResponse updateRole(String rolePublicId, UpdatePlatformRoleRequest request) {
        PlatformRole role = platformRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", rolePublicId));
        if (request.getName() != null) {
            role.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription().trim());
        }
        role = platformRoleRepository.save(role);
        return PlatformRoleResponse.from(role);
    }

    @Transactional
    public void deleteRole(String rolePublicId) {
        PlatformRole role = platformRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", rolePublicId));
        platformRoleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public List<String> getPermissionCodesForRole(String rolePublicId) {
        PlatformRole role = platformRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", rolePublicId));
        List<AdminRolePermission> assignments = adminRolePermissionRepository.findByRoleId(role.getId());
        return assignments.stream()
                .map(AdminRolePermission::getPermissionId)
                .map(adminPermissionRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get().getCode())
                .toList();
    }

    @Transactional
    public List<String> assignPermissionsToRole(String rolePublicId, AssignPermissionsToRoleRequest request) {
        PlatformRole role = platformRoleRepository.findByPublicId(rolePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", rolePublicId));
        Set<String> codes = request.getPermissionCodes() != null
                ? request.getPermissionCodes().stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet())
                : Set.of();
        adminRolePermissionRepository.deleteByRoleId(role.getId());
        for (String code : codes) {
            AdminPermission perm = adminPermissionRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission", code));
            adminRolePermissionRepository.save(new AdminRolePermission(role.getId(), perm.getId()));
        }
        return getPermissionCodesForRole(rolePublicId);
    }
}
