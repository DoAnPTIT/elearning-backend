package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PublicCourseMapper {
    CourseListDto toCourseListDto(Course course);
    CourseDetailDto toCourseDetailDto(Course course);
    SectionDto toSectionDto(Section section);
    LessonDto toLessonDto(Lesson lesson);
    AuthorDto toAuthorDto(User author);
}