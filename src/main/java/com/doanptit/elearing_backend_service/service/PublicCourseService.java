package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseCategory;

public interface PublicCourseService {
    PagedResponse<PublicCourseListDto> getAllPublicCourses(
            int page, int size, CourseCategory category,
            String title, String authorName, String... sort
    );

    PublicCourseDetailDto getPublicCourseDetails(Long courseId);

    PagedResponse<PublicCourseListDto> searchCourses(
            String query, int page, int size, String... sort
    );
}