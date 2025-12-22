package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.NotificationResponse;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Notification;
import com.doanptit.elearing_backend_service.repository.NotificationRepository;
import com.doanptit.elearing_backend_service.service.EmailService;
import com.doanptit.elearing_backend_service.service.NotificationService;
import com.doanptit.elearing_backend_service.service.even.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    @Async
    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        // SỬA: Lấy danh sách emails thay vì 1 ID
        List<String> recipients = event.getRecipientEmails();
        log.info("Sending notification to {} users", recipients.size());

        List<Notification> notificationsToSave = new ArrayList<>();

        // VÒNG LẶP: Xử lý cho từng người trong danh sách
        for (String email : recipients) {

            // 1. Tạo Entity
            Notification notification = Notification.builder()
                    .recipientId(email) // Lấy email từ vòng lặp
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .targetUrl(event.getTargetUrl())
                    .isRead(false)
                    .build();

            notificationsToSave.add(notification);

            sendNotificationEmail(email, event);
        }

        // 2. Lưu Batch vào DB (Nhanh hơn lưu lẻ)
        List<Notification> savedNotifs = notificationRepository.saveAll(notificationsToSave);

        // 3. Gửi Real-time qua WebSocket (Phải gửi lẻ từng người)
        for (Notification savedNotif : savedNotifs) {
            NotificationResponse response = NotificationResponse.builder()
                    .id(savedNotif.getId())
                    .title(savedNotif.getTitle())
                    .message(savedNotif.getMessage())
                    .targetUrl(savedNotif.getTargetUrl())
                    .isRead(false)
                    .createdAt(savedNotif.getCreatedAt())
                    .build();

            // Gửi đến đúng User đó
            messagingTemplate.convertAndSendToUser(
                    savedNotif.getRecipientId(),
                    "/queue/notifications",
                    response
            );
        }
    }

    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(email, pageable);

        Page<NotificationResponse> dtoPage = notifs.map(n -> NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .targetUrl(n.getTargetUrl())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build());

        return new PagedResponse<>(dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(), dtoPage.getTotalElements(), dtoPage.getTotalPages());
    }

    @Override
    public void markAsRead(Long id, String email) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getRecipientId().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String recipientId) {
        notificationRepository.markAllAsRead(recipientId);
    }

    @Override
    public long countUnread(String email) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(email);
    }

    // Hàm phụ trợ để tạo nội dung HTML cho đẹp
    private void sendNotificationEmail(String toEmail, NotificationEvent event) {
        String subject = "[E-Learning] " + event.getTitle();

        // Tạo nội dung HTML
        String htmlContent = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;">
                <h2 style="color: #2c3e50;">Thông báo mới</h2>
                <p style="font-size: 16px; color: #333;">%s</p>
                <br>
                <a href="http://localhost:5173%s" style="background-color: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;">Xem chi tiết</a>
                <p style="margin-top: 20px; font-size: 12px; color: #777;">Nếu nút trên không hoạt động, hãy copy link này: http://localhost:5173%s</p>
            </div>
            """, event.getMessage(), event.getTargetUrl(), event.getTargetUrl());

        emailService.sendEmail(toEmail, subject, htmlContent);
    }
}