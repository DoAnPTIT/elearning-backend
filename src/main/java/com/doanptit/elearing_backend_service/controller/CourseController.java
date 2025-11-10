package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseListDto;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.service.PublicCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final PublicCourseService publicCourseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PublicCourseListDto>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CourseCategory category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String authorName,
            @RequestParam(defaultValue = "id,asc") String... sort
    ) {
        PagedResponse<PublicCourseListDto> courses = publicCourseService.getAllPublicCourses(
                page, size, category, title, authorName, sort
        );
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicCourseDetailDto>> getCourseDetails(@PathVariable Long id) {
        PublicCourseDetailDto courseDetails = publicCourseService.getPublicCourseDetails(id);
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<PublicCourseListDto>>> searchCourses(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn,desc") String... sort
    ) {
        PagedResponse<PublicCourseListDto> courses = publicCourseService.searchCourses(
                q, page, size, sort
        );

        if (courses.getTotalElements() == 0) {
            return ResponseEntity.ok(ApiResponse.success("Không tìm thấy khóa học nào phù hợp.", courses));
        }

        return ResponseEntity.ok(ApiResponse.success(courses));
    }
}