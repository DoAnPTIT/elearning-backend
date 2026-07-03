package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    // (Dùng cho Teacher GET danh sách)
    Page<Course> findByAuthor_Email(String teacherEmail, Pageable pageable);

    // (Dùng cho Public GET danh sách)
    @Query("SELECT c FROM Course c WHERE c.status = 'ACTIVE'")
    Page<Course> findAllApproved(Pageable pageable);

    List<Course> findByStatus(CourseStatus status);

    boolean existsByTitleIgnoreCaseAndDeletedOnIsNull(String title);
}
