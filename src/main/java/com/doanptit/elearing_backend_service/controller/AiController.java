package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    // --- API 1: TEACHER DẠY AI (Ingest) ---
    // Gọi API này sau khi Teacher tạo xong khóa học để AI học dữ liệu
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/train/{courseId}")
    public ResponseEntity<ApiResponse<String>> trainAiForCourse(@PathVariable Long courseId) {

        aiService.ingestCourseData(courseId);

        return ResponseEntity.ok(ApiResponse.success("AI đã học dữ liệu khóa học thành công! Dữ liệu đã được lưu vào Vector Store."));
    }

    @PostMapping("/train/all")
    @PreAuthorize("hasRole('ADMIN')") // Bảo mật
    public ResponseEntity<ApiResponse<String>> trainAllCourses() {
        // Lưu ý: Việc này có thể mất thời gian nếu dữ liệu lớn
        // Tốt nhất nên chạy Async, nhưng để đơn giản ta chạy Sync trước
        aiService.ingestAllActiveCourses();
        return ResponseEntity.ok(ApiResponse.success("Đã hoàn tất nạp dữ liệu cho TOÀN BỘ khóa học Active!"));
    }

    // --- API 2: STUDENT HỎI AI (Chat) ---
    // Học viên gọi API này để hỏi đáp về nội dung khóa học
    @PreAuthorize("hasRole('STUDENT') || hasRole('TEACHER') || hasRole('ADMIN')")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chatWithAi(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String message = request.get("message");
        String userId = authentication.getName();

        // 1. Kiểm tra xem Frontend có gửi courseId không?
        Long courseId = null;
        if (request.containsKey("courseId") && request.get("courseId") != null) {
            try {
                courseId = Long.valueOf(request.get("courseId"));
            } catch (NumberFormatException e) {
                // courseId không hợp lệ hoặc null -> coi như là Chat Global
            }
        }

        // 2. Gọi Service
        String answer = aiService.chatWithCourse(message, courseId, userId);

        return ResponseEntity.ok(ApiResponse.success(answer));
    }
}