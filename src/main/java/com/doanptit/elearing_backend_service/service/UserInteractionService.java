package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.req.InteractionRequest;
import com.doanptit.elearing_backend_service.enums.InteractionType;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.Lesson;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.model.UserInteraction;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.repository.LessonRepository;
import com.doanptit.elearing_backend_service.repository.UserInteractionRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInteractionService {

    private final UserInteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    private static final int DEDUP_WINDOW_SECONDS = 5; // Prevent duplicate interactions within 5 seconds

    /**
     * Async wrapper - calls the transactional method.
     * Note: @Async and @Transactional don't work well together on the same method
     * because @Async runs on a different thread without transaction context.
     */
    @Async
    public void trackInteractionAsync(String userEmail, InteractionRequest request) {
        trackInteraction(userEmail, request);
    }

    @Transactional
    public void trackInteraction(String userEmail, InteractionRequest request) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Deduplication check for course interactions
            if (request.getCourseId() != null && request.getInteractionType() != InteractionType.SEARCH) {
                LocalDateTime since = LocalDateTime.now().minusSeconds(DEDUP_WINDOW_SECONDS);
                if (interactionRepository.hasRecentInteraction(
                        user.getId(), request.getCourseId(), request.getInteractionType(), since)) {
                    log.debug("Skipping duplicate interaction for user {} on course {}", 
                            user.getId(), request.getCourseId());
                    return;
                }
            }

            UserInteraction interaction = UserInteraction.builder()
                    .user(user)
                    .interactionType(request.getInteractionType())
                    .searchQuery(request.getSearchQuery())
                    .durationSeconds(request.getDurationSeconds())
                    .deviceType(request.getDeviceType())
                    .sessionId(request.getSessionId())
                    .build();

            // Set course if provided
            if (request.getCourseId() != null) {
                Course course = courseRepository.findById(request.getCourseId())
                        .orElseThrow(() -> new RuntimeException("Course not found: " + request.getCourseId()));
                interaction.setCourse(course);
            }

            // Set lesson if provided
            if (request.getLessonId() != null) {
                Lesson lesson = lessonRepository.findById(request.getLessonId())
                        .orElseThrow(() -> new RuntimeException("Lesson not found: " + request.getLessonId()));
                interaction.setLesson(lesson);
                // Also set course from lesson if not explicitly provided
                if (interaction.getCourse() == null && lesson.getSection() != null) {
                    interaction.setCourse(lesson.getSection().getCourse());
                }
            }

            interactionRepository.save(interaction);
            log.info("Tracked {} interaction for user {} on course {}", 
                    request.getInteractionType(), user.getId(), request.getCourseId());

        } catch (Exception e) {
            log.error("Failed to track interaction for user {}: {}", userEmail, e.getMessage());
            // Don't throw - interaction tracking should not break main flow
        }
    }

    /**
     * Synchronous tracking - for when you need to wait for completion.
     */
    public void trackInteractionSync(String userEmail, InteractionRequest request) {
        trackInteraction(userEmail, request);
    }
}
