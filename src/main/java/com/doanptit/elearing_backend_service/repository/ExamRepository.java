package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
}
