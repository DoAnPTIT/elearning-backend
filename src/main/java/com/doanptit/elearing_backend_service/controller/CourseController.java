package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.CourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.CourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') || hasRole('ADMIN') || hasRole('STUDENT')")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CourseListDto>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CourseCategory category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String authorName,
            @RequestParam(defaultValue = "id,asc") String... sort
    ) {
        PagedResponse<CourseListDto> courses = courseService.getAllPublicCourses(
                page, size, category, title, authorName, sort
        );
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailDto>> getCourseDetails(
            @PathVariable Long id,
            Authentication authentication) {

        CourseDetailDto courseDetails = courseService.getPublicCourseDetails(id, authentication);
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<CourseListDto>>> searchCourses(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn,desc") String... sort
    ) {
        PagedResponse<CourseListDto> courses = courseService.searchCourses(
                q, page, size, sort
        );

        if (courses.getTotalElements() == 0) {
            return ResponseEntity.ok(ApiResponse.success("Không tìm thấy khóa học nào phù hợp.", courses));
        }

        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<CourseCategory[]>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(CourseCategory.values()));
    }
}