package com.ibridge.repository;

import com.ibridge.domain.dto.response.QuestionAnalysisDTO;
import com.ibridge.domain.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    QuestionAnalysisDTO findAnalysisByQuestionId(Long parentId, Long questionId);
    Optional<Analysis> findByChildIdAndQuestionId(Long childId, Long questionId);
}
