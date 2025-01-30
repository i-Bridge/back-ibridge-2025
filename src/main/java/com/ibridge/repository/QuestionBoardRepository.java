package com.ibridge.repository;

import com.ibridge.domain.dto.response.QuestionBoardResponseDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionBoardRepository {
    QuestionBoardResponseDTO findQuestionByParentId(Long parentid, int page);
}
