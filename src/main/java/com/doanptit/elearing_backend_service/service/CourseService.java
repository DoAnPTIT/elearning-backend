package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.PagedResponse;
import com.doanptit.elearing_backend_service.dto.req.CreateCourseRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateExamRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateLessonRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateSectionRequestDto;
import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.enums.CourseCategory;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface CourseService {

    CreateCourseResponse createCourse(CreateCourseRequestDto request, String teacherEmail);
    String uploadCourseImage(Long courseId, MultipartFile file, String teacherEmail);
    SectionResponse createSection(Long courseId, CreateSectionRequestDto request, String teacherEmail);
    LessonResponse createLesson(Long sectionId, CreateLessonRequestDto request, String teacherEmail);
    String uploadLessonVideo(Long lessonId, MultipartFile file, String teacherEmail);
    String uploadLessonDocument(Long lessonId, MultipartFile file, String teacherEmail);
    ExamResponse createExam(Long sectionId, CreateExamRequestDto request, String teacherEmail);
    void submitCourseForReview(Long courseId, String teacherEmail);
        void hideCourse(Long courseId, String teacherEmail);

    PagedResponse<AdminCourseListDto> getAllCoursesForTeacher(String teacherEmail, int page, int size, String... sort);
    AdminCourseDetailDto getCourseForEdit(Long courseId, String teacherEmail);

    PagedResponse<CourseListDto> getAllPublicCourses(
            int page, int size, CourseCategory category,
            String title, String authorName, String... sort
    );

    CourseDetailDto getPublicCourseDetails(Long courseId, Authentication authentication);

    PagedResponse<CourseListDto> searchCourses(
            String query, int page, int size, String... sort
    );
}