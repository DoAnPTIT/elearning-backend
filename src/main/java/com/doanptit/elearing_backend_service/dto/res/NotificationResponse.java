package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String targetUrl;
    private boolean isRead;
    private LocalDateTime createdAt;
}