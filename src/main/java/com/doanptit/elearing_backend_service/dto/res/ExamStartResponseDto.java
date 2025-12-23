package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExamStartResponseDto {
    private Long examId;
    private String title;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private Integer attemptsRemaining;
    private LocalDateTime cooldownUntil;
    private LocalDateTime startedAt;
    private List<QuestionForStudentDto> questions;

    @Data
    @Builder
    public static class QuestionForStudentDto {
        private Long id;
        private String content;
        private Integer point;
        private List<AnswerForStudentDto> answers;
    }

    @Data
    @Builder
    public static class AnswerForStudentDto {
        private Long id;
        private String content;
    }
}


