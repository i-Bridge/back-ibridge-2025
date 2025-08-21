package com.ibridge.repository;

import com.ibridge.domain.dto.response.AnalysisResponseDTO;
import com.ibridge.domain.dto.response.KeywordDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.ChildPositiveBoard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChildPositiveBoardRepository extends JpaRepository<ChildPositiveBoard, Long> {
    @Query("SELECT new com.ibridge.domain.dto.response.KeywordDTO(cb.keyword, cb.keywordCount,cb.positive) " +
            "FROM ChildPositiveBoard cb " +
            "WHERE cb.child = :child AND cb.type = com.ibridge.domain.entity.PeriodType.MONTH " +
            "AND cb.period = :periodValue " +
            "ORDER BY cb.keywordCount DESC")
    List<KeywordDTO> findKeywordsAndPositivesByMonth(
            @Param("child") Child child,
            Pageable pageable);

    @Query("SELECT cb " +
            "FROM ChildPositiveBoard cb " +
            "WHERE cb.child = :child AND cb.type = :periodType AND cb.period = :periodValue " +
            "ORDER BY cb.period DESC")
    List<ChildPositiveBoard> findByChildAndTypeAndPeriod(Child child, String periodType, String periodValue, Pageable pageable);
}
