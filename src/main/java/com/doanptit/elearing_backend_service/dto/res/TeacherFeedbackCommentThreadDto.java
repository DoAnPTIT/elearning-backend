package com.doanptit.elearing_backend_service.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherFeedbackCommentThreadDto {
    private Long courseId;
    private String courseTitle;
    private Long lessonId;
    private String lessonTitle;
    private CommentResponseDto comment;
}


