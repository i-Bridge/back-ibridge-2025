package com.ibridge.repository;

import com.ibridge.domain.dto.response.QuestionAnalysisDTO;
import com.ibridge.domain.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    @Query("SELECT a FROM Analysis a WHERE a.question.id = :questionId")
    Optional<Analysis> findByQuestionId(@Param("questionId") Long questionId);
}
