package com.ibridge.repository;

import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.subject.id = :subjectId")
    List<Question> findBySubjectId(@Param("subjectId") Long subjectId);
    @Query("SELECT q FROM Question q WHERE q.id = :questionId AND q.subject.id = :subjectId")
    Optional<Question> findByIdAndSubjectId(@Param("questionId") Long questionId,
                                                      @Param("subjectId") Long subjectId);
}