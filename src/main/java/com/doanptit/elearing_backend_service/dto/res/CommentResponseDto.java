package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponseDto {
    private Long id;
    private String content;
    private LocalDateTime createdOn;

    // Thông tin người comment
    private UserSummaryDto user;

    // Danh sách trả lời (Đệ quy)
    private List<CommentResponseDto> replies;

    @Data
    @Builder
    public static class UserSummaryDto {
        private Long id;
        private String fullName;
        private String image;
        private String role;
    }
}