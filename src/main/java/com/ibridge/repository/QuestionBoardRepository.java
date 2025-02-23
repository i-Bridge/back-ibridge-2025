package com.ibridge.repository;

import com.ibridge.domain.dto.response.QuestionBoardResponseDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionBoardRepository extends JpaRepository<Question, Long> {
    QuestionBoardResponseDTO findQuestionByParentId(Long parentid, int page);
}
