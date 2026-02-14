package com.shopper.domain.admin.repository;

import com.shopper.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByPublicId(String publicId);

    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Admin> findAllByOrderByCreatedAtDesc();
}
