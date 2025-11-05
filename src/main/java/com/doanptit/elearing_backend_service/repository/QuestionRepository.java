package com.doanptit.elearing_backend_service.repository;

import com.doanptit.elearing_backend_service.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
}
