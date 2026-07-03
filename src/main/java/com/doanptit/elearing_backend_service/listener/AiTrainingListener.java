package com.doanptit.elearing_backend_service.listener;

import com.doanptit.elearing_backend_service.configuration.AsyncConfig;
import com.doanptit.elearing_backend_service.event.CourseContentUpdatedEvent;
import com.doanptit.elearing_backend_service.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiTrainingListener {

    private final AiService aiService;

    @Async(AsyncConfig.AI_TRAINING_EXECUTOR)
    @EventListener
    public void handleCourseContentUpdated(CourseContentUpdatedEvent event) {
        Long courseId = event.getCourseId();
        log.info("Course content updated event received for course ID: {}. Triggering AI training...", courseId);
        
        try {
            // Train AI for all courses to ensure consistency
            // This is async so it won't block the main request
            aiService.ingestAllActiveCourses();
            log.info("AI training completed successfully after course content update for course ID: {}", courseId);
        } catch (Exception e) {
            log.error("Error during AI training after course content update for course ID: {}", courseId, e);
        }
    }
}


