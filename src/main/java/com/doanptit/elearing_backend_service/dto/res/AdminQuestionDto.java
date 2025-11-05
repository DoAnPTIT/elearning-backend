package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.QuestionType;
import lombok.Data;
import java.util.List;

@Data
public class AdminQuestionDto {
    private Long id;
    private QuestionType questionType;
    private String content;
    private Integer point;
    private List<AdminAnswerDto> answers;
}