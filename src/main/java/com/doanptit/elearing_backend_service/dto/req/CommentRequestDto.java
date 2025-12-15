package com.doanptit.elearing_backend_service.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    // Nếu là reply thì gửi kèm parentId, nếu comment mới thì null
    private Long parentId;
}