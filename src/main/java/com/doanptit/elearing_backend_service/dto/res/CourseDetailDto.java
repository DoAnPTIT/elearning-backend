package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import lombok.Data;
import java.util.List;

@Data
public class CourseDetailDto {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private String image;
    private String objectives;
    private String targetAudience;
    private CourseCategory category;
    private AuthorDto author;
    private List<SectionDto> sections;
    private Long totalDuration; // Total duration in minutes
    private Integer totalLessons; // Total number of lessons
}