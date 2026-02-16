package com.securemarts.domain.pos.repository;

import com.securemarts.domain.pos.entity.POSRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface POSRegisterRepository extends JpaRepository<POSRegister, Long> {

    Optional<POSRegister> findByPublicId(String publicId);

    List<POSRegister> findByStoreId(Long storeId);

    long countByStoreId(Long storeId);

    Optional<POSRegister> findByStoreIdAndPublicId(Long storeId, String publicId);
}
