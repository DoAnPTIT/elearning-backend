package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminCourseMapper {
    @Mapping(target = "author", source = "author")
    @Mapping(target = "sectionCount", expression = "java(course.getSections() == null ? 0 : course.getSections().size())")
    AdminCourseListDto toCourseListDto(Course course);

    @Mapping(target = "author", source = "author")
    AdminCourseDetailDto toCourseDetailDto(Course course);
    AdminSectionDto toSectionDto(Section section);
    AdminLessonDto toLessonDto(Lesson lesson);
    AdminExamDto toExamDto(Exam exam);
    AdminQuestionDto toQuestionDto(Question question);
    AdminAnswerDto toAnswerDto(Answer answer);
    AdminAuthorDto toAuthorDto(User author);

    @Mapping(target = "enrollmentId", source = "id")
    @Mapping(target = "enrolledAt", source = "createdOn") // <-- THÊM DÒNG NÀY (Fix lỗi null)
    @Mapping(target = "studentId", source = "user.id")
    @Mapping(target = "studentEmail", source = "user.email")
    @Mapping(target = "studentFirstName", source = "user.firstname")
    @Mapping(target = "studentLastName", source = "user.lastname")
    EnrollmentStudentDto toEnrollmentStudentDto(Enrollment enrollment);
}