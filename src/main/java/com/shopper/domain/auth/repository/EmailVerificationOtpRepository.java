package com.shopper.domain.auth.repository;

import com.shopper.domain.auth.entity.EmailVerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, Long> {

    Optional<EmailVerificationOtp> findByEmailIgnoreCaseAndTargetType(String email, EmailVerificationOtp.TargetType targetType);

    void deleteByEmailIgnoreCaseAndTargetType(String email, EmailVerificationOtp.TargetType targetType);
}
