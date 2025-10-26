package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLessonRequestDto {
    @NotBlank(message = "Tiêu đề bài giảng không được để trống")
    private String title;
    @NotNull(message = "Loại bài giảng không được để trống")
    private LessonType lessonType;
    private String articleContent; // Dùng cho LessonType.ARTICLE
    private Integer lessonOrder;
}