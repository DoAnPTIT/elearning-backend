package com.doanptit.elearing_backend_service.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSectionRequestDto {
    @NotBlank(message = "Tiêu đề chương không được để trống")
    private String title;

    @JsonProperty("section_order")
    private Integer sectionOrder;

    private String description;
}
