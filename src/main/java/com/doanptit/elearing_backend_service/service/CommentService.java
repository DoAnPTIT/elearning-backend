package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.CommentRequestDto;
import com.doanptit.elearing_backend_service.dto.res.CommentResponseDto;

public interface CommentService {

    // Tạo comment mới hoặc reply
    CommentResponseDto createComment(Long lessonId, CommentRequestDto request, String userEmail);

    // Lấy danh sách comment của bài học (Phân trang)
    PagedResponse<CommentResponseDto> getCommentsByLesson(Long lessonId, int page, int size);

    // Xóa comment (Soft delete)
    void deleteComment(Long commentId, String userEmail);

    CommentResponseDto updateComment(Long commentId, CommentRequestDto request, String userEmail);
}