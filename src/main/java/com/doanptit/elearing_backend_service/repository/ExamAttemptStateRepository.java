package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.ExamAttemptState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamAttemptStateRepository extends JpaRepository<ExamAttemptState, Long> {
    Optional<ExamAttemptState> findByUser_EmailAndExam_Id(String email, Long examId);
}


