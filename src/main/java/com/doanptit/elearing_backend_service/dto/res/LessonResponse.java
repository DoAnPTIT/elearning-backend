package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.LessonType;
import lombok.Data;

@Data
public class LessonResponse {
    private Long id;
    private String title;
    private LessonType lessonType;
    private String articleContent;
    private String videoUrl;
    private Integer order;
}