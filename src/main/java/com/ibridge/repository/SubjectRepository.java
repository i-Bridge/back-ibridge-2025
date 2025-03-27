package com.ibridge.repository;

import com.ibridge.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByChildId(Long childId);
    Subject findOneByChildId(Long childId);

}
