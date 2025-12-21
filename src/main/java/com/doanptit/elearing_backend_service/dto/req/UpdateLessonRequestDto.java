package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.LessonType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateLessonRequestDto {
    private String title;

    @JsonProperty("lesson_type")
    private LessonType lessonType;

    @JsonProperty("article_content")
    private String articleContent;

    @JsonProperty("lesson_order")
    private Integer lessonOrder;

    private Integer duration;

    private String note;
}

