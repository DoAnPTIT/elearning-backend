package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.LearningGoal;
import com.doanptit.elearing_backend_service.enums.LearningStyle;
import com.doanptit.elearing_backend_service.enums.SkillLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyRequest {
    
    @NotNull(message = "Mục tiêu học tập không được để trống")
    private LearningGoal learningGoal;
    
    @NotNull(message = "Trình độ kỹ năng không được để trống")
    private SkillLevel skillLevel;
    
    @Min(value = 10, message = "Thời gian học tối thiểu 10 phút/ngày")
    @Max(value = 480, message = "Thời gian học tối đa 480 phút/ngày")
    private Integer dailyLearningTime;
    
    @NotNull(message = "Phong cách học tập không được để trống")
    private LearningStyle learningStyle;
    
    @NotEmpty(message = "Vui lòng chọn ít nhất 1 chủ đề quan tâm")
    private List<CourseCategory> interestedCategories;
}
