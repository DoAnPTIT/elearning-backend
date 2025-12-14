package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.res.*;
import com.doanptit.elearing_backend_service.enums.ExamType;
import com.doanptit.elearing_backend_service.enums.LessonType;
import com.doanptit.elearing_backend_service.model.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PublicCourseMapper {
    CourseListDto toCourseListDto(Course course);
    CourseDetailDto toCourseDetailDto(Course course);
    SectionDto toSectionDto(Section section);
    LessonDto toLessonDto(Lesson lesson);
    AuthorDto toAuthorDto(User author);

    @AfterMapping
    default void includeExamsInLessons(Section section, @MappingTarget SectionDto dto) {
        if (section.getExams() == null || section.getExams().isEmpty()) {
            return;
        }

        List<LessonDto> mergedLessons = dto.getLessons() != null
                ? new ArrayList<>(dto.getLessons())
                : new ArrayList<>();
        int baseOrder = mergedLessons.size();

        for (int i = 0; i < section.getExams().size(); i++) {
            Exam exam = section.getExams().get(i);
            LessonDto examLesson = new LessonDto();
            examLesson.setId(exam.getId());
            examLesson.setTitle(exam.getTitle());
            examLesson.setLessonType(exam.getExamType() == ExamType.QUIZ
                    ? LessonType.QUIZ
                    : LessonType.ASSIGNMENT);
            examLesson.setArticleContent(exam.getDescription());
            examLesson.setExamQuestions(mapExamQuestions(exam.getQuestions()));
            examLesson.setOrder(baseOrder + i + 1);
            mergedLessons.add(examLesson);
        }

        dto.setLessons(mergedLessons);
    }

    default List<ExamQuestionDto> mapExamQuestions(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExamQuestionDto> questionDtos = new ArrayList<>(questions.size());
        for (Question question : questions) {
            if (question == null) {
                continue;
            }
            ExamQuestionDto questionDto = new ExamQuestionDto();
            questionDto.setId(question.getId());
            questionDto.setContent(question.getContent());
            questionDto.setPoint(question.getPoint());
            questionDto.setAnswers(mapExamAnswers(question.getAnswers()));
            questionDtos.add(questionDto);
        }
        return questionDtos;
    }

    default List<ExamAnswerOptionDto> mapExamAnswers(List<Answer> answers) {
        if (answers == null || answers.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExamAnswerOptionDto> answerDtos = new ArrayList<>(answers.size());
        for (Answer answer : answers) {
            if (answer == null) {
                continue;
            }
            ExamAnswerOptionDto answerDto = new ExamAnswerOptionDto();
            answerDto.setId(answer.getId());
            answerDto.setContent(answer.getContent());
            answerDto.setIsCorrect(Boolean.TRUE.equals(answer.getIsCorrect()));
            answerDtos.add(answerDto);
        }
        return answerDtos;
    }
    
    @AfterMapping
    default void calculateCourseDetailTotals(@MappingTarget CourseDetailDto dto, Course course) {
        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
            long totalDurationInSeconds = 0;
            int totalLessons = 0;
            
            for (SectionDto section : dto.getSections()) {
                if (section.getLessons() != null) {
                    totalLessons += section.getLessons().size();
                    for (LessonDto lesson : section.getLessons()) {
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
    default void calculateCourseListTotals(@MappingTarget CourseListDto dto, Course course) {
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