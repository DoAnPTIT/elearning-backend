package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonNoteResponseDto {
    private Long lessonId;
    private String content;
    private LocalDateTime updatedOn;
}


