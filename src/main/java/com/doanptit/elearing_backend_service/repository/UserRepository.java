package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    int countByRole(Role role);

    List<User> findByRole(Role role);

    Page<User> findByRole(Role role, Pageable pageable);
}
