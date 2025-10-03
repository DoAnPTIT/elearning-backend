package com.doanptit.elearing_backend_service.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminStatisticsDto {

    @JsonProperty("total-students")
    private int totalStudents;

    @JsonProperty("total-teachers")
    private int totalTeachers;

    @JsonProperty("total-courses")
    private int totalCourses;
}
