package com.ibridge.repository;

import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 특정 자녀의 해당 월 모든 질문 조회
    @Query("SELECT q FROM Question q WHERE q.child = :child " +
            "AND YEAR(q.time) = :year " +
            "AND MONTH(q.time) = :month")
    List<Question> findByChildAndMonth(Child child, int year, int month);

    // 특정 자녀의 특정 날짜 질문 조회
    @Query("SELECT q FROM Question q WHERE q.child = :child " +
            "AND DATE(q.time) = :date")
    List<Question> findByChildAndDate(Child child, LocalDate date);
    Optional<Question> findByIdAndChild_Id(Long questionId, Long childId);
    List<Question> findByChildId(Long childId);
    Page<Question> findAllByChildId(Long childId, Pageable pageable);
}