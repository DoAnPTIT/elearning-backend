package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.req.CreateCourseRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateExamRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateLessonRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateSectionRequestDto;
import com.doanptit.elearing_backend_service.dto.res.SectionResponse;
import com.doanptit.elearing_backend_service.enums.CourseStatus;
import com.doanptit.elearing_backend_service.enums.LessonType;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.SectionMapper;
import com.doanptit.elearing_backend_service.model.*;
import com.doanptit.elearing_backend_service.repository.*;
import com.doanptit.elearing_backend_service.service.CourseService;
import com.doanptit.elearing_backend_service.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    @Transactional
    public Course createCourse(CreateCourseRequestDto request, String teacherEmail) {
        User author = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setObjectives(request.getObjectives());
        course.setTargetAudience(request.getTargetAudience());
        course.setCategory(request.getCategory());
        course.setAuthor(author);
        course.setStatus(CourseStatus.DRAFT);

        return courseRepository.save(course);
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

        Section section = new Section();
        section.setTitle(request.getTitle());
        section.setOrder(request.getSectionOrder());
        section.setCourse(course);

        sectionRepository.save(section);
        return sectionMapper.toSectionResponseDto(section);
    }

    @Override
    @Transactional
    public Lesson createLesson(Long sectionId, CreateLessonRequestDto request, String teacherEmail) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));
        checkCourseAuthorship(section.getCourse(), teacherEmail);

        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setLessonType(request.getLessonType());
        if (request.getLessonType() == LessonType.ARTICLE) {
            lesson.setArticleContent(request.getArticleContent());
        }
        lesson.setOrder(request.getLessonOrder());
        lesson.setSection(section);

        return lessonRepository.save(lesson);
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
    public Exam createExam(Long sectionId, CreateExamRequestDto request, String teacherEmail) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));
        checkCourseAuthorship(section.getCourse(), teacherEmail);

        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setExamType(request.getExamType());
        exam.setSection(section);

        // TODO: Xử lý lưu các câu hỏi (questions) và câu trả lời (answers) từ DTO

        return examRepository.save(exam);
    }

    @Override
    @Transactional
    public void submitCourseForReview(Long courseId, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkCourseAuthorship(course, teacherEmail);

        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

        List<Section> sections = course.getSections();

        if (sections == null || sections.isEmpty()) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
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
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
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
}
