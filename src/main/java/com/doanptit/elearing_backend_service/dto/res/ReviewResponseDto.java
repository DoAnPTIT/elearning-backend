package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponseDto {
    private Long id;
    private Integer rating;
    private String comment;
    private String userName;
    private String userAvatar;
    private LocalDateTime createdAt;
}
