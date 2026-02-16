package com.securemarts.domain.admin.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.domain.admin.dto.AdminLoginRequest;
import com.securemarts.domain.admin.dto.AdminMeResponse;
import com.securemarts.domain.admin.entity.Admin;
import com.securemarts.domain.admin.repository.AdminPermissionRepository;
import com.securemarts.domain.admin.repository.AdminRepository;
import com.securemarts.domain.auth.dto.TokenResponse;
import com.securemarts.security.JwtProperties;
import com.securemarts.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public TokenResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));
        if (!admin.isActive()) {
            throw new BusinessRuleException("Admin account is inactive");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessRuleException("Invalid email or password");
        }
        List<String> roles = admin.getRoleCodes() != null && !admin.getRoleCodes().isEmpty()
                ? List.copyOf(admin.getRoleCodes())
                : List.of(Admin.AdminRole.PLATFORM_ADMIN.name());
        List<String> scopes = admin.getRoleCodes() != null && !admin.getRoleCodes().isEmpty()
                ? adminPermissionRepository.findPermissionCodesByRoleIn(admin.getRoleCodes())
                : adminPermissionRepository.findPermissionCodesByRoleIn(Set.of(Admin.AdminRole.PLATFORM_ADMIN.name()));
        String accessToken = jwtService.createAccessToken(
                admin.getPublicId(),
                admin.getEmail(),
                roles,
                scopes != null ? scopes : List.of(),
                null);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTtl())
                .userId(admin.getPublicId())
                .email(admin.getEmail())
                .roles(roles)
                .scopes(scopes != null ? scopes : List.of())
                .storeId(null)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminMeResponse getMe(String adminPublicId) {
        Admin admin = adminRepository.findByPublicId(adminPublicId)
                .orElseThrow(() -> new BusinessRuleException("Admin not found"));
        if (!admin.isActive()) {
            throw new BusinessRuleException("Admin account is inactive");
        }
        List<String> roles = admin.getRoleCodes() != null && !admin.getRoleCodes().isEmpty()
                ? List.copyOf(admin.getRoleCodes())
                : List.of(Admin.AdminRole.PLATFORM_ADMIN.name());
        List<String> scopes = admin.getRoleCodes() != null && !admin.getRoleCodes().isEmpty()
                ? adminPermissionRepository.findPermissionCodesByRoleIn(admin.getRoleCodes())
                : adminPermissionRepository.findPermissionCodesByRoleIn(Set.of(Admin.AdminRole.PLATFORM_ADMIN.name()));
        return AdminMeResponse.builder()
                .publicId(admin.getPublicId())
                .email(admin.getEmail())
                .fullName(admin.getFullName())
                .roles(roles)
                .scopes(scopes != null ? scopes : List.of())
                .build();
    }
}
