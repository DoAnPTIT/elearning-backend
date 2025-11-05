package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.QuestionType;
import lombok.Data;
import java.util.List;

@Data
public class QuestionResponse {
    private Long id;
    private String content;
    private QuestionType questionType;
    private Integer point;
    private List<AnswerResponse> answers;
}