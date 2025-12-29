package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.LearningGoal;
import com.doanptit.elearing_backend_service.enums.LearningStyle;
import com.doanptit.elearing_backend_service.enums.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceResponse {
    private Long id;
    private LearningGoal learningGoal;
    private SkillLevel skillLevel;
    private Integer dailyLearningTime;
    private LearningStyle learningStyle;
    private List<CourseCategory> interestedCategories;
}
