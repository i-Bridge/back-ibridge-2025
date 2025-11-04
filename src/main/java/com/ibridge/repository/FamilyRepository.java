package com.ibridge.repository;

import com.ibridge.domain.entity.Family;
import com.ibridge.domain.entity.Parent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    @Query("SELECT f FROM Family f WHERE TRIM(f.name) =TRIM(:name)")
    Optional<Family> findByName(String name);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Family f " +
            "JOIN f.parents p " +
            "WHERE p = :parent")
    boolean existsFamilyByParentsContaining(@Param("parent") Parent parent);

    @Query("SELECT f FROM Family f JOIN f.parents p WHERE p IN :parents")
    Family findFamilyByParents(@Param("parents") List<Parent> parents);
}
