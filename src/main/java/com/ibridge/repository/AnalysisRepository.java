package com.ibridge.repository;

import com.ibridge.domain.dto.response.QuestionAnalysisDTO;
import com.ibridge.domain.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    QuestionAnalysisDTO findAnalysisByQuestionId(Long parentId, Long questionId);
}
