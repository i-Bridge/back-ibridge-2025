package com.ibridge.repository;

import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Subject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    @Query("SELECT s FROM Subject s where s.child.id = :childId and s.date = :date ORDER BY s.id")
    List<Subject> findByChildIdAndDate(Long childId, LocalDate date);

    Optional<Subject> findById(Long subjectId);

    @Query("SELECT s FROM Subject s WHERE s.child = :child")
    List<Subject> findAllByChild(Child child);

    @Query("SELECT new com.ibridge.domain.dto.SubjectDTO(s.id, s.title, true) " +
            "FROM Subject s " +
            "WHERE s.child = :child AND s.keyword = :keyword " +
            "ORDER BY s.date DESC, s.id DESC")
    List<SubjectDTO> findRecentSubjectDTOs(@Param("child") Child child,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);


    @Query("SELECT count(s) FROM Subject s WHERE s.child = :child and s.isCompleted = true")
    int countByChild(Child child);

    @Query("SELECT s FROM Subject s WHERE s.child = :child and s.isCompleted = true ORDER BY s.date LIMIT :count")
    List<Subject> findClusteringSubjectbyChild(Child child, int count);
}
