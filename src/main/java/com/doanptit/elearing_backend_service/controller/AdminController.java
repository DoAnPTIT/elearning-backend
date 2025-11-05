package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.AdminUpdateCourseStatusDto;
import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.service.AdminCourseService;
import com.doanptit.elearing_backend_service.service.AdminStatisticsService;
import com.doanptit.elearing_backend_service.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminStatisticsService adminStatisticsService;
    private final AdminCourseService adminCourseService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody @Valid UserRequestDto request) {
        UserResponseDto newUserDto = userService.createNewUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newUserDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
            @Parameter(description = "ID của người dùng cần xem", required = true) @PathVariable Integer id) {
        UserResponseDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
                                                       @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                                                       @Parameter(description = "Kích thước trang", example = "10") @RequestParam(defaultValue = "10") int size,
                                                       @Parameter(description = "Sắp xếp (ví dụ: firstname,desc)", example = "id,asc") @RequestParam(defaultValue = "id,asc") String[] sort) {
        return ResponseEntity.ok(ApiResponse.success(userService.findAllUsers(page, size, sort)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminStatisticsDto>> getStatisticsQuantityByRole() {
        AdminStatisticsDto stats = adminStatisticsService.getStatisticsQuantityByRole();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/users/batch-create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BatchCreationResult>> createUsersFromExcel(
            @Parameter(description = "File Excel chứa danh sách người dùng", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "Role gán cho tất cả user (STUDENT hoặc TEACHER)", required = true) @RequestParam("role") String role) {

        var result = userService.createUsersFromExcel(file, role);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<PagedResponse<AdminCourseListDto>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) CourseCategory category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String authorName,
            @RequestParam(defaultValue = "id,asc") String... sort
    ) {
        PagedResponse<AdminCourseListDto> courses =
                adminCourseService.getAllCourses(page, size, status, category, title, authorName, sort);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> getCourseDetails(
            @Parameter(description = "ID của khóa học cần xem", required = true) @PathVariable Long id) {
        AdminCourseDetailDto courseDetails = adminCourseService.getCourseDetails(id);
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/courses/{id}/status")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> updateCourseStatus(
            @Parameter(description = "ID của khóa học cần cập nhật", required = true) @PathVariable Long id,
            @Valid @RequestBody AdminUpdateCourseStatusDto request) {
        AdminCourseDetailDto updatedCourse = adminCourseService.updateCourseStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedCourse));
    }

}