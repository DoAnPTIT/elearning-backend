package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.res.PublicCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublicCourseService {
    Page<PublicCourseListDto> getAllPublicCourses(Pageable pageable);
    PublicCourseDetailDto getPublicCourseDetails(Long courseId);
}