package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
    boolean existsByEmail(String email);
}
