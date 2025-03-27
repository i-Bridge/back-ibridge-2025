package com.ibridge.repository;

import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    @Query("SELECT c FROM Child c WHERE c.family = :family ORDER BY c.birth ASC")
    List<Child> findAllByFamily(@Param("family") Family family);
}
