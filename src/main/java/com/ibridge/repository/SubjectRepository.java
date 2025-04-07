package com.ibridge.repository;

import com.ibridge.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    @Query("SELECT s FROM Subject s where s.child.id = :childId and s.date = :date")
    List<Subject> findByChildIdAndDate(Long childId, LocalDate date);
    void deleteByChildIdAndDate(Long childId, LocalDate date);
}
