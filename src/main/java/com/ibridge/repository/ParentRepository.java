package com.ibridge.repository;

import com.ibridge.domain.entity.Family;
import com.ibridge.domain.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface ParentRepository extends JpaRepository<Parent, Long> {

    @Query("SELECT COUNT(p) FROM Parent p WHERE p.family = :family")
    int getParentCount(@Param("family") Family family);

    boolean existsByEmail(String email);
    @Query
    Parent findParentByEmail(String email);

    @Query("SELECT p FROM Parent p WHERE p.family = :family")
    List<Parent> findAllByFamily(Family family);
}
