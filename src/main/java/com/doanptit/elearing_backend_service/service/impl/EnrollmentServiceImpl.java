package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.res.EnrollmentStudentDto;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.AdminCourseMapper;
import com.doanptit.elearing_backend_service.model.Course;
import com.doanptit.elearing_backend_service.model.Enrollment;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.CourseRepository;
import com.doanptit.elearing_backend_service.repository.EnrollmentRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.EnrollmentService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AdminCourseMapper adminCourseMapper;

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
    @Transactional
    public void removeEnrollment(Long enrollmentId, String teacherEmail) {
        Enrollment enrollment = findEnrollmentAndCheckAuthorship(enrollmentId, teacherEmail);
        enrollmentRepository.delete(enrollment);
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

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentStudentDto> getEnrollmentsForCourse(
            Long courseId, String teacherEmail, EnrollmentStatus status,
            String studentName, int page, int size, String... sort) {

        // 1. Kiểm tra bảo mật: Teacher có phải chủ khóa học không?
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        if (!course.getAuthor().getEmail().equals(teacherEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 2. Tạo Pageable (Dùng hàm phụ createPageable ở dưới)
        Pageable pageable = createPageable(page, size, sort, "enrolledAt"); // Mặc định sort theo ngày đăng ký

        // 3. Tạo Specification (Lọc động)
        Specification<Enrollment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // -- Lọc CỨNG: Chỉ lấy đăng ký của khóa học này --
            predicates.add(cb.equal(root.get("course").get("id"), courseId));

            // -- Lọc tùy chọn (Filter) --
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // -- Tìm kiếm (Search) --
            if (studentName != null && !studentName.isBlank()) {
                Join<Enrollment, User> userJoin = root.join("user", JoinType.LEFT);
                String pattern = "%" + studentName.toLowerCase() + "%";
                Predicate matchFirst = cb.like(cb.lower(userJoin.get("firstname")), pattern);
                Predicate matchLast = cb.like(cb.lower(userJoin.get("lastname")), pattern);
                Predicate matchEmail = cb.like(cb.lower(userJoin.get("email")), pattern);
                predicates.add(cb.or(matchFirst, matchLast, matchEmail));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 4. Gọi Repository
        Page<Enrollment> enrollmentPage = enrollmentRepository.findAll(spec, pageable);

        // 5. Map sang DTO (Dùng mapper của Admin vì nó có hàm toEnrollmentStudentDto)
        Page<EnrollmentStudentDto> dtoPage = enrollmentPage.map(adminCourseMapper::toEnrollmentStudentDto);

        // 6. Trả về
        return new PagedResponse<>(
                dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
                dtoPage.getTotalElements(), dtoPage.getTotalPages()
        );
    }

    private Pageable createPageable(int page, int size, String[] sort, String defaultSortField) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        List<String> allowedFields = List.of("id", "status", "enrolledAt", "createdOn"); // (Các trường trong Enrollment)
        List<Sort.Order> orders = new ArrayList<>();

        if (sort != null && sort.length > 0) {
            for (int i = 0; i < sort.length; i++) {
                String raw = sort[i];
                if (raw == null) continue;
                raw = raw.trim();
                if (raw.isBlank()) continue;
                if (raw.contains(",")) {
                    String[] parts = raw.split(",", 2);
                    String field = parts[0].trim();
                    String direction = parts.length == 2 ? parts[1].trim() : "asc";
                    if (!allowedFields.contains(field)) continue;
                    orders.add(direction.equalsIgnoreCase("desc") ? Sort.Order.desc(field) : Sort.Order.asc(field));
                    continue;
                }
                String next = (i + 1) < sort.length ? sort[i + 1] : null;
                if (next != null) {
                    next = next.trim();
                    if (next.equalsIgnoreCase("asc") || next.equalsIgnoreCase("desc")) {
                        String field = raw;
                        String direction = next;
                        if (allowedFields.contains(field)) {
                            orders.add(direction.equalsIgnoreCase("desc") ? Sort.Order.desc(field) : Sort.Order.asc(field));
                        }
                        i++;
                        continue;
                    }
                }
                String field = raw;
                if (!allowedFields.contains(field)) continue;
                orders.add(Sort.Order.asc(field));
            }
        }

        Sort sortSpec = orders.isEmpty()
                ? Sort.by(Sort.Order.desc(defaultSortField))
                : Sort.by(orders);

        return PageRequest.of(page, size, sortSpec);
    }
}