package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.res.AdminStatisticsDto;
import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    public AdminStatisticsDto getStatisticsQuantityByRole() {
        int totalStudents = userRepository.countByRole(Role.STUDENT);
        int totalTeachers = userRepository.countByRole(Role.TEACHER);
        long totalCourses = courseRepository.count();

        return AdminStatisticsDto.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalCourses((int) totalCourses)
                .build();
    }
}
