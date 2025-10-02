package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.PasswordResetToken;
import com.doanptit.elearing_backend_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}