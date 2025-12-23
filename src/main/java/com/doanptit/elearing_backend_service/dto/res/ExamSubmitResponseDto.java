package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExamSubmitResponseDto {
    private Long submissionId;
    private Integer score;
    private Integer maxScore;
    private Integer attemptsRemaining;
    private LocalDateTime cooldownUntil;
}


