package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourseProgressResponse {
    private Long courseId;
    private Long enrollmentId;
    private int totalLessons;
    private int completedLessons;
    private float progress;
    private List<Long> completedLessonIds;
    private LocalDateTime updatedAt;
}
