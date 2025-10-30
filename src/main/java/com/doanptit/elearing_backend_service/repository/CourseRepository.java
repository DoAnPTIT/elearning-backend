package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {


    // (Dùng cho Teacher GET danh sách)
    Page<Course> findByAuthor_Email(String teacherEmail, Pageable pageable);

    // (Dùng cho Public GET danh sách)
    @Query("SELECT c FROM Course c WHERE c.status = 'APPROVED'")
    Page<Course> findAllApproved(Pageable pageable);

    // (Dùng cho Public GET chi tiết)
    @Query("SELECT c FROM Course c " +
            "LEFT JOIN FETCH c.author " +
            "LEFT JOIN FETCH c.sections s " +
            "LEFT JOIN FETCH s.lessons l " +
            "WHERE c.id = :courseId AND c.status = 'APPROVED'")
    Optional<Course> findFullPublicCourseDetailsById(@Param("courseId") Long courseId);
}
