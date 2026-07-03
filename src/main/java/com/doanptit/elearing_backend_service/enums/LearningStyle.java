package com.doanptit.elearing_backend_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LearningStyle {
    VIDEO("Xem video"),
    ARTICLE("Đọc bài viết"),
    PRACTICE("Làm bài tập"),
    MIXED("Kết hợp tất cả");

    private final String vietnameseName;

    LearningStyle(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    @JsonValue
    public String getVietnameseName() {
        return vietnameseName;
    }

    @JsonCreator
    public static LearningStyle fromString(String value) {
        if (value == null) {
            return null;
        }
        for (LearningStyle style : LearningStyle.values()) {
            if (style.vietnameseName.equalsIgnoreCase(value) || style.name().equalsIgnoreCase(value)) {
                return style;
            }
        }
        throw new IllegalArgumentException("Unknown LearningStyle: " + value);
    }
}
