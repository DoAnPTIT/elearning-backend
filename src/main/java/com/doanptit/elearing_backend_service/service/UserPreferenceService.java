package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.req.SurveyRequest;
import com.doanptit.elearing_backend_service.dto.res.UserPreferenceResponse;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.model.UserPreference;
import com.doanptit.elearing_backend_service.repository.UserPreferenceRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserPreferenceResponse saveSurveyPreferences(String userEmail, SurveyRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserPreference preference = preferenceRepository.findByUserId(user.getId())
                .orElse(UserPreference.builder().user(user).build());

        preference.setLearningGoal(request.getLearningGoal());
        preference.setSkillLevel(request.getSkillLevel());
        preference.setDailyLearningTime(request.getDailyLearningTime());
        preference.setLearningStyle(request.getLearningStyle());
        
        // Ensure interestedCategories is initialized (null-safe)
        if (preference.getInterestedCategories() == null) {
            preference.setInterestedCategories(new java.util.ArrayList<>());
        } else {
            preference.getInterestedCategories().clear();
        }
        preference.getInterestedCategories().addAll(request.getInterestedCategories());

        UserPreference saved = preferenceRepository.save(preference);

        // Mark survey as completed
        user.setHasCompletedSurvey(true);
        userRepository.save(user);

        log.info("Saved preferences for user {}", user.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponse getPreferences(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserPreference preference = preferenceRepository.findByUserIdWithCategories(user.getId())
                .orElse(null);

        if (preference == null) {
            // Return default preferences for users who haven't completed survey
            return UserPreferenceResponse.builder()
                    .learningGoal(null)
                    .skillLevel(null)
                    .dailyLearningTime(null)
                    .learningStyle(null)
                    .interestedCategories(null)
                    .build();
        }

        return toResponse(preference);
    }

    @Transactional
    public UserPreferenceResponse getOrCreateDefaultPreferences(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserPreference preference = preferenceRepository.findByUserIdWithCategories(user.getId())
                .orElseGet(() -> {
                    log.info("Creating default preferences for user {}", user.getId());
                    UserPreference defaultPref = UserPreference.createDefault(user);
                    return preferenceRepository.save(defaultPref);
                });

        return toResponse(preference);
    }

    private UserPreferenceResponse toResponse(UserPreference preference) {
        return UserPreferenceResponse.builder()
                .id(preference.getId())
                .learningGoal(preference.getLearningGoal())
                .skillLevel(preference.getSkillLevel())
                .dailyLearningTime(preference.getDailyLearningTime())
                .learningStyle(preference.getLearningStyle())
                .interestedCategories(preference.getInterestedCategories())
                .build();
    }
}
