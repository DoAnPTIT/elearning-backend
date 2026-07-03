package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExamSubmitResponseDto {
    private Long submissionId;
    private Integer score;
    private Integer maxScore;
    private Integer attemptsRemaining;
    private LocalDateTime cooldownUntil;
    private List<QuestionResultDto> questionResults; // Chi tiết đáp án cho từng câu hỏi
}


