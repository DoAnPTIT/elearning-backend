package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateCourseResponse {
    private Long id;
    private String title;
    private String objectives;

    private String targetAudience;

    private CourseCategory category;
}
