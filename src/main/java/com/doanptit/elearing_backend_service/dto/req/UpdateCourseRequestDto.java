package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateCourseRequestDto {
    private String title;
    private String description;
    
    @JsonAlias({"short_description"})
    private String shortDescription;

    private String objectives;
    
    @JsonProperty("target_audience")
    private String targetAudience;
    
    private CourseCategory category;
}