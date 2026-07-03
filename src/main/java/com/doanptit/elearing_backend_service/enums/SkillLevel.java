package com.doanptit.elearing_backend_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SkillLevel {
    BEGINNER("Mới bắt đầu"),
    INTERMEDIATE("Trung cấp"),
    ADVANCED("Nâng cao");

    private final String vietnameseName;

    SkillLevel(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    @JsonValue
    public String getVietnameseName() {
        return vietnameseName;
    }

    @JsonCreator
    public static SkillLevel fromString(String value) {
        if (value == null) {
            return null;
        }
        for (SkillLevel level : SkillLevel.values()) {
            if (level.vietnameseName.equalsIgnoreCase(value) || level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown SkillLevel: " + value);
    }
}
