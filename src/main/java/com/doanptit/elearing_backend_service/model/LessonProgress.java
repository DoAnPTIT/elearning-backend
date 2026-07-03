package com.doanptit.elearing_backend_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lesson_progress",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"enrollment_id", "lesson_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "watch_progress")
    @Builder.Default
    private Float watchProgress = 0.0f; // Percentage watched (0-100)

    @Column(name = "last_watched_position")
    @Builder.Default
    private Integer lastWatchedPosition = 0; // Last watched position in seconds
}

