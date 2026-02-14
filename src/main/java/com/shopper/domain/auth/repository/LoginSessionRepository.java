package com.shopper.domain.auth.repository;

import com.shopper.domain.auth.entity.LoginSession;
import com.shopper.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {

    List<LoginSession> findByUserAndActiveTrue(User user);
}
