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
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AdminStatisticsService adminStatisticsService;
    private final AdminCourseService adminCourseService;

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody @Valid UserRequestDto request) {
        UserResponseDto newUserDto = userService.createNewUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newUserDto));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Integer id) {
        UserResponseDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String role,
                                                      @RequestParam(defaultValue = "id,asc") String[] sort) {
        return ResponseEntity.ok(ApiResponse.success(userService.findAllUsersForAdmin(page, size, role, sort)));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminStatisticsDto>> getStatisticsQuantityByRole() {
        AdminStatisticsDto stats = adminStatisticsService.getStatisticsQuantityByRole();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping(value = "/users/batch-create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BatchCreationResult>> createUsersFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("role") String role) {

        var result = userService.createUsersFromExcel(file, role);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    // --- API MỚI: LẤY DANH SÁCH KHÓA HỌC (PHÂN TRANG) ---
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<PagedResponse<AdminCourseListDto>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) CourseCategory category, // <-- Đây là code bạn gửi
            @RequestParam(required = false) String title, // <-- Đây là code bạn gửi
            @RequestParam(required = false) String authorName, // <-- Đây là code bạn gửi
            @RequestParam(defaultValue = "id,asc") String... sort
    ) {
        PagedResponse<AdminCourseListDto> courses =
                adminCourseService.getAllCourses(page, size, status, category, title, authorName, sort);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // --- API MỚI: LẤY CHI TIẾT 1 KHÓA HỌC (DATA ĐA DẠNG) ---
    @GetMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> getCourseDetails(@PathVariable Long id) {
        AdminCourseDetailDto courseDetails = adminCourseService.getCourseDetails(id);
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }

    // --- API MỚI: DUYỆT HOẶC TỪ CHỐI KHÓA HỌC ---
    @PatchMapping("/courses/{id}/status")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> updateCourseStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateCourseStatusDto request) {
        AdminCourseDetailDto updatedCourse = adminCourseService.updateCourseStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedCourse));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCourse(@PathVariable Long id) {
        adminCourseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa khóa học thành công."));
    }

}