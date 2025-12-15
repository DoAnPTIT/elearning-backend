package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Lấy comment gốc (parent = null) của một BÀI HỌC (Lesson)
    @Query("SELECT c FROM Comment c WHERE c.lesson.id = :lessonId AND c.parentComment IS NULL AND c.deletedOn IS NULL")
    Page<Comment> findRootCommentsByLessonId(Long lessonId, Pageable pageable);
}