package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PublicCourseMapper {
    PublicCourseListDto toCourseListDto(Course course);
    PublicCourseDetailDto toCourseDetailDto(Course course);
    PublicSectionDto toSectionDto(Section section);
    PublicLessonDto toLessonDto(Lesson lesson);
    PublicAuthorDto toAuthorDto(User author);
}