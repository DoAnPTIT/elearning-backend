package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.NotificationResponse;
import com.doanptit.elearing_backend_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Lấy danh sách thông báo
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var result = notificationService.getMyNotifications(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Đánh dấu đã đọc
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id, Authentication authentication) {
        notificationService.markAsRead(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc"));
    }

    // Đếm số lượng chưa đọc (để hiển thị số trên quả chuông)
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnread(Authentication authentication) {
        long count = notificationService.countUnread(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}