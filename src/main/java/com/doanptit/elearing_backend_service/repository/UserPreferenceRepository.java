package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    
    Optional<UserPreference> findByUserId(Long userId);
    
    Optional<UserPreference> findByUserEmail(String email);
    
    boolean existsByUserId(Long userId);
    
    @Query("SELECT up FROM UserPreference up LEFT JOIN FETCH up.interestedCategories WHERE up.user.id = :userId")
    Optional<UserPreference> findByUserIdWithCategories(@Param("userId") Long userId);
}
