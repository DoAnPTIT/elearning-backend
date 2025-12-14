package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.LessonProgressRequest;
import com.doanptit.elearing_backend_service.dto.res.CourseProgressResponse;
import com.doanptit.elearing_backend_service.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final EnrollmentService enrollmentService;

    /**
     * Lấy danh sách enrollment của student hiện tại
     * Filter theo status: PENDING, APPROVED, REJECTED
     */
    @GetMapping("/enrollments")
    public ResponseEntity<ApiResponse<PagedResponse<?>>> getMyEnrollments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        PagedResponse<?> enrollments = enrollmentService.getStudentEnrollments(
                authentication.getName(), status, page, size);

        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

        @PatchMapping("/lessons/{lessonId}/progress")
        public ResponseEntity<ApiResponse<CourseProgressResponse>> updateLessonProgress(
            @PathVariable Long lessonId,
            @RequestBody(required = false) LessonProgressRequest request,
            Authentication authentication) {

        CourseProgressResponse response = enrollmentService.updateLessonProgress(
            lessonId,
            request,
            authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(response));
        }

        @GetMapping("/courses/{courseId}/progress")
        public ResponseEntity<ApiResponse<CourseProgressResponse>> getCourseProgress(
            @PathVariable Long courseId,
            Authentication authentication) {

        CourseProgressResponse response = enrollmentService.getCourseProgress(
            courseId,
            authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(response));
        }
}
