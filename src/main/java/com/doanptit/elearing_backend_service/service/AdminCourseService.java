package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.AdminUpdateCourseStatusDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseStatus;

public interface AdminCourseService {
    PagedResponse<AdminCourseListDto> getAllCourses(int page, int size, CourseStatus status, String... sort);
    AdminCourseDetailDto getCourseDetails(Long courseId);
    AdminCourseDetailDto updateCourseStatus(Long courseId, AdminUpdateCourseStatusDto request);
}
