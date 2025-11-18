package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import com.doanptit.elearing_backend_service.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Kiểm tra 1 user đã đăng ký 1 course hay chưa
    Optional<Enrollment> findByUser_EmailAndCourse_Id(String email, Long courseId);

    // Lấy status đăng ký của user cho 1 course (để kiểm tra quyền xem)
    @Query("SELECT e.status FROM Enrollment e WHERE e.user.email = :email AND e.course.id = :courseId")
    Optional<EnrollmentStatus> findEnrollmentStatus(String email, Long courseId);
}