package com.ibridge.repository;

import com.ibridge.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Long saveQuestion(Long parentId, String question);
}