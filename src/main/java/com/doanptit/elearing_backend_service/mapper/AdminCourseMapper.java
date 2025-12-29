package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.model.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdminCourseMapper {
    @Mapping(target = "author", source = "author")
    @Mapping(target = "sectionCount", expression = "java(course.getSections() == null ? 0 : course.getSections().size())")
    @Mapping(target = "totalStudents", expression = "java(course.getEnrollments() == null ? 0 : (int) course.getEnrollments().stream().filter(e -> e.getStatus() == com.doanptit.elearing_backend_service.enums.EnrollmentStatus.APPROVED).count())")
    @Mapping(target = "updatedAt", source = "updatedOn")
    @Mapping(target = "totalDuration", ignore = true)
    @Mapping(target = "totalLessons", ignore = true)
    AdminCourseListDto toCourseListDto(Course course);

    @Mapping(target = "author", source = "author")
    @Mapping(target = "totalDuration", ignore = true)
    @Mapping(target = "totalLessons", ignore = true)
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

    @AfterMapping
    default void calculateCourseDetailTotals(@MappingTarget AdminCourseDetailDto dto, Course course) {
        if (course.getSections() != null && !course.getSections().isEmpty()) {
            long totalDurationInSeconds = 0;
            int totalLessons = 0;
            
            for (Section section : course.getSections()) {
                if (section.getLessons() != null) {
                    totalLessons += section.getLessons().size();
                    for (Lesson lesson : section.getLessons()) {
                        if (lesson.getDuration() != null) {
                            totalDurationInSeconds += lesson.getDuration();
                        }
                    }
                }
            }
            
            // Convert seconds to minutes (rounded up)
            long totalDurationInMinutes = (totalDurationInSeconds + 59) / 60;
            dto.setTotalDuration(totalDurationInMinutes);
            dto.setTotalLessons(totalLessons);
        } else {
            dto.setTotalDuration(0L);
            dto.setTotalLessons(0);
        }
    }

    @AfterMapping
    default void calculateCourseListTotals(@MappingTarget AdminCourseListDto dto, Course course) {
        if (course.getSections() != null && !course.getSections().isEmpty()) {
            long totalDurationInSeconds = 0;
            int totalLessons = 0;
            
            for (Section section : course.getSections()) {
                if (section.getLessons() != null) {
                    totalLessons += section.getLessons().size();
                    for (Lesson lesson : section.getLessons()) {
                        if (lesson.getDuration() != null) {
                            totalDurationInSeconds += lesson.getDuration();
                        }
                    }
                }
            }
            
            // Convert seconds to minutes (rounded up)
            long totalDurationInMinutes = (totalDurationInSeconds + 59) / 60;
            dto.setTotalDuration(totalDurationInMinutes);
            dto.setTotalLessons(totalLessons);
        } else {
            dto.setTotalDuration(0L);
            dto.setTotalLessons(0);
        }
    }
}