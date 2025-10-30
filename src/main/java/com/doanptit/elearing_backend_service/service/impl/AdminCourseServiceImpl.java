package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.req.AdminUpdateCourseStatusDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.AdminCourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.AdminCourseMapper;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.service.AdminCourseService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseRepository courseRepository;
    private final AdminCourseMapper adminCourseMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCourseListDto> getAllCourses(Pageable pageable) {
        Page<Course> coursePage = courseRepository.findAll(pageable);
        return coursePage.map(adminCourseMapper::toCourseListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCourseDetailDto getCourseDetails(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return adminCourseMapper.toCourseDetailDto(course);
    }

    @Override
    @Transactional
    public AdminCourseDetailDto updateCourseStatus(Long courseId, AdminUpdateCourseStatusDto request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_REVIEW); // (Tạo lỗi này)
        }

        course.setStatus(request.getStatus());

        if (request.getStatus() == CourseStatus.REJECTED) {
            course.setRejectionReason(request.getRejectionReason());
        } else {
            course.setRejectionReason(null);
        }

        return getCourseDetails(courseId);
    }
}