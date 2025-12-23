package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.ExamSubmitRequestDto;
import com.doanptit.elearing_backend_service.dto.res.ExamStartResponseDto;
import com.doanptit.elearing_backend_service.dto.res.ExamSubmitResponseDto;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.*;
import com.doanptit.elearing_backend_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentExamController {
    private final ExamRepository examRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ExamAttemptStateRepository attemptStateRepository;
    private final SubmissionRepository submissionRepository;

    @PostMapping("/{examId}/start")
    @Transactional
    public ResponseEntity<ApiResponse<ExamStartResponseDto>> start(
            @PathVariable Long examId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Long courseId = exam.getSection() != null && exam.getSection().getCourse() != null
                ? exam.getSection().getCourse().getId()
                : null;
        if (courseId != null) {
            EnrollmentStatus status = enrollmentRepository.findEnrollmentStatus(email, courseId)
                    .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_APPROVED));
            if (status != EnrollmentStatus.APPROVED) {
                throw new AppException(ErrorCode.ENROLLMENT_NOT_APPROVED);
            }
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ExamAttemptState state = attemptStateRepository.findByUser_EmailAndExam_Id(email, examId)
                .orElseGet(() -> ExamAttemptState.builder()
                        .user(user)
                        .exam(exam)
                        .attemptsUsed(0)
                        .build());

        LocalDateTime now = LocalDateTime.now();
        if (state.getCooldownUntil() != null) {
            if (state.getCooldownUntil().isAfter(now)) {
                throw new AppException(ErrorCode.EXAM_COOLDOWN_ACTIVE);
            } else {
                state.setCooldownUntil(null);
                state.setAttemptsUsed(0);
            }
        }

        int maxAttempts = exam.getMaxAttempts() != null && exam.getMaxAttempts() > 0 ? exam.getMaxAttempts() : 1;
        int attemptsUsed = state.getAttemptsUsed() != null ? state.getAttemptsUsed() : 0;
        int attemptsRemaining = Math.max(0, maxAttempts - attemptsUsed);
        if (attemptsRemaining <= 0) {
            state.setCooldownUntil(now.plusHours(24));
            state.setAttemptsUsed(0);
            attemptStateRepository.save(state);
            throw new AppException(ErrorCode.EXAM_COOLDOWN_ACTIVE);
        }

        state.setLastStartedAt(now);
        ExamAttemptState savedState = attemptStateRepository.save(state);

        List<ExamStartResponseDto.QuestionForStudentDto> questions = Optional.ofNullable(exam.getQuestions())
                .orElse(Collections.emptyList())
                .stream()
                .filter(q -> q.getActive() == null || q.getActive())
                .map(q -> ExamStartResponseDto.QuestionForStudentDto.builder()
                        .id(q.getId())
                        .content(q.getContent())
                        .point(q.getPoint())
                        .answers(Optional.ofNullable(q.getAnswers()).orElse(Collections.emptyList()).stream()
                                .map(a -> ExamStartResponseDto.AnswerForStudentDto.builder()
                                        .id(a.getId())
                                        .content(a.getContent())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        ExamStartResponseDto dto = ExamStartResponseDto.builder()
                .examId(exam.getId())
                .title(exam.getTitle())
                .timeLimitMinutes(exam.getTimeLimitMinutes())
                .maxAttempts(maxAttempts)
                .attemptsRemaining(attemptsRemaining)
                .cooldownUntil(savedState.getCooldownUntil())
                .startedAt(savedState.getLastStartedAt())
                .questions(questions)
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/{examId}/submit")
    @Transactional
    public ResponseEntity<ApiResponse<ExamSubmitResponseDto>> submit(
            @PathVariable Long examId,
            @RequestBody(required = false) ExamSubmitRequestDto request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ExamAttemptState state = attemptStateRepository.findByUser_EmailAndExam_Id(email, examId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        LocalDateTime now = LocalDateTime.now();
        if (state.getCooldownUntil() != null && state.getCooldownUntil().isAfter(now)) {
            throw new AppException(ErrorCode.EXAM_COOLDOWN_ACTIVE);
        }

        Integer timeLimitMinutes = exam.getTimeLimitMinutes();
        if (timeLimitMinutes != null && timeLimitMinutes > 0 && state.getLastStartedAt() != null) {
            if (state.getLastStartedAt().plusMinutes(timeLimitMinutes).isBefore(now)) {
                throw new AppException(ErrorCode.EXAM_TIME_EXPIRED);
            }
        }

        Map<Long, Set<Long>> submitted = new HashMap<>();
        if (request != null && request.getAnswers() != null) {
            for (var a : request.getAnswers()) {
                if (a == null || a.getQuestionId() == null) continue;
                Set<Long> ids = a.getAnswerIds() != null ? new HashSet<>(a.getAnswerIds()) : new HashSet<>();
                submitted.put(a.getQuestionId(), ids);
            }
        }

        int score = 0;
        int maxScore = 0;
        List<Question> questions = Optional.ofNullable(exam.getQuestions()).orElse(Collections.emptyList());
        for (Question q : questions) {
            if (q == null) continue;
            int point = q.getPoint() != null ? q.getPoint() : 0;
            maxScore += point;

            Set<Long> correct = Optional.ofNullable(q.getAnswers()).orElse(Collections.emptyList()).stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .map(Answer::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<Long> chosen = submitted.getOrDefault(q.getId(), Collections.emptySet());
            if (!correct.isEmpty() && correct.equals(chosen)) {
                score += point;
            }
        }

        Submission submission = Submission.builder()
                .score(score)
                .user(user)
                .exam(exam)
                .build();
        Submission savedSubmission = submissionRepository.save(submission);

        int maxAttempts = exam.getMaxAttempts() != null && exam.getMaxAttempts() > 0 ? exam.getMaxAttempts() : 1;
        int attemptsUsed = state.getAttemptsUsed() != null ? state.getAttemptsUsed() : 0;
        attemptsUsed += 1;

        Integer attemptsRemaining;
        if (attemptsUsed >= maxAttempts) {
            state.setCooldownUntil(now.plusHours(24));
            state.setAttemptsUsed(0);
            attemptsRemaining = 0;
        } else {
            state.setAttemptsUsed(attemptsUsed);
            attemptsRemaining = Math.max(0, maxAttempts - attemptsUsed);
        }
        state.setLastStartedAt(null);
        attemptStateRepository.save(state);

        ExamSubmitResponseDto dto = ExamSubmitResponseDto.builder()
                .submissionId(savedSubmission.getId())
                .score(score)
                .maxScore(maxScore)
                .attemptsRemaining(attemptsRemaining)
                .cooldownUntil(state.getCooldownUntil())
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}


