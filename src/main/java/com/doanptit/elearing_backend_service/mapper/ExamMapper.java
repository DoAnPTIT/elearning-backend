package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.req.CreateExamRequestDto;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.model.Answer;
import com.doanptit.elearing_backend_service.model.Exam;
import com.doanptit.elearing_backend_service.model.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamMapper {

    // --- DTO -> Entity (chỉ map các trường cơ bản) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "questions", ignore = true) // Sẽ map thủ công trong service
    @Mapping(target = "submissions", ignore = true)
    Exam toEntity(CreateExamRequestDto dto);

    // --- Entity -> DTO (map lồng nhau) ---
    ExamResponse toResponseDto(Exam exam);
    QuestionResponse toResponseDto(Question question);
    AnswerResponse toResponseDto(Answer answer);
}