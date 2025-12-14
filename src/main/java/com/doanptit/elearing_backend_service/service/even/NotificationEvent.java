package com.doanptit.elearing_backend_service.service.even;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {
    private final String recipientId; // Email người nhận
    private final String title;
    private final String message;
    private final String targetUrl;

    public NotificationEvent(Object source, String recipientId, String title, String message, String targetUrl) {
        super(source);
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.targetUrl = targetUrl;
    }
}