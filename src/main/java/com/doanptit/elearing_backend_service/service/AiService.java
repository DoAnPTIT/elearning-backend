package com.doanptit.elearing_backend_service.service;

public interface AiService {
    void ingestCourseData(Long courseId);
    void ingestAllActiveCourses();
    String chatWithCourse(String message, Long courseId, String userId); // Chat
}