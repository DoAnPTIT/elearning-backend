package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.CreateCourseRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateExamRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateLessonRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateSectionRequestDto;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') || hasRole('ADMIN')")
public class TeacherCourseController {

    private final CourseService courseCreationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AdminCourseListDto>>> getAllMyCourses(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String... sort) {

        PagedResponse<AdminCourseListDto> courses = courseCreationService.getAllCoursesForTeacher(
                authentication.getName(), page, size, sort);

        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> getMyCourseForEdit(
            @PathVariable Long courseId,
            Authentication authentication) {
        AdminCourseDetailDto courseDetails = courseCreationService.getCourseForEdit(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }

    // Bước 1: Tạo thông tin chung của khóa học
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequestDto request,
            Authentication authentication) {
        CreateCourseResponse newCourse = courseCreationService.createCourse(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newCourse));
    }

    // Upload ảnh bìa cho khóa học
    @PostMapping("/{courseId}/image")
    public ResponseEntity<ApiResponse<String>> uploadCourseImage(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String imageUrl = courseCreationService.uploadCourseImage(courseId, file, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }

    // Bước 2: Tạo chương mới cho khóa học
    @PostMapping("/{courseId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateSectionRequestDto request,
            Authentication authentication) {
        SectionResponse newSection = courseCreationService.createSection(courseId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newSection));
    }

    // Bước 3: Tạo bài giảng mới trong chương
    @PostMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @PathVariable Long sectionId,
            @Valid @RequestBody CreateLessonRequestDto request,
            Authentication authentication) {
        LessonResponse newLesson = courseCreationService.createLesson(sectionId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newLesson));
    }

    // Bước 4: Upload video cho bài giảng
    @PostMapping("/lessons/upload/{lessonId}/video")
    public ResponseEntity<ApiResponse<String>> uploadLessonVideo(
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String videoUrl = courseCreationService.uploadLessonVideo(lessonId, file, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(videoUrl));
    }

    // Bước 4: Tạo bài kiểm tra trong chương
    @PostMapping("/sections/{sectionId}/exams")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @PathVariable Long sectionId,
            @Valid @RequestBody CreateExamRequestDto request,
            Authentication authentication) {
        ExamResponse newExam = courseCreationService.createExam(sectionId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newExam));
    }

    // Bước 5: Gửi khóa học đi duyệt
    @PatchMapping("/{courseId}/submit-review")
    public ResponseEntity<ApiResponse<String>> submitCourseForReview(
            @PathVariable Long courseId,
            Authentication authentication) {
        courseCreationService.submitCourseForReview(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi khóa học thành công, vui lòng đợi thông báo từ email trong quá trình chúng tôi phê duyệt"));
    }
}
