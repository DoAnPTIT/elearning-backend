package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.LessonType;
import lombok.Data;

@Data
public class LessonDto {
    private Long id;
    private String title;
    private Long duration;
    private Integer order;
    private LessonType lessonType;
}