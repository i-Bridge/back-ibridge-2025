package com.ibridge.repository;

import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    @Query("SELECT s FROM Subject s where s.child.id = :childId and s.isCompleted = true")
    Page<Subject> findCompletedByChildId(Long childId, Pageable pageable);

    List<Subject> findByChildIdAndIsCompleted(Long childId, boolean isCompleted, Sort sort);
    Optional<Subject> findById(Long subjectId);

    @Query("SELECT s FROM Subject s WHERE s.child = :child")
    List<Subject> findAllByChild(Child child);

    @Query("""
    SELECT new com.ibridge.domain.dto.SubjectDTO(
        s.id, s.title, true, s.date
    )
    FROM Subject s
    WHERE s.child = :child
      AND s.keyword = :keyword
      AND s.date = (
          SELECT MAX(s2.date)
          FROM Subject s2
          WHERE s2.child = s.child
            AND s2.keyword = s.keyword
            AND s2.date IN (
                SELECT cpb.period
                FROM ChildPositiveBoard cpb
                WHERE cpb.child = s.child
                  AND cpb.keyword = s.keyword
                  AND cpb.period = (
                      SELECT MAX(cpb2.period)
                      FROM ChildPositiveBoard cpb2
                      WHERE cpb2.child = s.child
                        AND cpb2.keyword = s.keyword
                  )
            )
      )
    ORDER BY s.date DESC, s.id DESC
    """)
    List<SubjectDTO> findRecentSubjectDTOs(
            @Param("child") Child child,
            @Param("keyword") String keyword,
            Pageable pageable);


    @Query("SELECT count(s) FROM Subject s WHERE s.child = :child and s.isCompleted = true")
    int countByChild(Child child);

    @Query("SELECT s FROM Subject s WHERE s.child = :child and s.isCompleted = true ORDER BY s.date LIMIT :count")
    List<Subject> findClusteringSubjectbyChild(Child child, int count);

    @Query("SELECT s FROM Subject s WHERE s.child.id = :childId and s.date = :date")
    List<Subject> findByChildIdAndDate(Long childId, LocalDate date);

    @Query("SELECT new com.ibridge.domain.dto.SubjectDTO(s.id, s.title, s.isAnswer, s.date) " +
            "FROM Subject s WHERE s.child.id = :childId AND s.date >= :date and s.isCompleted = false")
    List<SubjectDTO> findSubjectsByChildIdAndDate(Long childId, LocalDate date);


    @Query("SELECT s FROM Subject s WHERE s.child.id = :childId AND s.isCompleted = true ORDER BY s.id ASC")
    List<Subject> findCompletedSubjectsByChildId(@Param("childId") Long childId);
    @Query("SELECT s FROM Subject s WHERE s.id = :subjectId")
    Subject findBySubjectId(Long subjectId);
}
