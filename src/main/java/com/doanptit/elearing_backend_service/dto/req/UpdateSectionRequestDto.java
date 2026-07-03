package com.doanptit.elearing_backend_service.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateSectionRequestDto {
    private String title;

    @JsonProperty("section_order")
    private Integer sectionOrder;

    private String description;
}

