package com.securemarts.domain.auth.repository;

import com.securemarts.domain.auth.entity.LoginSession;
import com.securemarts.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {

    List<LoginSession> findByUserAndActiveTrue(User user);
}
