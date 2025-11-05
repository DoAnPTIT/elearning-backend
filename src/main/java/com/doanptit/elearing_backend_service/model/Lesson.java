package com.doanptit.elearing_backend_service.model;

import com.doanptit.elearing_backend_service.enums.LessonType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String image;
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    private LessonType lessonType;

    private String videoUrl;

    private Long duration;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String articleContent;

    @Column(name = "lesson_order")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

}
