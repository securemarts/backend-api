package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByStoreId(Long storeId);

    Optional<BankAccount> findByStoreIdAndPayoutDefaultTrue(Long storeId);
}
