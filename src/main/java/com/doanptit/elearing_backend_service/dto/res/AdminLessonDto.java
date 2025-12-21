package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.LessonType;
import lombok.Data;

@Data
public class AdminLessonDto {
    private Long id;
    private String title;
    private LessonType lessonType;
    private String videoUrl;
    private Long duration;
    private String articleContent;
    private String note;
    private Integer order;
}