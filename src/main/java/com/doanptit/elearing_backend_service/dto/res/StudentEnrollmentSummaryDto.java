package com.doanptit.elearing_backend_service.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEnrollmentSummaryDto {
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String courseImage;
    private Float progress;
    private String status;
    private String teacherName;
    private String lastActivity;
}
