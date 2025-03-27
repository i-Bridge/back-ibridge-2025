package com.ibridge.repository;

import com.ibridge.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByChildId(Long childId);
    @Query("SELECT s FROM Subject s where s.child.id = :childId")
    Subject findSubjectByChildId(Long childId);
}
