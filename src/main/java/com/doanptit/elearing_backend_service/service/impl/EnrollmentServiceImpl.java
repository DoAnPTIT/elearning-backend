package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.Enrollment;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.repository.EnrollmentRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

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