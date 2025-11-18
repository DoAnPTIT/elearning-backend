package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import lombok.Data;
import java.util.List;

@Data
public class CourseDetailDto {
    private Long id;
    private String title;
    private String description;
    private String image;
    private String objectives;
    private String targetAudience;
    private CourseCategory category;
    private AuthorDto author;
    private List<SectionDto> sections;
}