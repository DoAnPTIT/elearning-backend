package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.CommentRequestDto;
import com.doanptit.elearing_backend_service.dto.res.CommentResponseDto;
import com.doanptit.elearing_backend_service.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // Lấy comment của Lesson
    // GET /api/comments/lesson/10?page=0&size=5
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponseDto>>> getLessonComments(
            @PathVariable Long lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<CommentResponseDto> response = commentService.getCommentsByLesson(lessonId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Tạo comment/reply mới
    // POST /api/comments/lesson/10
    @PostMapping("/lesson/{lessonId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
            @PathVariable Long lessonId,
            @Valid @RequestBody CommentRequestDto request,
            Authentication authentication) {

        CommentResponseDto response = commentService.createComment(lessonId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Xóa comment (Soft delete)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        commentService.deleteComment(commentId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa bình luận."));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto request,
            Authentication authentication) {

        CommentResponseDto response = commentService.updateComment(commentId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}