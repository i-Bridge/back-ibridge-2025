package com.ibridge.repository;

import com.ibridge.domain.dto.response.QuestionListResponseDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Long saveQuestion(Long parentId, String question);
    void updateQuestion(Long parentId, Long questionId, String question);
    List<QuestionListResponseDTO.QuestionDTO> findQuestionsByParentId(Long parentId);
    void deleteQuestion(Long parentId, Long questionId);
}