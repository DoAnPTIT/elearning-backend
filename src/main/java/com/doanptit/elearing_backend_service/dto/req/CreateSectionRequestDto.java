package com.doanptit.elearing_backend_service.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSectionRequestDto {
    @NotBlank(message = "Tiêu đề chương không được để trống")
    private String title;
    private Integer sectionOrder;
}
