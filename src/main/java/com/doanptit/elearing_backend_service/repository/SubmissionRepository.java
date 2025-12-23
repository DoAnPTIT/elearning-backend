package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}


