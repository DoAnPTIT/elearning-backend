package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.LessonType;
import lombok.Data;

import java.util.List;

@Data
public class LessonDto {
    private Long id;
    private String title;
    private Long duration;
    private Integer order;
    private LessonType lessonType;
    private String videoUrl;
    private String articleContent;
    private List<ExamQuestionDto> examQuestions;
}