package com.shopper.domain.onboarding.repository;

import com.shopper.domain.onboarding.entity.SubscriptionPlanLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanLimitRepository extends JpaRepository<SubscriptionPlanLimit, Long> {

    Optional<SubscriptionPlanLimit> findByPlan(String plan);
}
