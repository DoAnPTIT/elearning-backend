package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCourseRequestDto {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    private String objectives;

    @JsonProperty("target_audience")
    private String targetAudience;

    @NotNull(message = "Thể loại không được để trống")
    private CourseCategory category;
}
