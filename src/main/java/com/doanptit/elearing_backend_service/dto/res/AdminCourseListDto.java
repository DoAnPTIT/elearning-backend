package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import lombok.Data;

@Data
public class AdminCourseListDto {
    private Long id;
    private String title;
    private CourseStatus status;
    private CourseCategory category;
    private AdminAuthorDto author;
    private Integer sectionCount;
}