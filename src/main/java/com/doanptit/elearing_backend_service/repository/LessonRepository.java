package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
	long countBySection_Course_Id(Long courseId);
}
