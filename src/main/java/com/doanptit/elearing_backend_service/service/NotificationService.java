package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.NotificationResponse;
import com.doanptit.elearing_backend_service.service.even.NotificationEvent;

public interface NotificationService {

    /**
     * Phương thức lắng nghe sự kiện:
     * 1. Nhận data từ Event
     * 2. Lưu xuống Database
     * 3. Bắn WebSocket tới Client
     */
    void handleNotificationEvent(NotificationEvent event);

    /**
     * Lấy danh sách thông báo của user (có phân trang)
     * Sắp xếp: Mới nhất lên đầu
     */
    PagedResponse<NotificationResponse> getMyNotifications(String recipientId, int page, int size);

    /**
     * Đánh dấu 1 thông báo cụ thể là đã đọc
     */
    void markAsRead(Long notificationId, String recipientId);

    /**
     * (Tùy chọn thêm) Đánh dấu TẤT CẢ thông báo là đã đọc
     * Chức năng này giống nút "Mark all as read" trên Facebook
     */
    void markAllAsRead(String recipientId);

    /**
     * Đếm số lượng thông báo chưa đọc
     * Dùng để hiển thị số đỏ trên quả chuông (Badge)
     */
    long countUnread(String recipientId);
}