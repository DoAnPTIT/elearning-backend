package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseDetailDto;
import com.doanptit.elearing_backend_service.dto.res.PublicCourseListDto;
import com.doanptit.elearing_backend_service.service.PublicCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "3. Public Courses", description = "Các API public cho phép mọi người xem khóa học (Không cần xác thực)")
public class PublicCourseController {

    private final PublicCourseService publicCourseService;

    @Operation(summary = "[PUBLIC] Lấy danh sách khóa học (Trang chủ)",
            description = "Lấy danh sách các khóa học đã được 'APPROVED' (phê duyệt), có phân trang. (API này nên được sửa để trả về PagedResponse)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công, trả về danh sách khóa học")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PublicCourseListDto>>> getAllPublicCourses(
            @PageableDefault(sort = "id") Pageable pageable) {
        Page<PublicCourseListDto> courses = publicCourseService.getAllPublicCourses(pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @Operation(summary = "[PUBLIC] Lấy chi tiết 1 khóa học (để xem)",
            description = "Lấy thông tin chi tiết của 1 khóa học đã được 'APPROVED'. Chỉ trả về thông tin công khai (tiêu đề bài giảng), không bao gồm nội dung.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công, trả về chi tiết khóa học"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học (hoặc khóa học chưa được duyệt)")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicCourseDetailDto>> getPublicCourseDetails(
            @Parameter(description = "ID của khóa học cần xem", required = true) @PathVariable Long id) {
        PublicCourseDetailDto courseDetails = publicCourseService.getPublicCourseDetails(id);
        return ResponseEntity.ok(ApiResponse.success(courseDetails));
    }
}