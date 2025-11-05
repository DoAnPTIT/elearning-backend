package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateQuestionRequestDto {
    @NotBlank
    private String content;

    @JsonProperty("question_type")
    @NotNull
    private QuestionType questionType;

    private Integer point;
    private List<CreateAnswerRequestDto> answers;
}