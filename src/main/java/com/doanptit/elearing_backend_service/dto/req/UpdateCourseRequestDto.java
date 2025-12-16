package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import lombok.Data;

@Data
public class UpdateCourseRequestDto {
    private String title;
    private String description;
    private String objectives;
    private String targetAudience;
    private CourseCategory category;
}