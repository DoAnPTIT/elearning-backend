package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.ExamType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateExamRequestDto {
    @NotBlank
    private String title;
    private String description;
    @JsonProperty("exam_type")
    @NotNull
    private ExamType examType;
    private List<CreateQuestionRequestDto> questions;
}