package com.doanptit.elearing_backend_service.model;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.LearningGoal;
import com.doanptit.elearing_backend_service.enums.LearningStyle;
import com.doanptit.elearing_backend_service.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_goal")
    private LearningGoal learningGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    private SkillLevel skillLevel;

    @Column(name = "daily_learning_time")
    private Integer dailyLearningTime; // in minutes

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style")
    private LearningStyle learningStyle;

    @ElementCollection(targetClass = CourseCategory.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_categories", joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    @Builder.Default
    private List<CourseCategory> interestedCategories = new ArrayList<>();

    // Factory method for default preferences
    public static UserPreference createDefault(User user) {
        return UserPreference.builder()
                .user(user)
                .learningGoal(LearningGoal.SKILL_UP)
                .skillLevel(SkillLevel.BEGINNER)
                .dailyLearningTime(30)
                .learningStyle(LearningStyle.MIXED)
                .interestedCategories(new ArrayList<>(List.of(CourseCategory.INFORMATION_TECHNOLOGY)))
                .build();
    }
}
