package com.doanptit.elearing_backend_service.service.even;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collections;
import java.util.List;

@Getter
public class NotificationEvent extends ApplicationEvent {

    // Thay vì lưu 1 người, ta lưu 1 danh sách
    private final List<String> recipientEmails;

    private final String title;
    private final String message;
    private final String targetUrl;

    // CONSTRUCTOR 1: Dùng cho trường hợp gửi 1 người (Như Enroll, Submit Course...)
    // (Logic cũ vẫn chạy bình thường, nó tự gói 1 người thành List)
    public NotificationEvent(Object source, String recipientEmail, String title, String message, String targetUrl) {
        super(source);
        this.recipientEmails = Collections.singletonList(recipientEmail);
        this.title = title;
        this.message = message;
        this.targetUrl = targetUrl;
    }

    // CONSTRUCTOR 2: Dùng cho trường hợp gửi NHIỀU người (Update Course, Comment Reply...)
    public NotificationEvent(Object source, List<String> recipientEmails, String title, String message, String targetUrl) {
        super(source);
        this.recipientEmails = recipientEmails;
        this.title = title;
        this.message = message;
        this.targetUrl = targetUrl;
    }
}