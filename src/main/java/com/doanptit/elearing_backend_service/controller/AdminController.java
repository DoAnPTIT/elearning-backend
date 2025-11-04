package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.AdminUpdateCourseStatusDto;
import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.service.AdminCourseService;
import com.doanptit.elearing_backend_service.service.AdminStatisticsService;
import com.doanptit.elearing_backend_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "1. Admin Management", description = "Các API cho phép Admin quản lý toàn bộ hệ thống")
public class AdminController {

    private final UserService userService;
    private final AdminStatisticsService adminStatisticsService;
    private final AdminCourseService adminCourseService;

    @Operation(summary = "[ADMIN] Tạo người dùng mới", description = "Tạo một người dùng mới (STUDENT hoặc TEACHER). Yêu cầu quyền ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo người dùng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (ví dụ: email trùng)")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody @Valid UserRequestDto request) {
        UserResponseDto newUserDto = userService.createNewUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newUserDto));
    }

    @Operation(summary = "[ADMIN] Lấy thông tin người dùng bằng ID", description = "Yêu cầu quyền ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
            @Parameter(description = "ID của người dùng cần xem", required = true) @PathVariable Integer id) {
        UserResponseDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    @Operation(summary = "[ADMIN] Lấy danh sách người dùng (Phân trang, Sắp xếp)", description = "Lấy danh sách tất cả người dùng, hỗ trợ phân trang và sắp xếp. Yêu cầu quyền ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp (ví dụ: firstname,desc)", example = "id,asc") @RequestParam(defaultValue = "id,asc") String[] sort) {
        return ResponseEntity.ok(ApiResponse.success(userService.findAllUsers(page, size, sort)));
    }

    @Operation(summary = "[ADMIN] Lấy thống kê hệ thống", description = "Lấy số lượng tổng user, teacher, và student. Yêu cầu quyền ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminStatisticsDto>> getStatisticsQuantityByRole() {
        AdminStatisticsDto stats = adminStatisticsService.getStatisticsQuantityByRole();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "[ADMIN] Tạo người dùng hàng loạt từ Excel", description = "Tải lên file Excel (.xlsx) để tạo hàng loạt người dùng. Yêu cầu quyền ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/batch-create")
    public ResponseEntity<ApiResponse<BatchCreationResult>> createUsersFromExcel(
            @Parameter(description = "File Excel chứa danh sách người dùng", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "Role gán cho tất cả user (STUDENT hoặc TEACHER)", required = true) @RequestParam("role") String role) {

        var result = userService.createUsersFromExcel(file, role);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    // --- API MỚI: LẤY DANH SÁCH KHÓA HỌC (PHÂN TRANG) ---
    @Operation(summary = "[ADMIN] Lấy danh sách khóa học (Phân trang, Lọc, Sắp xếp)",
            description = "Lấy danh sách tất cả khóa học, hỗ trợ lọc theo 'status' và sắp xếp. Yêu cầu quyền ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền (không phải Admin)")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<PagedResponse<AdminCourseListDto>>> getAllCourses(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Lọc theo trạng thái (DRAFT, PENDING_APPROVAL, APPROVED, REJECTED)", example = "DRAFT") @RequestParam(required = false) CourseStatus status,
            @Parameter(description = "Sắp xếp (ví dụ: title,desc)", example = "id,asc") @RequestParam(defaultValue = "id,asc") String... sort) {

        PagedResponse<AdminCourseListDto> courses = adminCourseService.getAllCourses(page, size, status, sort);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // --- API MỚI: LẤY CHI TIẾT 1 KHÓA HỌC (DATA ĐA DẠNG) ---
    @Operation(summary = "[ADMIN] Lấy chi tiết 1 khóa học",
            description = "Lấy toàn bộ cây thông tin lồng nhau của một khóa học. Yêu cầu quyền ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> getCourseDetails(
            @Parameter(description = "ID của khóa học cần xem", required = true) @PathVariable Long id) {
        AdminCourseDetailDto courseDetails = adminCourseService.getCourseDetails(id);
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }

    // --- API MỚI: DUYỆT HOẶC TỪ CHỐI KHÓA HỌC ---
    @Operation(summary = "[ADMIN] Phê duyệt hoặc Từ chối khóa học",
            description = "Thay đổi trạng thái của một khóa học (thường là từ PENDING_APPROVAL sang APPROVED hoặc REJECTED). Yêu cầu quyền ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công, trả về khóa học đã cập nhật"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ (ví dụ: khóa học đang ở DRAFT)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/courses/{id}/status")
    public ResponseEntity<ApiResponse<AdminCourseDetailDto>> updateCourseStatus(
            @Parameter(description = "ID của khóa học cần cập nhật", required = true) @PathVariable Long id,
            @Valid @RequestBody AdminUpdateCourseStatusDto request) {
        AdminCourseDetailDto updatedCourse = adminCourseService.updateCourseStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedCourse));
    }
}