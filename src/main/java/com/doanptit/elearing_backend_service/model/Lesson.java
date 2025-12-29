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
    @Builder.Default
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type")
    private LessonType lessonType;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "duration")
    private Long duration;

    @Lob
    @Column(name = "article_content", columnDefinition = "TEXT")
    private String articleContent;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "lesson_order")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

}
