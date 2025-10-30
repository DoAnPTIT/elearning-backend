package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.req.AdminUpdateCourseStatusDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCourseService {
    Page<AdminCourseListDto> getAllCourses(Pageable pageable);
    AdminCourseDetailDto getCourseDetails(Long courseId);
    AdminCourseDetailDto updateCourseStatus(Long courseId, AdminUpdateCourseStatusDto request);
}
