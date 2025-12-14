package com.doanptit.elearing_backend_service.dto.req;

import lombok.Data;

@Data
public class LessonProgressRequest {
    private Float progress;
    private Boolean completed;
}
