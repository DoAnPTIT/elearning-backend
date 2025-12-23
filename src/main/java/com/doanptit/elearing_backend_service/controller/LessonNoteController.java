package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.LessonNoteRequestDto;
import com.doanptit.elearing_backend_service.dto.res.LessonNoteResponseDto;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Lesson;
import com.doanptit.elearing_backend_service.model.LessonNote;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.LessonNoteRepository;
import com.doanptit.elearing_backend_service.repository.LessonRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/lessons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class LessonNoteController {
    private final LessonNoteRepository lessonNoteRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @GetMapping("/{lessonId}/note")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<LessonNoteResponseDto>> getNote(
            @PathVariable Long lessonId,
            Authentication authentication
    ) {
        LessonNote note = lessonNoteRepository.findByUser_EmailAndLesson_Id(authentication.getName(), lessonId)
                .orElse(null);
        LessonNoteResponseDto dto = LessonNoteResponseDto.builder()
                .lessonId(lessonId)
                .content(note != null ? note.getContent() : "")
                .updatedOn(note != null ? note.getUpdatedOn() : null)
                .build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{lessonId}/note")
    @Transactional
    public ResponseEntity<ApiResponse<LessonNoteResponseDto>> upsertNote(
            @PathVariable Long lessonId,
            @RequestBody(required = false) LessonNoteRequestDto request,
            Authentication authentication
    ) {
        String content = request != null ? request.getContent() : null;

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        LessonNote note = lessonNoteRepository.findByUser_EmailAndLesson_Id(authentication.getName(), lessonId)
                .orElseGet(() -> LessonNote.builder().user(user).lesson(lesson).build());
        note.setContent(content != null ? content : "");
        LessonNote saved = lessonNoteRepository.save(note);

        LessonNoteResponseDto dto = LessonNoteResponseDto.builder()
                .lessonId(lessonId)
                .content(saved.getContent())
                .updatedOn(saved.getUpdatedOn())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Đã lưu ghi chú.", dto));
    }
}


