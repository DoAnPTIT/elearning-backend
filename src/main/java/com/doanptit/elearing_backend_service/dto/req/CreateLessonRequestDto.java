package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.LessonType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLessonRequestDto {
    @NotBlank(message = "Tiêu đề bài giảng không được để trống")
    private String title;

    @JsonProperty("lesson_type")
    @NotNull(message = "Loại bài giảng không được để trống")
    private LessonType lessonType;

    @JsonProperty("article_content")
    private String articleContent; // Dùng cho LessonType.ARTICLE

    @JsonProperty("lesson_order")
    private Integer lessonOrder;

    private Integer duration; // Duration in seconds

    private String note;
}