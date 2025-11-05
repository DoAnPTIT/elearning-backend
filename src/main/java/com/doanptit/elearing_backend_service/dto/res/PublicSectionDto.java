package com.doanptit.elearing_backend_service.dto.res;

import lombok.Data;
import java.util.List;

@Data
public class PublicSectionDto {
    private Long id;
    private String title;
    private Integer order;
    private List<PublicLessonDto> lessons;
}
