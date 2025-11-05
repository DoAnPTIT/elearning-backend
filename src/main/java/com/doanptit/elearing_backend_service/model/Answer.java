package com.doanptit.elearing_backend_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private Boolean isCorrect = false;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}

