package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.CreateCourseRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateExamRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateLessonRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateSectionRequestDto;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.enums.LessonType;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.*;
import com.doanptit.elearing_backend_service.model.*;
import com.doanptit.elearing_backend_service.repository.*;
import com.doanptit.elearing_backend_service.service.CourseService;
import com.doanptit.elearing_backend_service.service.S3Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseCreationServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final SectionMapper  sectionMapper;
    private final LessonMapper lessonMapper;
    private final ExamMapper examMapper;
    private final CourseMapper courseMapper;
    private final AdminCourseMapper adminCourseMapper;

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

        Lesson lesson = lessonMapper.toEntity(request);

        if (request.getLessonType() == LessonType.ARTICLE) {
            lesson.setArticleContent(request.getArticleContent());
        }
        lesson.setSection(section);

        Lesson savedLesson = lessonRepository.save(lesson);

        return lessonMapper.toResponseDto(savedLesson);
    }

    @Override
    @Transactional
    public String uploadLessonVideo(Long lessonId, MultipartFile file, String teacherEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        checkCourseAuthorship(lesson.getSection().getCourse(), teacherEmail);

        if (lesson.getLessonType() != LessonType.VIDEO) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

        String videoUrl = s3Service.uploadFile(file, "lesson-videos");
        lesson.setVideoUrl(videoUrl);
        lessonRepository.save(lesson);
        return videoUrl;
    }

    @Override
    @Transactional
    public ExamResponse createExam(Long sectionId, CreateExamRequestDto request, String teacherEmail) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));
        checkCourseAuthorship(section.getCourse(), teacherEmail);

        // 1. Tạo Exam chính (dùng mapper cho các trường cơ bản)
        Exam exam = examMapper.toEntity(request);
        exam.setSection(section);

        // 2. Map thủ công Question và Answer để đảm bảo quan hệ 2 chiều
        List<Question> questions = new ArrayList<>();
        if (request.getQuestions() != null) {
            for (var questionDto : request.getQuestions()) {
                Question question = new Question();
                question.setContent(questionDto.getContent());
                question.setQuestionType(questionDto.getQuestionType());
                question.setPoint(questionDto.getPoint());
                question.setExam(exam); // Quan hệ về Exam

                List<Answer> answers = new ArrayList<>();
                if (questionDto.getAnswers() != null) {
                    for (var answerDto : questionDto.getAnswers()) {
                        Answer answer = new Answer();
                        answer.setContent(answerDto.getContent());
                        answer.setIsCorrect(answerDto.getIsCorrect());
                        answer.setQuestion(question); // Quan hệ về Question
                        answers.add(answer);
                    }
                }
                question.setAnswers(answers);
                questions.add(question);
            }
        }
        exam.setQuestions(questions);

        // 3. Lưu (Cascade sẽ lưu cả Question và Answer)
        Exam savedExam = examRepository.save(exam);

        // 4. Trả về DTO (dùng mapper)
        return examMapper.toResponseDto(savedExam);
    }

    @Override
    @Transactional
    public void submitCourseForReview(Long courseId, String teacherEmail) {
        Course course = findCourseByIdAndAuthor(courseId, teacherEmail);

        // 1. Kiểm tra trạng thái
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_REVIEW);
        }

        // 2. Kiểm tra ảnh bìa (Yêu cầu mới)
        if (course.getImage() == null || course.getImage().isEmpty()) {
            throw new AppException(ErrorCode.COURSE_MISSING_COVER_IMAGE);
        }

        // 3. Kiểm tra nội dung (Yêu cầu cũ)
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

        // 4. Cập nhật trạng thái
        course.setStatus(CourseStatus.PENDING_APPROVAL);
        courseRepository.save(course);
    }

    private Course findCourseByIdAndAuthor(Long courseId, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        checkCourseAuthorship(course, teacherEmail);
        return course;
    }

    private void checkCourseAuthorship(Course course, String teacherEmail) {
        if (!course.getAuthor().getEmail().equals(teacherEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminCourseListDto> getAllCoursesForTeacher(String teacherEmail, int page, int size, String... sort) {

        // 1. Logic Sort (Phiên bản an toàn, copy từ Admin)
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null && sort.length > 0) {
            for (String sortOrder : sort) {
                if (sortOrder == null || sortOrder.trim().isEmpty()) continue;
                String[] parts = sortOrder.split(",");
                String field = parts[0].trim();
                if (field.isEmpty() || field.equalsIgnoreCase("asc") || field.equalsIgnoreCase("desc")) continue;

                if (parts.length == 2 && parts[1] != null && !parts[1].trim().isEmpty()) {
                    Sort.Direction direction = parts[1].trim().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    orders.add(new Sort.Order(direction, field));
                } else {
                    orders.add(new Sort.Order(Sort.Direction.ASC, field));
                }
            }
        }
        if (orders.isEmpty()) {
            orders.add(new Sort.Order(Sort.Direction.ASC, "id"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        // 2. Gọi Repository (Không cần filter status)
        Page<Course> coursePage = courseRepository.findByAuthor_Email(teacherEmail, pageable);

        // 3. Map sang DTO
        Page<AdminCourseListDto> dtoPage = coursePage.map(adminCourseMapper::toCourseListDto);

        // 4. Chuyển sang PagedResponse
        return new PagedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCourseDetailDto getCourseForEdit(Long courseId, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkCourseAuthorship(course, teacherEmail); // (Hàm helper cũ của bạn)

        return adminCourseMapper.toCourseDetailDto(course);
    }
}
