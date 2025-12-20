package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.CommentRequestDto;
import com.doanptit.elearing_backend_service.dto.res.CommentResponseDto;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Comment;
import com.doanptit.elearing_backend_service.model.Lesson;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.CommentRepository;
import com.doanptit.elearing_backend_service.repository.LessonRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.CommentService;
import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.service.even.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponseDto createComment(Long lessonId, CommentRequestDto request, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCreatedByUser(currentUser);
        comment.setUpdatedByUser(currentUser);
        comment.setLesson(lesson);

        // 2. Xử lý logic Reply và Notification
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            if (!parent.getLesson().getId().equals(lessonId)) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            comment.setParentComment(parent);

            // Chuẩn bị dữ liệu thông báo
            String replierName = currentUser.getFirstname() + " " + currentUser.getLastname();
            String parentAuthorEmail = parent.getCreatedByUser().getEmail();
            String parentAuthorName = parent.getCreatedByUser().getFirstname() + " " + parent.getCreatedByUser().getLastname();
            String lessonTitle = lesson.getTitle();
            String link = String.format("/learning/%d?lesson=%d",
                    lesson.getSection().getCourse().getId(), lesson.getId());
            String notiTitle = "Phản hồi bình luận mới";

            // A. Thông báo cho chủ sở hữu comment gốc (Người được reply trực tiếp)
            if (!parentAuthorEmail.equals(userEmail)) {
                String msgForParent = String.format("%s đã phản hồi bình luận của bạn trong bài giảng %s",
                        replierName, lessonTitle);

                eventPublisher.publishEvent(new NotificationEvent(this,
                        List.of(parentAuthorEmail), notiTitle, msgForParent, link));
            }

            // B. Thông báo cho những người khác đã từng reply trong thread này
            List<String> otherReplierEmails = commentRepository.findEmailsOfRepliers(parent.getId());

            // Loại bỏ chính mình và loại bỏ chủ comment gốc (vì đã gửi ở bước A)
            Set<String> otherRecipients = new HashSet<>(otherReplierEmails);
            otherRecipients.remove(userEmail);
            otherRecipients.remove(parentAuthorEmail);

            if (!otherRecipients.isEmpty()) {
                String msgForOthers = String.format("%s đã phản hồi bình luận của %s trong bài giảng %s",
                        replierName, parentAuthorName, lessonTitle);

                eventPublisher.publishEvent(new NotificationEvent(this,
                        new ArrayList<>(otherRecipients), notiTitle, msgForOthers, link));
            }
        }

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponseDto> getCommentsByLesson(Long lessonId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        Page<Comment> commentPage = commentRepository.findRootCommentsByLessonId(lessonId, pageable);

        List<CommentResponseDto> content = commentPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content, commentPage.getNumber(), commentPage.getSize(),
                commentPage.getTotalElements(), commentPage.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        User requester = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Logic Soft Delete (Xóa mềm - chỉ điền deletedOn)
        // Chỉ Chính chủ hoặc Admin mới được xóa
        boolean isOwner = comment.getCreatedByUser().getEmail().equals(userEmail);
        boolean isAdmin = requester.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        comment.setDeletedOn(LocalDateTime.now()); // Soft delete
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto request, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // CHECK QUYỀN: Chỉ chính chủ mới được sửa
        if (!comment.getCreatedByUser().getEmail().equals(userEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Cập nhật nội dung
        comment.setContent(request.getContent());

        // (Trường updatedOn sẽ tự động được cập nhật nhờ @UpdateTimestamp trong BaseEntity/Comment Entity)

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    // --- HÀM MAP ĐỆ QUY (Để hiện comment cha con lồng nhau) ---
    private CommentResponseDto mapToDto(Comment comment) {
        User u = comment.getCreatedByUser();

        // Tạo thông tin tóm tắt của User
        CommentResponseDto.UserSummaryDto userDto = CommentResponseDto.UserSummaryDto.builder()
                .id(u.getId())
                .fullName((u.getFirstname() + " " + u.getLastname()).trim())
                .image(u.getImage())
                .role(u.getRole().name())
                .build();

        // Đệ quy lấy replies (chỉ lấy những cái chưa bị xóa)
        List<CommentResponseDto> repliesDto = null;
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            repliesDto = comment.getReplies().stream()
                    .filter(c -> c.getDeletedOn() == null) // Bỏ qua comment đã xóa
                    .map(this::mapToDto) // Gọi lại chính nó
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