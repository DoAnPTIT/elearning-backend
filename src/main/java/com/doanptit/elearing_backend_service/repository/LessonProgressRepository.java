package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByEnrollment_IdAndLesson_Id(Long enrollmentId, Long lessonId);
}

