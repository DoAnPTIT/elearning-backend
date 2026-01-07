package com.doanptit.elearing_backend_service.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private String name;
    private String description;
    private Long courseId;
    private String courseTitle;
    private Long createdBy;
    private Integer memberCount;
    private Integer onlineCount;
    private Set<Long> onlineUserIds;
    private Boolean isCurrentUserOnline;
    private ChatMessageResponse lastMessage;
    private LocalDateTime createdAt;
}

