package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByCourseIdAndUserId(Long course_id, Long user_id);

    Page<Review> findByCourseId(Long courseId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double calculateAverageRating(Long courseId);

    long countByCourseId(Long courseId);
}
