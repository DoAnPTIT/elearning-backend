package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.NotificationResponse;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Notification;
import com.doanptit.elearing_backend_service.repository.NotificationRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    @Override
    @Async
    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Sending notification to user: {}", event.getRecipientId());

        // 1. Lưu vào Database (Persistence)
        Notification notification = Notification.builder()
                .recipientId(event.getRecipientId())
                .title(event.getTitle())
                .message(event.getMessage())
                .targetUrl(event.getTargetUrl())
                .isRead(false)
                .build();
        Notification savedNotif = notificationRepository.save(notification);

        // 2. Tạo Response DTO
        NotificationResponse response = NotificationResponse.builder()
                .id(savedNotif.getId())
                .title(savedNotif.getTitle())
                .message(savedNotif.getMessage())
                .targetUrl(savedNotif.getTargetUrl())
                .isRead(false)
                .createdAt(savedNotif.getCreatedAt())
                .build();

        // 3. Gửi Real-time qua WebSocket
        // User sẽ nhận được tại topic: /user/{email}/queue/notifications
        messagingTemplate.convertAndSendToUser(
                event.getRecipientId(),
                "/queue/notifications",
                response
        );
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
}