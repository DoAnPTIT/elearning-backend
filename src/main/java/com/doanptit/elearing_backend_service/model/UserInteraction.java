package com.doanptit.elearing_backend_service.model;

import com.doanptit.elearing_backend_service.enums.InteractionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_interactions", indexes = {
        @Index(name = "idx_user_interactions_user_id", columnList = "user_id"),
        @Index(name = "idx_user_interactions_course_id", columnList = "course_id"),
        @Index(name = "idx_user_interactions_type", columnList = "interaction_type"),
        @Index(name = "idx_user_interactions_created_on", columnList = "created_on")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInteraction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @Column(name = "search_query")
    private String searchQuery;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // Additional metadata for ML
    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "session_id")
    private String sessionId;
}
