package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.enums.InteractionType;
import com.doanptit.elearing_backend_service.model.UserInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    
    Page<UserInteraction> findByUserId(Long userId, Pageable pageable);
    
    Page<UserInteraction> findByUserIdAndInteractionType(Long userId, InteractionType type, Pageable pageable);
    
    List<UserInteraction> findByUserIdAndCourseId(Long userId, Long courseId);
    
    // Count interactions by type for a user-course pair
    long countByUserIdAndCourseIdAndInteractionType(Long userId, Long courseId, InteractionType type);
    
    // Get recent interactions for a user
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.user.id = :userId AND ui.createdOn >= :since ORDER BY ui.createdOn DESC")
    List<UserInteraction> findRecentByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    // Get all interactions for recommendation model training
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.createdOn >= :since")
    List<UserInteraction> findAllSince(@Param("since") LocalDateTime since);
    
    // Get search queries by user
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.user.id = :userId AND ui.interactionType = 'SEARCH' ORDER BY ui.createdOn DESC")
    List<UserInteraction> findSearchesByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Check if user has interacted with course recently (for deduplication)
    @Query("SELECT COUNT(ui) > 0 FROM UserInteraction ui WHERE ui.user.id = :userId AND ui.course.id = :courseId AND ui.interactionType = :type AND ui.createdOn >= :since")
    boolean hasRecentInteraction(@Param("userId") Long userId, @Param("courseId") Long courseId, 
                                  @Param("type") InteractionType type, @Param("since") LocalDateTime since);
    
    // Aggregation for recommendation: count interactions per course for a user
    @Query("SELECT ui.course.id, COUNT(ui) FROM UserInteraction ui WHERE ui.user.id = :userId AND ui.course IS NOT NULL GROUP BY ui.course.id")
    List<Object[]> countInteractionsPerCourseByUserId(@Param("userId") Long userId);
    
    // Get most viewed courses (for popularity-based recommendations)
    @Query("SELECT ui.course.id, COUNT(ui) as cnt FROM UserInteraction ui WHERE ui.interactionType = :type AND ui.course IS NOT NULL GROUP BY ui.course.id ORDER BY cnt DESC")
    List<Object[]> findMostPopularCoursesByType(@Param("type") InteractionType type, Pageable pageable);
}
