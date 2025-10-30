package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.res.PublicCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseListDto;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.PublicCourseMapper;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.service.PublicCourseService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicCourseServiceImpl implements PublicCourseService {

    private final CourseRepository courseRepository;
    private final PublicCourseMapper publicCourseMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PublicCourseListDto> getAllPublicCourses(Pageable pageable) {
        Page<Course> coursePage = courseRepository.findAllApproved(pageable);
        return coursePage.map(publicCourseMapper::toCourseListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicCourseDetailDto getPublicCourseDetails(Long courseId) {
        Course course = courseRepository.findFullPublicCourseDetailsById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return publicCourseMapper.toCourseDetailDto(course);
    }
}