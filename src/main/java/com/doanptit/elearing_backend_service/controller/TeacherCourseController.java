package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.*;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.service.CourseService;
import com.doanptit.elearing_backend_service.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') || hasRole('ADMIN')")
public class TeacherCourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AdminCourseListDto>>> getAllMyCourses(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            // Thêm các tham số lọc
            @RequestParam(required = false) String title,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) CourseCategory category,
            //
            @RequestParam(defaultValue = "id,asc") String... sort) {

        PagedResponse<AdminCourseListDto> courses = courseService.getAllCoursesForTeacher(
                authentication.getName(), page, size, title, status, category, sort);

        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> getMyCourseForEdit(
            @PathVariable Long courseId,
            Authentication authentication) {
        AdminCourseDetailDto courseDetails = courseService.getCourseForEdit(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }

    // Bước 1: Tạo thông tin chung của khóa học
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequestDto request,
            Authentication authentication) {
        CreateCourseResponse newCourse = courseService.createCourse(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newCourse));
    }

    // Upload ảnh bìa cho khóa học
    @PostMapping(value = "/{courseId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadCourseImage(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String imageUrl = courseService.uploadCourseImage(courseId, file, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }

    // Bước 2: Tạo chương mới cho khóa học
    @PostMapping("/{courseId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateSectionRequestDto request,
            Authentication authentication) {
        SectionResponse newSection = courseService.createSection(courseId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newSection));
    }

    // Cập nhật chương
    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable Long sectionId,
            @RequestBody UpdateSectionRequestDto request,
            Authentication authentication) {
        SectionResponse updated = courseService.updateSection(sectionId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // Xóa chương
    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<ApiResponse<String>> deleteSection(
            @PathVariable Long sectionId,
            Authentication authentication) {
        courseService.deleteSection(sectionId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa chương thành công"));
    }

    // Bước 3: Tạo bài giảng mới trong chương
    @PostMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @PathVariable Long sectionId,
            @Valid @RequestBody CreateLessonRequestDto request,
            Authentication authentication) {
        LessonResponse newLesson = courseService.createLesson(sectionId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newLesson));
    }

    // Cập nhật bài học
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable Long lessonId,
            @RequestBody UpdateLessonRequestDto request,
            Authentication authentication) {
        LessonResponse updated = courseService.updateLesson(lessonId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // Xóa bài học
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<String>> deleteLesson(
            @PathVariable Long lessonId,
            Authentication authentication) {
        courseService.deleteLesson(lessonId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa bài học thành công"));
    }

    // Bước 4: Upload video cho bài giảng
    @PostMapping(value = "/lessons/upload/{lessonId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadLessonVideo(
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String videoUrl = courseService.uploadLessonVideo(lessonId, file, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(videoUrl));
    }

    // Upload tài liệu (PDF) cho bài giảng ARTICLE
    @PostMapping(value = "/lessons/upload/{lessonId}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadLessonDocument(
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String documentUrl = courseService.uploadLessonDocument(lessonId, file, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(documentUrl));
    }

    // Bước 4: Tạo bài kiểm tra trong chương
    @PostMapping("/sections/{sectionId}/exams")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @PathVariable Long sectionId,
            @Valid @RequestBody CreateExamRequestDto request,
            Authentication authentication) {
        ExamResponse newExam = courseService.createExam(sectionId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newExam));
    }

    // Bước 5: Gửi khóa học đi duyệt
    @PatchMapping("/{courseId}/submit-review")
    public ResponseEntity<ApiResponse<String>> submitCourseForReview(
            @PathVariable Long courseId,
            Authentication authentication) {
        courseService.submitCourseForReview(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi khóa học thành công, vui lòng đợi thông báo từ email trong quá trình chúng tôi phê duyệt"));
    }

    @PatchMapping("/{courseId}/hide")
    public ResponseEntity<ApiResponse<String>> hideCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        courseService.hideCourse(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Khóa học đã được chuyển sang trạng thái ẩn."));
    }

    // --- API : LẤY DANH SÁCH ENROLLMENT CỦA KHÓA HỌC ---
    // (Chỉ Teacher/Admin mới có quyền)
    @GetMapping("/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<PagedResponse<?>>> getCourseEnrollments(
            @PathVariable Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        PagedResponse<?> enrollments = enrollmentService.getCourseEnrollments(
                courseId, status, authentication.getName(), page, size);
        
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    // --- API : PHÊ DUYỆT ĐĂNG KÝ ---
    // (Chỉ Teacher/Admin mới có quyền)
    @PatchMapping("/enrollments/{enrollmentId}/approve")
    public ResponseEntity<ApiResponse<String>> approveEnrollment(
            @PathVariable Long enrollmentId,
            Authentication authentication) {

        enrollmentService.approveEnrollment(enrollmentId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Phê duyệt học viên thành công."));
    }

    // --- API : TỪ CHỐI ĐĂNG KÝ ---
    // (Chỉ Teacher/Admin mới có quyền)
    @PatchMapping("/enrollments/{enrollmentId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectEnrollment(
            @PathVariable Long enrollmentId,
            Authentication authentication) {

        enrollmentService.rejectEnrollment(enrollmentId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối học viên."));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateCourseRequestDto request,
            Authentication authentication) {
        log.info("api update course");
        AdminCourseDetailDto updatedCourse = courseService.updateCourse(courseId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updatedCourse));
    }
}