package com.doanptit.elearing_backend_service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CourseContentUpdatedEvent extends ApplicationEvent {
    private final Long courseId;

    public CourseContentUpdatedEvent(Object source, Long courseId) {
        super(source);
        this.courseId = courseId;
    }
}