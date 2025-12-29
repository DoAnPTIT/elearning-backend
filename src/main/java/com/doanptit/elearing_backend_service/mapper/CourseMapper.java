package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.req.CreateCourseRequestDto;
import com.doanptit.elearing_backend_service.dto.res.CreateCourseResponse;
import com.doanptit.elearing_backend_service.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    Course toEntity(CreateCourseRequestDto dto);

    CreateCourseResponse toResponseDto(Course course);
}