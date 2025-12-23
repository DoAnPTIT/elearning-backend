package com.doanptit.elearing_backend_service.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitRequestDto {
    private List<AnswerSelectionDto> answers;

    @Data
    public static class AnswerSelectionDto {
        private Long questionId;
        private List<Long> answerIds;
    }
}


