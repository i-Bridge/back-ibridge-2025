package com.ibridge.repository;

import com.ibridge.domain.dto.response.QuestionListResponseDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Long saveQuestion(Long parentId, String question);
    void updateQuestion(Long parentId, Long questionId, String question);
    List<QuestionListResponseDTO.QuestionDTO> findQuestionsByParentId(Long parentId);
    void deleteQuestion(Long parentId, Long questionId);

    // 특정 자녀의 해당 월 모든 질문 조회
    @Query("SELECT q FROM Question q WHERE q.child = :child " +
            "AND YEAR(q.time) = :year " +
            "AND MONTH(q.time) = :month")
    List<Question> findByChildAndMonth(Child child, int year, int month);

    // 특정 자녀의 특정 날짜 질문 조회
    @Query("SELECT q FROM Question q WHERE q.child = :child " +
            "AND DATE(q.time) = :date")
    List<Question> findByChildAndDate(Child child, LocalDate date);
}