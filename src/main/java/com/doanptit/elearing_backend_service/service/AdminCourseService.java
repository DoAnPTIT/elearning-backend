package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.AdminUpdateCourseStatusDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;

public interface AdminCourseService {
    PagedResponse<AdminCourseListDto> getAllCourses(
            int page, int size, CourseStatus status, CourseCategory category,
            String title, String authorName, Boolean hasStudents, EnrollmentStatus enrollmentStatus, String... sort
    );
    AdminCourseDetailDto getCourseDetails(Long courseId);
    AdminCourseDetailDto updateCourseStatus(Long courseId, AdminUpdateCourseStatusDto request);
    void deleteCourse(Long courseId);
}
