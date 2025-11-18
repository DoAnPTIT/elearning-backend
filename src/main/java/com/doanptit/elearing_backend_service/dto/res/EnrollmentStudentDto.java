package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import lombok.Data;
import java.time.LocalDateTime;

// DTO này cho Teacher/Admin xem sinh viên nào đã đăng ký
@Data
public class EnrollmentStudentDto {
    private Long enrollmentId; // ID của lượt đăng ký
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private Long studentId;
    private String studentEmail;
    private String studentFirstName;
    private String studentLastName;
}