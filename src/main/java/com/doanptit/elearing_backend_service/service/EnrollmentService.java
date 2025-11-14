package com.doanptit.elearing_backend_service.service;

public interface EnrollmentService {
    /**
     * Sinh viên (đã đăng nhập) đăng ký một khóa học
     */
    void enrollCourse(Long courseId, String studentEmail);

    void approveEnrollment(Long enrollmentId, String teacherEmail);
    void rejectEnrollment(Long enrollmentId, String teacherEmail);
}