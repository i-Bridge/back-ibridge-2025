package com.ibridge.repository;

import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.domain.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q join Subject s on q.subject.id = s.id WHERE s.child.id = :childId ORDER BY s.date")
    List<Question> findBySubjectIdAndChildId(@Param("subjectId") Long subjectId, @Param("childId") Long childId);

    @Query("SELECT q FROM Question q WHERE q.id = :questionId AND q.subject.child.id = :childId AND q.subject.id = :subjectId")
    Optional<Question> findByIdAndChildIdAndSubjectId(@Param("questionId") Long questionId,
                                                      @Param("childId") Long childId,
                                                      @Param("subjectId") Long subjectId);
    Question findBySubjectId(Long subjectId);

    @Query("SELECT q FROM Question q WHERE q.subject = :subject")
    List<Question> findAllBySubject(Subject subject);
}