package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    // (Dùng cho Teacher GET danh sách)
    Page<Course> findByAuthor_Email(String teacherEmail, Pageable pageable);

    // (Dùng cho Public GET danh sách)
    @Query("SELECT c FROM Course c WHERE c.status = 'APPROVED'")
    Page<Course> findAllApproved(Pageable pageable);

}
