package com.doanptit.elearing_backend_service.event.listener;

import com.doanptit.elearing_backend_service.event.CourseContentUpdatedEvent;
import com.doanptit.elearing_backend_service.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Nhớ thêm annotation này để log
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
@Slf4j
public class AiTrainingListener {

    private final AiService aiService;

    @Async // Chạy trên luồng riêng, không làm đơ giao diện Teacher
    @EventListener
    public void handleCourseUpdate(CourseContentUpdatedEvent event) {
        log.info(">>>> BẮT ĐẦU TỰ ĐỘNG TRAIN AI CHO COURSE ID: {}", event.getCourseId());
        try {
            aiService.ingestCourseData(event.getCourseId());
            log.info(">>>> TRAIN AI HOÀN TẤT CHO COURSE ID: {}", event.getCourseId());
        } catch (Exception e) {
            log.error(">>>> LỖI KHI TRAIN AI: ", e);
        }
    }
}