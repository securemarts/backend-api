package com.securemarts.domain.inventory.service;

import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.inventory.dto.CreateSupplierRequest;
import com.securemarts.domain.inventory.dto.SupplierResponse;
import com.securemarts.domain.inventory.dto.UpdateSupplierRequest;
import com.securemarts.domain.inventory.entity.Supplier;
import com.securemarts.domain.inventory.repository.SupplierRepository;
import com.securemarts.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional
    public SupplierResponse create(Long storeId, CreateSupplierRequest request) {
        Supplier s = new Supplier();
        s.setStoreId(storeId);
        s.setName(request.getName().trim());
        s.setEmail(request.getEmail());
        s.setPhone(request.getPhone());
        s.setCompany(request.getCompany());
        s.setAddress1(request.getAddress1());
        s.setAddress2(request.getAddress2());
        s.setCity(request.getCity());
        s.setState(request.getState());
        s.setCountry(request.getCountry());
        s.setPostalCode(request.getPostalCode());
        s.setNotes(request.getNotes());
        s = supplierRepository.save(s);
        return SupplierResponse.from(s);
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> list(Long storeId, boolean activeOnly, Pageable pageable) {
        var page = activeOnly
                ? supplierRepository.findByStoreIdAndActiveTrue(storeId, pageable)
                : supplierRepository.findByStoreId(storeId, pageable);
        return PageResponse.of(page.map(SupplierResponse::from));
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long storeId, String supplierPublicId) {
        Supplier s = findByStoreOrThrow(storeId, supplierPublicId);
        return SupplierResponse.from(s);
    }

    @Transactional
    public SupplierResponse update(Long storeId, String supplierPublicId, UpdateSupplierRequest request) {
        Supplier s = findByStoreOrThrow(storeId, supplierPublicId);
        if (request.getName() != null) s.setName(request.getName().trim());
        if (request.getEmail() != null) s.setEmail(request.getEmail());
        if (request.getPhone() != null) s.setPhone(request.getPhone());
        if (request.getCompany() != null) s.setCompany(request.getCompany());
        if (request.getAddress1() != null) s.setAddress1(request.getAddress1());
        if (request.getAddress2() != null) s.setAddress2(request.getAddress2());
        if (request.getCity() != null) s.setCity(request.getCity());
        if (request.getState() != null) s.setState(request.getState());
        if (request.getCountry() != null) s.setCountry(request.getCountry());
        if (request.getPostalCode() != null) s.setPostalCode(request.getPostalCode());
        if (request.getNotes() != null) s.setNotes(request.getNotes());
        s = supplierRepository.save(s);
        return SupplierResponse.from(s);
    }

    @Transactional
    public void deactivate(Long storeId, String supplierPublicId) {
        Supplier s = findByStoreOrThrow(storeId, supplierPublicId);
        s.setActive(false);
        supplierRepository.save(s);
    }

    public Supplier findByStoreOrThrow(Long storeId, String supplierPublicId) {
        return supplierRepository.findByPublicIdAndStoreId(supplierPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierPublicId));
    }
}
