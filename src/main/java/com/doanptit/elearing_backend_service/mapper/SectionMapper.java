package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.res.SectionResponse;
import com.doanptit.elearing_backend_service.model.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionMapper {
//    @Mapping(target = "lessons", ignore = true)
//    @Mapping(target = "exams", ignore = true)
    SectionResponse toSectionResponseDto(Section section);
}
