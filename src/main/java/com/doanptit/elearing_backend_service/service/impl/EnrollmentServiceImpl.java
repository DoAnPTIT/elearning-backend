package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.LessonProgressRequest;
import com.doanptit.elearing_backend_service.dto.res.CourseProgressResponse;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.Enrollment;
import com.doanptit.elearing_backend_service.model.Lesson;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.repository.EnrollmentRepository;
import com.doanptit.elearing_backend_service.repository.LessonRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    private static final String COMPLETED_LESSON_DELIMITER = ",";

    @Override
    @Transactional
    public void enrollCourse(Long courseId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new AppException(ErrorCode.COURSE_NOT_ACTIVE);
        }

        enrollmentRepository.findByUser_EmailAndCourse_Id(studentEmail, courseId)
                .ifPresent(enrollment -> {
                    throw new AppException(ErrorCode.ALREADY_ENROLLED);
                });

        Enrollment newEnrollment = Enrollment.builder()
                .user(student)
                .course(course)
                .status(EnrollmentStatus.PENDING)
                .build();

        enrollmentRepository.save(newEnrollment);
    }

    @Override
    @Transactional
    public void approveEnrollment(Long enrollmentId, String teacherEmail) {
        Enrollment enrollment = findEnrollmentAndCheckAuthorship(enrollmentId, teacherEmail);

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new AppException(ErrorCode.ENROLLMENT_NOT_PENDING);
        }

        enrollment.setStatus(EnrollmentStatus.APPROVED);
        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public void rejectEnrollment(Long enrollmentId, String teacherEmail) {
        Enrollment enrollment = findEnrollmentAndCheckAuthorship(enrollmentId, teacherEmail);

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new AppException(ErrorCode.ENROLLMENT_NOT_PENDING);
        }

        enrollment.setStatus(EnrollmentStatus.REJECTED);
        enrollmentRepository.save(enrollment);
    }

    @Override
    public PagedResponse<?> getCourseEnrollments(Long courseId, String status, String teacherEmail, int page, int size) {
        // Verify course exists and teacher owns it
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.getAuthor().getEmail().equals(teacherEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        Page<Enrollment> enrollmentPage;

        if (status != null && !status.isEmpty()) {
            try {
                EnrollmentStatus enrollmentStatus = EnrollmentStatus.valueOf(status.toUpperCase());
                enrollmentPage = enrollmentRepository.findByCourse_IdAndStatus(courseId, enrollmentStatus, pageable);
            } catch (IllegalArgumentException e) {
                enrollmentPage = enrollmentRepository.findByCourse_Id(courseId, pageable);
            }
        } else {
            enrollmentPage = enrollmentRepository.findByCourse_Id(courseId, pageable);
        }

        List<Map<String, Object>> content = enrollmentPage.getContent().stream()
                .map(this::mapEnrollmentToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                enrollmentPage.getNumber(),
                enrollmentPage.getSize(),
                enrollmentPage.getTotalElements(),
                enrollmentPage.getTotalPages()
        );
    }

    @Override
    public PagedResponse<?> getStudentEnrollments(String studentEmail, String status, int page, int size) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        Page<Enrollment> enrollmentPage;

        if (status != null && !status.isEmpty()) {
            try {
                EnrollmentStatus enrollmentStatus = EnrollmentStatus.valueOf(status.toUpperCase());
                enrollmentPage = enrollmentRepository.findByUser_EmailAndStatus(studentEmail, enrollmentStatus, pageable);
            } catch (IllegalArgumentException e) {
                enrollmentPage = enrollmentRepository.findByUser_Email(studentEmail, pageable);
            }
        } else {
            enrollmentPage = enrollmentRepository.findByUser_Email(studentEmail, pageable);
        }

        List<Map<String, Object>> content = enrollmentPage.getContent().stream()
                .map(this::mapEnrollmentWithCourseToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                enrollmentPage.getNumber(),
                enrollmentPage.getSize(),
                enrollmentPage.getTotalElements(),
                enrollmentPage.getTotalPages()
        );
    }

    private Map<String, Object> mapEnrollmentToDto(Enrollment enrollment) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", enrollment.getId());
        dto.put("status", enrollment.getStatus().toString());
        dto.put("createdAt", enrollment.getCreatedOn());
        dto.put("updatedAt", enrollment.getUpdatedOn());
        dto.put("progress", enrollment.getProgress());
        
        Map<String, Object> userDto = new HashMap<>();
        userDto.put("id", enrollment.getUser().getId());
        userDto.put("email", enrollment.getUser().getEmail());
        userDto.put("firstname", enrollment.getUser().getFirstname());
        userDto.put("lastname", enrollment.getUser().getLastname());
        dto.put("user", userDto);
        dto.put("student", userDto);
        
        return dto;
    }

    private Map<String, Object> mapEnrollmentWithCourseToDto(Enrollment enrollment) {
        Map<String, Object> dto = mapEnrollmentToDto(enrollment);
        
        Course course = enrollment.getCourse();
        Map<String, Object> courseDto = new HashMap<>();
        courseDto.put("id", course.getId());
        courseDto.put("title", course.getTitle());
        courseDto.put("description", course.getDescription());
        courseDto.put("image", course.getImage());
        courseDto.put("thumbnail", course.getImage()); // Use image as thumbnail
        
        if (course.getAuthor() != null) {
            Map<String, Object> authorDto = new HashMap<>();
            authorDto.put("id", course.getAuthor().getId());
            authorDto.put("fullname", course.getAuthor().getFirstname() + " " + course.getAuthor().getLastname());
            courseDto.put("author", authorDto);
        }
        
        dto.put("course", courseDto);
        dto.put("courseId", course.getId());
        
        return dto;
    }

    @Override
    public Object getEnrollmentForCourse(Long courseId, String studentEmail) {
        var enrollmentOpt = enrollmentRepository.findByUser_EmailAndCourse_Id(studentEmail, courseId);
        
        if (enrollmentOpt.isEmpty()) {
            return null;
        }
        
        Enrollment enrollment = enrollmentOpt.get();
        return mapEnrollmentToDto(enrollment);
    }

    @Override
    @Transactional
    public CourseProgressResponse updateLessonProgress(Long lessonId, LessonProgressRequest request, String studentEmail) {
        if (request == null) {
            request = new LessonProgressRequest();
            request.setCompleted(true);
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Long courseId = lesson.getSection().getCourse().getId();

        Enrollment enrollment = enrollmentRepository
                .findByUser_EmailAndCourse_Id(studentEmail, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (enrollment.getStatus() != EnrollmentStatus.APPROVED) {
            throw new AppException(ErrorCode.ENROLLMENT_NOT_APPROVED);
        }

        Float progressValue = request.getProgress();
        Boolean completedFlag = request.getCompleted();

        boolean markCompleted = Boolean.TRUE.equals(completedFlag) ||
                (progressValue != null && progressValue >= 100f);

        boolean shouldRemoveCompletion = Boolean.FALSE.equals(completedFlag) ||
                (progressValue != null && progressValue < 100f);

        Set<Long> completedLessons = extractCompletedLessonIds(enrollment);

        if (markCompleted) {
            completedLessons.add(lessonId);
        } else if (shouldRemoveCompletion) {
            completedLessons.remove(lessonId);
        }

        return synchronizeEnrollmentProgress(enrollment, completedLessons);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(Long courseId, String studentEmail) {
        Enrollment enrollment = enrollmentRepository
                .findByUser_EmailAndCourse_Id(studentEmail, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (enrollment.getStatus() != EnrollmentStatus.APPROVED) {
            throw new AppException(ErrorCode.ENROLLMENT_NOT_APPROVED);
        }

        Set<Long> completedLessons = extractCompletedLessonIds(enrollment);
        long totalLessons = lessonRepository.countBySection_Course_Id(courseId);
        float progressPercent = calculateProgressPercent(totalLessons, completedLessons.size());

        return mapCourseProgressResponse(enrollment, totalLessons, completedLessons, progressPercent);
    }

    private CourseProgressResponse synchronizeEnrollmentProgress(Enrollment enrollment,
                                                                 Set<Long> completedLessonIds) {
        Long courseId = enrollment.getCourse().getId();
        long totalLessons = lessonRepository.countBySection_Course_Id(courseId);

        float progressPercent = calculateProgressPercent(totalLessons, completedLessonIds.size());

        enrollment.setProgress(progressPercent);
        enrollment.setCompletedLessons(serializeCompletedLessonIds(completedLessonIds));
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return mapCourseProgressResponse(savedEnrollment, totalLessons, completedLessonIds, progressPercent);
    }

    private float calculateProgressPercent(long totalLessons, int completedLessons) {
        if (totalLessons <= 0) {
            return 0f;
        }
        return (completedLessons * 100f) / totalLessons;
    }

    private CourseProgressResponse mapCourseProgressResponse(Enrollment enrollment,
                                                             long totalLessons,
                                                             Set<Long> completedLessonIds,
                                                             float progressPercent) {
        List<Long> completedLessonIdList = completedLessonIds.stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        return CourseProgressResponse.builder()
                .courseId(enrollment.getCourse().getId())
                .enrollmentId(enrollment.getId())
                .totalLessons((int) totalLessons)
                .completedLessons(completedLessonIdList.size())
                .progress(progressPercent)
                .completedLessonIds(completedLessonIdList)
                .updatedAt(enrollment.getUpdatedOn())
                .build();
    }

    private Set<Long> extractCompletedLessonIds(Enrollment enrollment) {
        String stored = enrollment.getCompletedLessons();
        if (stored == null || stored.isBlank()) {
            return new LinkedHashSet<>();
        }

        return Arrays.stream(stored.split(COMPLETED_LESSON_DELIMITER))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .map(token -> {
                    try {
                        return Long.valueOf(token);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String serializeCompletedLessonIds(Set<Long> lessonIds) {
        if (lessonIds == null || lessonIds.isEmpty()) {
            return null;
        }

        return lessonIds.stream()
                .filter(Objects::nonNull)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(COMPLETED_LESSON_DELIMITER));
    }

    private Enrollment findEnrollmentAndCheckAuthorship(Long enrollmentId, String teacherEmail) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        String authorEmail = enrollment.getCourse().getAuthor().getEmail();

        if (!authorEmail.equals(teacherEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return enrollment;
    }
}