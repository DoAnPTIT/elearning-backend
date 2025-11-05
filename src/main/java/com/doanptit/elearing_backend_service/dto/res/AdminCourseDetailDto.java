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
    private CourseStatus status;
    private String image;
    private String objectives;
    private String targetAudience;
    private CourseCategory category;
    private AdminAuthorDto author;
    private List<AdminSectionDto> sections;
}
