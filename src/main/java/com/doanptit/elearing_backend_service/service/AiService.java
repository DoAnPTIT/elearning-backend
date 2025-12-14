package com.doanptit.elearing_backend_service.service;

public interface AiService {
    void ingestCourseData(Long courseId); // Nạp dữ liệu
    String chatWithCourse(String message, Long courseId, String userId); // Chat
}