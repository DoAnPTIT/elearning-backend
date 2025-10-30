package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateCourseStatusDto {
    @NotNull(message = "Trạng thái không được để trống")
    private CourseStatus status;
    @JsonProperty("rejection_reason")
    private String rejectionReason;
}