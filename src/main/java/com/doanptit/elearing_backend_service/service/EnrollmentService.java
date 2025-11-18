package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.EnrollmentStudentDto;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;

public interface EnrollmentService {
    void enrollCourse(Long courseId, String studentEmail);

    void approveEnrollment(Long enrollmentId, String teacherEmail);
    void rejectEnrollment(Long enrollmentId, String teacherEmail);
    void removeEnrollment(Long enrollmentId, String teacherEmail);

    PagedResponse<EnrollmentStudentDto> getEnrollmentsForCourse(
            Long courseId, String teacherEmail, EnrollmentStatus status,
            String studentName, int page, int size, String... sort
    );
}