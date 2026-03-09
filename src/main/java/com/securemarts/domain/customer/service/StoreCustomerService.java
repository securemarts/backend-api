package com.securemarts.domain.customer.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.customer.dto.CreateStoreCustomerRequest;
import com.securemarts.domain.customer.dto.StoreCustomerResponse;
import com.securemarts.domain.customer.dto.UpdateStoreCustomerRequest;
import com.securemarts.domain.customer.entity.StoreCustomer;
import com.securemarts.domain.customer.repository.StoreCustomerRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreCustomerService {

    private final StoreCustomerRepository storeCustomerRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public StoreCustomerResponse create(Long storeId, CreateStoreCustomerRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", String.valueOf(storeId));
        }
        if (storeCustomerRepository.existsByStoreIdAndPhone(storeId, request.getPhone())) {
            throw new BusinessRuleException("A customer with this phone number already exists in this store");
        }
        StoreCustomer c = new StoreCustomer();
        c.setStoreId(storeId);
        c.setName(request.getName());
        c.setPhone(request.getPhone());
        c.setEmail(request.getEmail());
        c.setAddress(request.getAddress());
        c.setCreditLimit(request.getCreditLimit());
        c = storeCustomerRepository.save(c);
        return StoreCustomerResponse.from(c);
    }

    @Transactional(readOnly = true)
    public Page<StoreCustomerResponse> list(Long storeId, String search, Pageable pageable) {
        Page<StoreCustomer> page = search != null && !search.isBlank()
                ? storeCustomerRepository.findByStoreIdAndSearch(storeId, search.trim(), pageable)
                : storeCustomerRepository.findByStoreId(storeId, pageable);
        return page.map(StoreCustomerResponse::from);
    }

    @Transactional(readOnly = true)
    public StoreCustomerResponse get(Long storeId, String customerPublicId) {
        StoreCustomer c = storeCustomerRepository.findByStoreIdAndPublicId(storeId, customerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("StoreCustomer", customerPublicId));
        return StoreCustomerResponse.from(c);
    }

    @Transactional
    public StoreCustomerResponse update(Long storeId, String customerPublicId, UpdateStoreCustomerRequest request) {
        StoreCustomer c = storeCustomerRepository.findByStoreIdAndPublicId(storeId, customerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("StoreCustomer", customerPublicId));
        if (request.getName() != null) c.setName(request.getName());
        if (request.getPhone() != null) {
            if (storeCustomerRepository.existsByStoreIdAndPhone(storeId, request.getPhone())
                    && !request.getPhone().equals(c.getPhone())) {
                throw new BusinessRuleException("A customer with this phone number already exists in this store");
            }
            c.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) c.setEmail(request.getEmail());
        if (request.getAddress() != null) c.setAddress(request.getAddress());
        if (request.getCreditLimit() != null) c.setCreditLimit(request.getCreditLimit());
        c = storeCustomerRepository.save(c);
        return StoreCustomerResponse.from(c);
    }

    @Transactional
    public void delete(Long storeId, String customerPublicId) {
        StoreCustomer c = storeCustomerRepository.findByStoreIdAndPublicId(storeId, customerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("StoreCustomer", customerPublicId));
        storeCustomerRepository.delete(c);
    }

    /** Resolve store customer by store and public id; used by invoice and POS. */
    @Transactional(readOnly = true)
    public StoreCustomer getEntity(Long storeId, String customerPublicId) {
        return storeCustomerRepository.findByStoreIdAndPublicId(storeId, customerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("StoreCustomer", customerPublicId));
    }
}
