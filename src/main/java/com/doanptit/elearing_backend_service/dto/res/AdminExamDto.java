package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.ExamType;
import lombok.Data;
import java.util.List;

@Data
public class AdminExamDto {
    private Long id;
    private String title;
    private String description;
    private ExamType examType;
    private List<AdminQuestionDto> questions;
}