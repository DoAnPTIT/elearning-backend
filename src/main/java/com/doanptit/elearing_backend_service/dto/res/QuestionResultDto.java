package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionResultDto {
    private Long questionId;
    private String questionContent;
    private List<Long> correctAnswerIds;
    private List<Long> userAnswerIds;
    private Boolean isCorrect;
    private Integer point;
    private Integer earnedPoint;
}


