package com.doanptit.elearing_backend_service.dto.res;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminStatisticsDto {
    private int totalStudents;
    private int totalTeachers;
    private int totalCourses;
}
