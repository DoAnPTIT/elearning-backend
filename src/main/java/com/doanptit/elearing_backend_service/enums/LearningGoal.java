package com.doanptit.elearing_backend_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LearningGoal {
    GET_JOB("Đi làm ngay sau khi học"),
    SKILL_UP("Nâng cao kỹ năng hiện tại"),
    CAREER_CHANGE("Chuyển ngành nghề"),
    HOBBY("Học cho vui / sở thích");

    private final String vietnameseName;

    LearningGoal(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    @JsonValue
    public String getVietnameseName() {
        return vietnameseName;
    }

    @JsonCreator
    public static LearningGoal fromString(String value) {
        if (value == null) {
            return null;
        }
        for (LearningGoal goal : LearningGoal.values()) {
            if (goal.vietnameseName.equalsIgnoreCase(value) || goal.name().equalsIgnoreCase(value)) {
                return goal;
            }
        }
        throw new IllegalArgumentException("Unknown LearningGoal: " + value);
    }
}
