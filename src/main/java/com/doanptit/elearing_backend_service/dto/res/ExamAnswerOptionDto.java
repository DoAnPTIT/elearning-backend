package com.doanptit.elearing_backend_service.dto.res;

import lombok.Data;

@Data
public class ExamAnswerOptionDto {
    private Long id;
    private String content;
    private Boolean isCorrect;
}
