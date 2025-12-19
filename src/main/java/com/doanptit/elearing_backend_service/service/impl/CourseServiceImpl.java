package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.*;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.enums.EnrollmentStatus;
import com.doanptit.elearing_backend_service.enums.ExamType;
import com.doanptit.elearing_backend_service.enums.LessonType;
import com.doanptit.elearing_backend_service.event.CourseContentUpdatedEvent;
import com.doanptit.elearing_backend_service.enums.*;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.*;
import com.doanptit.elearing_backend_service.model.*;
import com.doanptit.elearing_backend_service.repository.*;
import com.doanptit.elearing_backend_service.service.CourseService;
import com.doanptit.elearing_backend_service.service.S3Service;
import com.doanptit.elearing_backend_service.service.even.NotificationEvent;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final S3Service s3Service;
    private final SectionMapper  sectionMapper;
    private final LessonMapper lessonMapper;
    private final ExamMapper examMapper;
    private final CourseMapper courseMapper;
    private final AdminCourseMapper adminCourseMapper;
    private final PublicCourseMapper publicCourseMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CreateCourseResponse createCourse(CreateCourseRequestDto request, String teacherEmail) {
        User author = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Course course = courseMapper.toEntity(request);
        course.setAuthor(author);
        course.setStatus(CourseStatus.DRAFT);
        Course savedCourse = courseRepository.save(course);
        return courseMapper.toResponseDto(savedCourse);
    }

    @Override
    @Transactional
    public String uploadCourseImage(Long courseId, MultipartFile file, String teacherEmail) {
        Course course = findCourseByIdAndAuthor(courseId, teacherEmail);
        String imageUrl = s3Service.uploadFile(file, "course-images");
        course.setImage(imageUrl);
        courseRepository.save(course);
        return imageUrl;
    }

    @Override
    @Transactional
    public SectionResponse createSection(Long courseId, CreateSectionRequestDto request, String teacherEmail) {
        Course course = findCourseByIdAndAuthor(courseId, teacherEmail);
        Section section = sectionMapper.toEntity(request);
        section.setCourse(course);
        Section savedSection = sectionRepository.save(section);
        return sectionMapper.toSectionResponseDto(savedSection);
    }

    @Override
    @Transactional
    public LessonResponse createLesson(Long sectionId, CreateLessonRequestDto request, String teacherEmail) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));
        checkCourseAuthorship(section.getCourse(), teacherEmail);


        // If lesson type is QUIZ or ASSIGNMENT, create an Exam instead
        if (request.getLessonType() == LessonType.QUIZ || request.getLessonType() == LessonType.ASSIGNMENT) {
            Exam exam = new Exam();
            exam.setTitle(request.getTitle());
            exam.setDescription(request.getArticleContent()); // Reuse article content field for description
            exam.setExamType(request.getLessonType() == LessonType.QUIZ ? ExamType.QUIZ : ExamType.ASSIGNMENT);
            exam.setSection(section);
            exam.setActive(true);
            Exam savedExam = examRepository.save(exam);

            // Return as LessonResponse (convert exam to lesson response format)
            LessonResponse response = new LessonResponse();
            response.setId(savedExam.getId());
            response.setTitle(savedExam.getTitle());
            response.setLessonType(request.getLessonType());
            return response;
        }

        // Normal lesson creation for VIDEO and ARTICLE
        Lesson lesson = lessonMapper.toEntity(request);
        if (request.getLessonType() == LessonType.ARTICLE) {
            lesson.setArticleContent(request.getArticleContent());
        }
        lesson.setSection(section);

        Lesson savedLesson = lessonRepository.save(lesson);

        // >>>> LOGIC MỚI: Chỉ train AI nếu khóa học đang ACTIVE <<<<
        Course course = section.getCourse();
        if (course.getStatus() == CourseStatus.ACTIVE) {
            eventPublisher.publishEvent(new CourseContentUpdatedEvent(this, course.getId()));
        }

        return lessonMapper.toResponseDto(savedLesson);
    }

    @Override
    @Transactional
    public String uploadLessonVideo(Long lessonId, MultipartFile file, String teacherEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        checkCourseAuthorship(lesson.getSection().getCourse(), teacherEmail);

        if (lesson.getLessonType() != LessonType.VIDEO) {
            throw new AppException(ErrorCode.INTERNAL_ERROR); // (Nên đổi sang lỗi cụ thể)
        }

        String videoUrl = s3Service.uploadFile(file, "lesson-videos");
        lesson.setVideoUrl(videoUrl);
        lessonRepository.save(lesson);

        Course course = lesson.getSection().getCourse();
        if (course.getStatus() == CourseStatus.ACTIVE) {
            eventPublisher.publishEvent(new CourseContentUpdatedEvent(this, course.getId()));
        }
        return videoUrl;
    }

    @Override
    @Transactional
    public String uploadLessonDocument(Long lessonId, MultipartFile file, String teacherEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        checkCourseAuthorship(lesson.getSection().getCourse(), teacherEmail);

        if (lesson.getLessonType() != LessonType.ARTICLE) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

        String documentUrl = s3Service.uploadFile(file, "lesson-documents");
        lesson.setVideoUrl(documentUrl); // Reuse videoUrl field for document URL
        lessonRepository.save(lesson);
        return documentUrl;
    }

    @Override
    @Transactional
    public ExamResponse createExam(Long sectionId, CreateExamRequestDto request, String teacherEmail) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));
        checkCourseAuthorship(section.getCourse(), teacherEmail);

        Exam exam = examMapper.toEntity(request);
        exam.setSection(section);

        List<Question> questions = new ArrayList<>();
        if (request.getQuestions() != null) {
            for (var questionDto : request.getQuestions()) {
                Question question = new Question();
                question.setContent(questionDto.getContent());
                question.setQuestionType(questionDto.getQuestionType());
                question.setPoint(questionDto.getPoint());
                question.setExam(exam);

                List<Answer> answers = new ArrayList<>();
                if (questionDto.getAnswers() != null) {
                    for (var answerDto : questionDto.getAnswers()) {
                        Answer answer = new Answer();
                        answer.setContent(answerDto.getContent());
                        answer.setIsCorrect(answerDto.getIsCorrect());
                        answer.setQuestion(question);
                        answers.add(answer);
                    }
                }
                question.setAnswers(answers);
                questions.add(question);
            }
        }
        exam.setQuestions(questions);

        Exam savedExam = examRepository.save(exam);
        return examMapper.toResponseDto(savedExam);
    }

    @Override
    @Transactional
    public void submitCourseForReview(Long courseId, String teacherEmail) {
        Course course = findCourseByIdAndAuthor(courseId, teacherEmail);

        if (course.getStatus() != CourseStatus.DRAFT
            && course.getStatus() != CourseStatus.REJECTED
            && course.getStatus() != CourseStatus.HIDDEN) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_REVIEW);
        }
        if (course.getImage() == null || course.getImage().isEmpty()) {
            throw new AppException(ErrorCode.COURSE_MISSING_COVER_IMAGE);
        }
        List<Section> sections = course.getSections();
        if (sections == null || sections.isEmpty()) {
            throw new AppException(ErrorCode.COURSE_IS_EMPTY);
        }
        boolean hasContent = false;
        for (Section section : sections) {
            if ((section.getLessons() != null && !section.getLessons().isEmpty()) ||
                    (section.getExams() != null && !section.getExams().isEmpty())) {
                hasContent = true;
                break;
            }
        }
        if (!hasContent) {
            throw new AppException(ErrorCode.COURSE_IS_EMPTY);
        }
        course.setStatus(CourseStatus.PENDING_APPROVAL);
        courseRepository.save(course);
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {
            eventPublisher.publishEvent(new NotificationEvent(this,
                    admin.getEmail(),
                    "Yêu cầu phê duyệt khóa học",
                    "Giảng viên " + admin.getUsername() + " vừa gửi yêu cầu duyệt khóa học: " + course.getTitle(),
                    "/admin/courses/" + courseId // URL admin sẽ click vào
            ));
        }
    }

    @Override
    @Transactional
    public void hideCourse(Long courseId, String teacherEmail) {
        Course course = findCourseByIdAndAuthor(courseId, teacherEmail);
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new AppException(ErrorCode.COURSE_NOT_ACTIVE);
        }
        course.setStatus(CourseStatus.HIDDEN);
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminCourseListDto> getAllCoursesForTeacher(
            String teacherEmail, int page, int size,
            String title, CourseStatus status, CourseCategory category, // <-- Tham số mới
            String... sort) {

        Pageable pageable = createPageable(page, size, sort, "id");

        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Điều kiện CỨNG: Chỉ lấy của Teacher này
            predicates.add(cb.equal(root.get("author").get("email"), teacherEmail));

            // Lọc động (Mới)
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (title != null && !title.isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        Page<AdminCourseListDto> dtoPage = coursePage.map(adminCourseMapper::toCourseListDto);

        return new PagedResponse<>(
                dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
                dtoPage.getTotalElements(), dtoPage.getTotalPages()
        );
    }

    @Override
    @Transactional
    public AdminCourseDetailDto updateCourse(Long courseId, UpdateCourseRequestDto request, String requesterEmail) {
        // 1. Tìm khóa học
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // 2. Lấy User thực hiện
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Check quyền (Admin hoặc Tác giả)
        boolean isAuthor = course.getAuthor().getEmail().equals(requesterEmail);
        boolean isAdmin = requester.getRole() == com.doanptit.elearing_backend_service.enums.Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<String> changedFields = new ArrayList<>();

        // 4. Logic Update + Tracking (Chỉ ghi nhận nếu dữ liệu thực sự thay đổi)

        // -- Title --
        if (request.getTitle() != null) {
            // 1. Kiểm tra validation: Nếu gửi lên mà rỗng -> Bắn lỗi ngay
            if (request.getTitle().isBlank()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            // 2. Logic Update: Nếu dữ liệu hợp lệ và khác dữ liệu cũ -> Update
            if (!request.getTitle().equals(course.getTitle())) {
                course.setTitle(request.getTitle());
                changedFields.add("Tiêu đề");
            }
        }

        // -- Description --
        if (request.getDescription() != null
                && !request.getDescription().equals(course.getDescription())) {
            course.setDescription(request.getDescription());
            changedFields.add("Mô tả");
        }

        // -- Objectives --
        if (request.getObjectives() != null
                && !request.getObjectives().equals(course.getObjectives())) {
            course.setObjectives(request.getObjectives());
            changedFields.add("Mục tiêu khóa học");
        }

        // -- Target Audience --
        if (request.getTargetAudience() != null
                && !request.getTargetAudience().equals(course.getTargetAudience())) {
            course.setTargetAudience(request.getTargetAudience());
            changedFields.add("Đối tượng học viên");
        }

        // -- Category --
        if (request.getCategory() != null
                && request.getCategory() != course.getCategory()) {
            course.setCategory(request.getCategory());
            changedFields.add("Danh mục");
        }

        // 5. Lưu lại
        Course savedCourse = courseRepository.save(course);

        if (!changedFields.isEmpty()) {
            // Tạo chuỗi: "Tiêu đề, Mô tả, Mục tiêu"
            String changesText = String.join(", ", changedFields);

            // Nội dung thông báo
            String notificationTitle = "Cập nhật khóa học: " + savedCourse.getTitle();
            String notificationMessage = "Giảng viên đã cập nhật các mục: [" + changesText + "] của khóa học.";

            // Trong hàm updateCourse ...

            // A. Gửi cho HỌC VIÊN
            List<Enrollment> students = enrollmentRepository.findAllByCourseIdAndStatus(savedCourse.getId(), EnrollmentStatus.APPROVED);

            // Dùng Stream để lấy list email nhanh gọn
            List<String> studentEmails = students.stream()
                    .map(e -> e.getUser().getEmail())
                    .collect(Collectors.toList());

            if (!studentEmails.isEmpty()) {
                eventPublisher.publishEvent(new NotificationEvent(this,
                        studentEmails, // Gửi cả list
                        notificationTitle,
                        notificationMessage,
                        "/learning/" + savedCourse.getId()
                ));
            }

            // B. Gửi cho ADMIN
            List<User> admins = userRepository.findByRole(Role.ADMIN);

            List<String> adminEmails = admins.stream()
                    .filter(a -> !a.getEmail().equals(requesterEmail)) // Lọc chính mình
                    .map(User::getEmail)
                    .collect(Collectors.toList());

            if (!adminEmails.isEmpty()) {
                eventPublisher.publishEvent(new NotificationEvent(this,
                        adminEmails,
                        "Admin/GV cập nhật khóa học",
                        "User " + requesterEmail + " đã sửa đổi [" + changesText + "]...",
                        "/admin/courses/" + savedCourse.getId()
                ));
            }
        }

        // 6. Logic AI (Giữ nguyên)
        if (savedCourse.getStatus() == CourseStatus.ACTIVE) {
            eventPublisher.publishEvent(new CourseContentUpdatedEvent(this, savedCourse.getId()));
        }

        return adminCourseMapper.toCourseDetailDto(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCourseDetailDto getCourseForEdit(Long courseId, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        checkCourseAuthorship(course, teacherEmail);
        return adminCourseMapper.toCourseDetailDto(course);
    }

    private Course findCourseByIdAndAuthor(Long courseId, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        if (!course.getAuthor().getEmail().equals(teacherEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return course;
    }

    private void checkCourseAuthorship(Course course, String teacherEmail) {
        if (!course.getAuthor().getEmail().equals(teacherEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CourseListDto> getAllPublicCourses(
            int page, int size, CourseCategory category,
            String title, String authorName, String... sort) {

        Pageable pageable = createPageable(page, size, sort, "id");

        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), CourseStatus.ACTIVE));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (title != null && !title.isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            if (authorName != null && !authorName.isBlank()) {
                Join<Course, User> author = root.join("author", JoinType.LEFT);
                String pattern = "%" + authorName.toLowerCase() + "%";
                Predicate matchFirst = cb.like(cb.lower(author.get("firstname")), pattern);
                Predicate matchLast = cb.like(cb.lower(author.get("lastname")), pattern);
                predicates.add(cb.or(matchFirst, matchLast));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        // Dùng PublicCourseMapper (tên DTO của bạn là CourseListDto)
        Page<CourseListDto> dtoPage = coursePage.map(publicCourseMapper::toCourseListDto);

        return new PagedResponse<>(
                dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
                dtoPage.getTotalElements(), dtoPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailDto getPublicCourseDetails(Long courseId, Authentication authentication) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // 1. Khóa học phải ACTIVE (của bạn)
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 2. Logic kiểm tra Student
        // (Chúng ta mặc định người gọi API này là Student,
        // vì Admin/Teacher sẽ gọi API riêng của họ)
        String studentEmail = authentication.getName();
        EnrollmentStatus status = enrollmentRepository.findEnrollmentStatus(studentEmail, courseId)
                .orElse(null); // (null nếu chưa đăng ký)

        if (status == EnrollmentStatus.APPROVED) {
            // Đã được duyệt -> Trả về DTO
            return publicCourseMapper.toCourseDetailDto(course);
        }

        // Nếu không (chưa đăng ký, PENDING, REJECTED) -> Báo lỗi
        throw new AppException(ErrorCode.ENROLLMENT_NOT_APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CourseListDto> searchCourses(String q, int page, int size, String... sort) {

        Pageable pageable = createPageable(page, size, sort, "createdOn");

        Specification<Course> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), CourseStatus.ACTIVE));

            if (q != null && !q.trim().isEmpty()) {
                String searchQuery = "%" + q.toLowerCase().trim() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchQuery);
                Predicate categoryMatch = cb.like(cb.lower(root.get("category").as(String.class)), searchQuery);
                Join<Course, User> authorJoin = root.join("author", JoinType.LEFT);
                Predicate firstNameMatch = cb.like(cb.lower(authorJoin.get("firstname")), searchQuery);
                Predicate lastNameMatch = cb.like(cb.lower(authorJoin.get("lastname")), searchQuery);
                Predicate authorMatch = cb.or(firstNameMatch, lastNameMatch);
                predicates.add(cb.or(titleMatch, categoryMatch, authorMatch));
                criteriaQuery.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        // Dùng PublicCourseMapper (tên DTO của bạn là CourseListDto)
        Page<CourseListDto> dtoPage = coursePage.map(publicCourseMapper::toCourseListDto);

        return new PagedResponse<>(
                dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
                dtoPage.getTotalElements(), dtoPage.getTotalPages()
        );
    }

    private Pageable createPageable(int page, int size, String[] sort, String defaultSortField) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        List<String> allowedFields = List.of("id", "title", "category", "createdOn"); // (Các trường được phép sort)
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