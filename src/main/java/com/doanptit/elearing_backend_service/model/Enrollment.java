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

    private Boolean active = true;
    private Float progress;

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

