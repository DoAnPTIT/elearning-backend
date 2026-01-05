package com.doanptit.elearing_backend_service.dto.req;

import lombok.Data;

@Data
public class LessonProgressRequest {
    private Float progress; // Percentage watched (0-100)
    private Boolean completed;
    private Integer lastWatchedPosition; // Last watched position in seconds
}
