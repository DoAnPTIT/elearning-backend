package com.doanptit.elearing_backend_service.enums;

public enum CourseStatus {
    DRAFT,              // 0: Giảng viên đang soạn thảo, chưa gửi duyệt
    PENDING_APPROVAL,   // 1: Đã gửi chờ admin duyệt
    ACTIVE,             // 2: Đã được duyệt và đang hoạt động
    HIDDEN,             // 3: Giảng viên tạm ẩn khóa học
    REJECTED,           // 4: Bị từ chối
    ARCHIVED            // 5: Đã lưu trữ
}
