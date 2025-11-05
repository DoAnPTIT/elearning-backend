package com.doanptit.elearing_backend_service.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAnswerRequestDto {
    @NotBlank
    private String content;
    private Boolean isCorrect;
}
