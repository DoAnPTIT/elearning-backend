package com.doanptit.elearing_backend_service.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private String senderImage;
    private String content;
    private String messageType; // TEXT, IMAGE, FILE, SYSTEM
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Boolean edited;
    private Boolean deleted;
    private LocalDateTime createdAt;
}

