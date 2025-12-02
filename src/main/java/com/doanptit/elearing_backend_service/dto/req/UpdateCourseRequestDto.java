package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCourseRequestDto {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    private String description;
    private String objectives;
    private String targetAudience;
    private CourseCategory category;
}