package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.LessonNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonNoteRepository extends JpaRepository<LessonNote, Long> {
    Optional<LessonNote> findByUser_EmailAndLesson_Id(String email, Long lessonId);
}


