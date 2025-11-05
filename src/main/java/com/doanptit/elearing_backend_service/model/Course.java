package com.doanptit.elearing_backend_service.model;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    private String image;
    private Boolean active = true;

    @OneToMany(mappedBy = "course")
    private List<Enrollment> enrollments;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    @Column(columnDefinition = "TEXT")
    private String targetAudience;

    @Enumerated(EnumType.STRING)
    private CourseCategory category;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<Section> sections;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}

