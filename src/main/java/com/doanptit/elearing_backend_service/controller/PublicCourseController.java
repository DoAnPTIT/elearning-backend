package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseListDto;
import com.doanptit.elearing_backend_service.service.PublicCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/courses")
@RequiredArgsConstructor
public class PublicCourseController {

    private final PublicCourseService publicCourseService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PublicCourseListDto>>> getAllPublicCourses(
            @PageableDefault(sort = "id") Pageable pageable) {
        Page<PublicCourseListDto> courses = publicCourseService.getAllPublicCourses(pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicCourseDetailDto>> getPublicCourseDetails(@PathVariable Long id) {
        PublicCourseDetailDto courseDetails = publicCourseService.getPublicCourseDetails(id);
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }
}