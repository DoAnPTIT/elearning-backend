package com.doanptit.elearing_backend_service.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class ExamQuestionDto {
    private Long id;
    private String content;
    private Integer point;
    private List<ExamAnswerOptionDto> answers;
}
