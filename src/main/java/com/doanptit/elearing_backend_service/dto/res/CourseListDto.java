package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import lombok.Data;

@Data
public class CourseListDto {
    private Long id;
    private String title;
    private String image;
    private CourseCategory category;
    private AuthorDto author;
}