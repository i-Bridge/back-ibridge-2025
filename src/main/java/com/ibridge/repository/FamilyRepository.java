package com.ibridge.repository;

import com.ibridge.domain.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    @Query("SELECT f FROM Family f WHERE TRIM(f.name) =TRIM(:name)")
    Optional<Family> findByName(String name);
}
