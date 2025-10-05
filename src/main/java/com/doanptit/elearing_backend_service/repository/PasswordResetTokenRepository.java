package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.PasswordResetToken;
import com.doanptit.elearing_backend_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndDeletedOnFalse(String token);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.deletedOn = true WHERE t.user = :user")
    void softDeleteByUser(@Param("user") User user);
}