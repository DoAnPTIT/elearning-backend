package com.doanptit.elearing_backend_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exam_attempt_state",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exam_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAttemptState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "attempts_used", nullable = false)
    private Integer attemptsUsed;

    @Column(name = "cooldown_until")
    private LocalDateTime cooldownUntil;

    @Column(name = "last_started_at")
    private LocalDateTime lastStartedAt;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}


