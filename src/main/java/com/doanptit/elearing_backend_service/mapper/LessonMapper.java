package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.req.CreateLessonRequestDto;
import com.doanptit.elearing_backend_service.dto.res.LessonResponse;
import com.doanptit.elearing_backend_service.model.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "videoUrl", ignore = true)
    @Mapping(target = "articleContent", ignore = true)
    @Mapping(target = "order", source = "lessonOrder")
    Lesson toEntity(CreateLessonRequestDto dto);

    LessonResponse toResponseDto(Lesson lesson);
}