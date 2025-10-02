package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
