package com.ibridge.repository;

import com.ibridge.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    @Query("SELECT s FROM Subject s where s.child.id = :childId and s.date = :date ORDER BY s.id")
    List<Subject> findByChildIdAndDate(Long childId, LocalDate date);

    Optional<Subject> findById(Long subjectId);
    void deleteByChildIdAndDate(Long childId, LocalDate date);
}
