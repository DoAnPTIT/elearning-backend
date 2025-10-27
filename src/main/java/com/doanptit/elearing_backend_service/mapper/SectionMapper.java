package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.req.CreateSectionRequestDto;
import com.doanptit.elearing_backend_service.dto.res.SectionResponse;
import com.doanptit.elearing_backend_service.model.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "exams", ignore = true)
    @Mapping(target = "order", source = "sectionOrder")
    Section toEntity(CreateSectionRequestDto dto);

//    @Mapping(target = "lessons", ignore = true)
//    @Mapping(target = "exams", ignore = true)
    SectionResponse toSectionResponseDto(Section section);
}
