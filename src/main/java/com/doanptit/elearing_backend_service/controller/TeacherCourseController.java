package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.CreateCourseRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateExamRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateLessonRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateSectionRequestDto;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') || hasRole('ADMIN')")
@Tag(name = "4. Teacher - Course Creation", description = "Các API cho phép Teacher tạo và quản lý khóa học (lưu nháp, gửi duyệt)")
public class TeacherCourseController {

    private final CourseService courseCreationService;

    @Operation(summary = "[TEACHER] Lấy danh sách khóa học của tôi",
            description = "Lấy danh sách (phân trang) các khóa học do chính Teacher đang đăng nhập sở hữu. Yêu cầu quyền TEACHER/ADMIN.")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AdminCourseListDto>>> getAllMyCourses(
            Authentication authentication,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp (ví dụ: title,desc)", example = "id,asc") @RequestParam(defaultValue = "id,asc") String... sort) {

        PagedResponse<AdminCourseListDto> courses = courseCreationService.getAllCoursesForTeacher(
                authentication.getName(), page, size, sort);

        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @Operation(summary = "[TEACHER] Lấy chi tiết khóa học để sửa (Load nháp)",
            description = "Lấy toàn bộ cây thông tin lồng nhau (Section, Lesson, Exam...) của một khóa học để Teacher tiếp tục chỉnh sửa. Yêu cầu quyền TEACHER/ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền (không phải chủ khóa học)")
    })
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> getMyCourseForEdit(
            @Parameter(description = "ID của khóa học cần sửa", required = true) @PathVariable Long courseId,
            Authentication authentication) {
        AdminCourseDetailDto courseDetails = courseCreationService.getCourseForEdit(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }

    // Bước 1: Tạo thông tin chung của khóa học
    @Operation(summary = "[TEACHER] Bước 1: Tạo thông tin khóa học (Draft)",
            description = "Tạo một khung khóa học mới với trạng thái DRAFT. Yêu cầu quyền TEACHER/ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequestDto request,
            Authentication authentication) {
        CreateCourseResponse newCourse = courseCreationService.createCourse(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newCourse));
    }

    // Upload ảnh bìa cho khóa học
    @Operation(summary = "[TEACHER] Tải ảnh bìa khóa học", description = "Yêu cầu quyền TEACHER/ADMIN.")
    @PostMapping(value = "/{courseId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadCourseImage(
            @Parameter(description = "ID của khóa học", required = true) @PathVariable Long courseId,
            @Parameter(description = "File ảnh bìa", required = true) @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String imageUrl = courseCreationService.uploadCourseImage(courseId, file, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }

    // Bước 2: Tạo chương mới cho khóa học
    @Operation(summary = "[TEACHER] Bước 2: Tạo chương mới", description = "Thêm một chương mới vào khóa học nháp. Yêu cầu quyền TEACHER/ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công")
    })
    @PostMapping("/{courseId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(
            @Parameter(description = "ID của khóa học", required = true) @PathVariable Long courseId,
            @Valid @RequestBody CreateSectionRequestDto request,
            Authentication authentication) {
        SectionResponse newSection = courseCreationService.createSection(courseId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newSection));
    }

    // Bước 3: Tạo bài giảng mới trong chương
    @Operation(summary = "[TEACHER] Bước 3: Tạo bài giảng mới", description = "Thêm một bài giảng (Video/Article) vào một chương. Yêu cầu quyền TEACHER/ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công")
    })
    @PostMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @Parameter(description = "ID của chương", required = true) @PathVariable Long sectionId,
            @Valid @RequestBody CreateLessonRequestDto request,
            Authentication authentication) {
        LessonResponse newLesson = courseCreationService.createLesson(sectionId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newLesson));
    }

    // Bước 4: Upload video cho bài giảng
    @Operation(summary = "[TEACHER] Tải video bài giảng", description = "Tải file video cho một bài giảng (loại VIDEO). Yêu cầu quyền TEACHER/ADMIN.")
    @PostMapping(value = "/lessons/upload/{lessonId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadLessonVideo(
            @Parameter(description = "ID của bài giảng", required = true) @PathVariable Long lessonId,
            @Parameter(description = "File video", required = true) @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String videoUrl = courseCreationService.uploadLessonVideo(lessonId, file, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(videoUrl));
    }

    // Bước 4: Tạo bài kiểm tra trong chương
    @Operation(summary = "[TEACHER] Bước 4: Tạo bài kiểm tra", description = "Thêm một bài kiểm tra (lồng câu hỏi/câu trả lời) vào một chương. Yêu cầu quyền TEACHER/ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công")
    })
    @PostMapping("/sections/{sectionId}/exams")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @Parameter(description = "ID của chương", required = true) @PathVariable Long sectionId,
            @Valid @RequestBody CreateExamRequestDto request,
            Authentication authentication) {
        ExamResponse newExam = courseCreationService.createExam(sectionId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newExam));
    }

    // Bước 5: Gửi khóa học đi duyệt
    @Operation(summary = "[TEACHER] Bước 5: Gửi khóa học đi duyệt",
            description = "Thay đổi trạng thái khóa học từ DRAFT/REJECTED sang PENDING_APPROVAL. Yêu cầu quyền TEACHER/ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gửi duyệt thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Khóa học không đủ điều kiện (ví dụ: thiếu ảnh bìa, thiếu bài giảng)")
    })
    @PatchMapping("/{courseId}/submit-review")
    public ResponseEntity<ApiResponse<String>> submitCourseForReview(
            @Parameter(description = "ID của khóa học", required = true) @PathVariable Long courseId,
            Authentication authentication) {
        courseCreationService.submitCourseForReview(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi khóa học thành công, vui lòng đợi thông báo từ email trong quá trình chúng tôi phê duyệt"));
    }
}