package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.LessonProgressRequest;
import com.doanptit.elearing_backend_service.dto.res.CourseProgressResponse;

public interface EnrollmentService {
    /**
     * Sinh viên (đã đăng nhập) đăng ký một khóa học
     */
    void enrollCourse(Long courseId, String studentEmail);

    void approveEnrollment(Long enrollmentId, String teacherEmail);
    void rejectEnrollment(Long enrollmentId, String teacherEmail);
    
    /**
     * Lấy danh sách enrollment của một khóa học (cho Teacher)
     */
    PagedResponse<?> getCourseEnrollments(Long courseId, String status, String teacherEmail, int page, int size);

    /**
     * Lấy danh sách enrollment của một khóa học (cho Admin).
     */
    PagedResponse<?> getCourseEnrollmentsForAdmin(Long courseId, String status, int page, int size);
    
    /**
     * Lấy danh sách enrollment của student hiện tại
     */
    PagedResponse<?> getStudentEnrollments(String studentEmail, String status, int page, int size);
    
    /**
     * Lấy enrollment của student cho 1 khóa học cụ thể
     * Return null nếu chưa enroll
     */
    Object getEnrollmentForCourse(Long courseId, String studentEmail);

    CourseProgressResponse updateLessonProgress(Long lessonId, LessonProgressRequest request, String studentEmail);

    CourseProgressResponse getCourseProgress(Long courseId, String studentEmail);
}