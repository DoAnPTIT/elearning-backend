package com.doanptit.elearing_backend_service.enums;

public enum CourseStatus {
    DRAFT,          // 0: Giảng viên đang soạn thảo, chưa gửi duyệt
    PENDING_APPROVAL, // 1: Đã gửi chờ admin duyệt
    ACTIVE,         // 2: Đã được duyệt và đang hoạt động
    REJECTED,       // 3: Bị từ chối
    ARCHIVED        // 4: Đã lưu t
}
