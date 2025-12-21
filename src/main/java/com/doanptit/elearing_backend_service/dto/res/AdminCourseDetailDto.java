package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import lombok.Data;
import java.util.List;

@Data
public class AdminCourseDetailDto {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private CourseStatus status;
    private String image;
    private String objectives;
    private String targetAudience;
    private CourseCategory category;
    private AdminAuthorDto author;
    private List<AdminSectionDto> sections;
    private List<EnrollmentStudentDto> enrollments;
    private Long totalDuration; // Total duration in minutes
    private Integer totalLessons; // Total number of lessons
}
