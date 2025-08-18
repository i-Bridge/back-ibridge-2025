package com.ibridge.repository;

import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.ChildStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Optional;

public interface ChildStatRepository extends JpaRepository<ChildStat, Long> {
    @Query("SELECT cs FROM ChildStat cs WHERE cs.child = :child and cs.type = 0 and cs.period =: today")
    Optional<ChildStat> findByChildToday(@Param("child") Child child, @Param("today") String today);
}
