package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.req.CreateCourseRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateExamRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateLessonRequestDto;
import com.doanptit.elearing_backend_service.dto.req.CreateSectionRequestDto;
import com.doanptit.elearing_backend_service.dto.res.CreateCourseResponse;
import com.doanptit.elearing_backend_service.dto.res.ExamResponse;
import com.doanptit.elearing_backend_service.dto.res.LessonResponse;
import com.doanptit.elearing_backend_service.dto.res.SectionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CourseService {
    // Bước 1: Tạo khóa học
    CreateCourseResponse createCourse(CreateCourseRequestDto request, String teacherEmail);

    // Upload ảnh bìa cho khóa học
    String uploadCourseImage(Long courseId, MultipartFile file, String teacherEmail);

    // Bước 2: Tạo chương
    SectionResponse createSection(Long courseId, CreateSectionRequestDto request, String teacherEmail);

    // Bước 3: Tạo bài giảng
    LessonResponse createLesson(Long sectionId, CreateLessonRequestDto request, String teacherEmail);

    // Upload video cho bài giảng
    String uploadLessonVideo(Long lessonId, MultipartFile file, String teacherEmail);

    // Bước 4: Tạo bài kiểm tra
    ExamResponse createExam(Long sectionId, CreateExamRequestDto request, String teacherEmail);

    // Bước 5: Gửi duyệt
    void submitCourseForReview(Long courseId, String teacherEmail);
}
