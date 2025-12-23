package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.CommentResponseDto;
import com.doanptit.elearing_backend_service.dto.res.TeacherFeedbackCommentThreadDto;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Comment;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.CommentRepository;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') || hasRole('ADMIN')")
public class TeacherFeedbackController {
    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @GetMapping("/comments")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PagedResponse<TeacherFeedbackCommentThreadDto>>> getFeedbackComments(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        String teacherEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        Page<Comment> rootComments;
        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
            if (course.getAuthor() == null || course.getAuthor().getEmail() == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            if (!Objects.equals(course.getAuthor().getEmail(), teacherEmail)) {
                // Admin could be allowed; if admin, skip ownership check
                User requester = userRepository.findByEmail(teacherEmail)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                if (requester.getRole() == null || requester.getRole().name().equals("ADMIN") == false) {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
            }
            rootComments = commentRepository.findRootCommentsByCourseId(courseId, pageable);
        } else {
            rootComments = commentRepository.findRootCommentsByTeacherEmail(teacherEmail, pageable);
        }

        List<TeacherFeedbackCommentThreadDto> content = rootComments.getContent().stream()
                .map(this::mapThread)
                .collect(Collectors.toList());

        PagedResponse<TeacherFeedbackCommentThreadDto> response = new PagedResponse<>(
                content,
                rootComments.getNumber(),
                rootComments.getSize(),
                rootComments.getTotalElements(),
                rootComments.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private TeacherFeedbackCommentThreadDto mapThread(Comment root) {
        Course course = root.getLesson() != null && root.getLesson().getSection() != null
                ? root.getLesson().getSection().getCourse()
                : null;
        return TeacherFeedbackCommentThreadDto.builder()
                .courseId(course != null ? course.getId() : null)
                .courseTitle(course != null ? course.getTitle() : null)
                .lessonId(root.getLesson() != null ? root.getLesson().getId() : null)
                .lessonTitle(root.getLesson() != null ? root.getLesson().getTitle() : null)
                .comment(mapToDto(root))
                .build();
    }

    // Copied mapping style from CommentServiceImpl to keep reply recursion + filter deleted replies.
    private CommentResponseDto mapToDto(Comment comment) {
        User u = comment.getCreatedByUser();
        CommentResponseDto.UserSummaryDto userDto = CommentResponseDto.UserSummaryDto.builder()
                .id(u != null ? u.getId() : null)
                .fullName((u != null ? (Objects.toString(u.getFirstname(), "") + " " + Objects.toString(u.getLastname(), "")).trim() : "").trim())
                .image(u != null ? u.getImage() : null)
                .role(u != null && u.getRole() != null ? u.getRole().name() : null)
                .build();

        List<CommentResponseDto> repliesDto = null;
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            repliesDto = comment.getReplies().stream()
                    .filter(c -> c.getDeletedOn() == null)
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdOn(comment.getCreatedOn())
                .user(userDto)
                .replies(repliesDto)
                .build();
    }
}


