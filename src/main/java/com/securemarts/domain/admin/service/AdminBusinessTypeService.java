package com.securemarts.domain.admin.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.admin.dto.AdminBusinessTypeRequest;
import com.securemarts.domain.admin.dto.AdminBusinessTypeResponse;
import com.securemarts.domain.onboarding.entity.BusinessType;
import com.securemarts.domain.onboarding.repository.BusinessTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBusinessTypeService {

    private final BusinessTypeRepository businessTypeRepository;

    @Transactional(readOnly = true)
    public Page<AdminBusinessTypeResponse> list(Pageable pageable) {
        Page<BusinessType> page = businessTypeRepository.findAll(pageable);
        return page.map(AdminBusinessTypeResponse::from);
    }

    @Transactional
    public AdminBusinessTypeResponse create(AdminBusinessTypeRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (businessTypeRepository.findByCode(code).isPresent()) {
            throw new BusinessRuleException("Business type code already exists: " + code);
        }
        BusinessType t = new BusinessType();
        t.setCode(code);
        t.setName(request.getName().trim());
        t.setDescription(request.getDescription());
        t.setIconKey(request.getIconKey());
        t = businessTypeRepository.save(t);
        return AdminBusinessTypeResponse.from(t);
    }

    @Transactional
    public AdminBusinessTypeResponse update(String publicId, AdminBusinessTypeRequest request) {
        BusinessType t = businessTypeRepository.findAll().stream()
                .filter(b -> publicId.equals(b.getPublicId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("BusinessType", publicId));
        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim().toUpperCase();
            if (!newCode.equals(t.getCode()) && businessTypeRepository.findByCode(newCode).isPresent()) {
                throw new BusinessRuleException("Business type code already exists: " + newCode);
            }
            t.setCode(newCode);
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            t.setName(request.getName().trim());
        }
        t.setDescription(request.getDescription());
        t.setIconKey(request.getIconKey());
        t = businessTypeRepository.save(t);
        return AdminBusinessTypeResponse.from(t);
    }

    @Transactional
    public void delete(String publicId) {
        BusinessType t = businessTypeRepository.findAll().stream()
                .filter(b -> publicId.equals(b.getPublicId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("BusinessType", publicId));
        businessTypeRepository.delete(t);
    }
}

