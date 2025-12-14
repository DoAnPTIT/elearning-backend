package com.doanptit.elearing_backend_service.model;

import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = {
                // 2. Đảm bảo 1 user không thể đăng ký 1 course 2 lần
                @UniqueConstraint(columnNames = {"user_id", "course_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        @Builder.Default
        private Boolean active = true;

        @Builder.Default
        private Float progress = 0f;

    @Column(name = "completed_lessons", columnDefinition = "TEXT")
    private String completedLessons;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnrollmentStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}

