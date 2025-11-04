package com.ibridge.repository;

import com.ibridge.domain.dto.response.AnalysisResponseDTO;
import com.ibridge.domain.dto.response.KeywordDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.ChildPositiveBoard;
import com.ibridge.domain.entity.PeriodType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChildPositiveBoardRepository extends JpaRepository<ChildPositiveBoard, Long> {
//    @Query("SELECT new com.ibridge.domain.dto.response.KeywordDTO(cb.keyword, cb.keywordCount,cb.positive) " +
//            "FROM ChildPositiveBoard cb " +
//            "WHERE cb.child = :child AND cb.type = com.ibridge.domain.entity.PeriodType.MONTH " +
//            "AND cb.period = :periodValue " +
//            "ORDER BY cb.keywordCount DESC")
//    List<KeywordDTO> findKeywordsAndPositivesByMonth(
//            @Param("child") Child child,
//            Pageable pageable);
//
//    @Query("SELECT cb " +
//            "FROM ChildPositiveBoard cb " +
//            "WHERE cb.child = :child AND cb.type = :periodType AND cb.period = :periodValue " +
//            "ORDER BY cb.period DESC")
//    List<ChildPositiveBoard> findByChildAndTypeAndPeriod(Child child, PeriodType periodType, String periodValue, Pageable pageable);
//
//    @Query("SELECT cb " +
//            "FROM ChildPositiveBoard cb " +
//            "WHERE cb.child = :child " +
//            "AND cb.type = :periodType " +
//            "AND cb.period = :period " +
//            "AND cb.keyword = :keyword")
//    Optional<ChildPositiveBoard> findByKeywordandChildwithDatewithType(String keyword, Child child, String period, PeriodType periodType);

    @Query("SELECT new com.ibridge.domain.dto.response.KeywordDTO(c.keyword, c.keywordCount, c.positive) " +
            "FROM ChildPositiveBoard c " +
            "WHERE c.child.id = :childId " +
            "AND c.period = (" +
            "   SELECT MAX(c2.period) " +
            "   FROM ChildPositiveBoard c2 " +
            "   WHERE c2.child.id = :childId" +
            ")")
    List<KeywordDTO> findKeywordsByChildId(@Param("childId") Long childId);

    @Query("SELECT cp FROM ChildPositiveBoard cp WHERE cp.child = :child")
    List<ChildPositiveBoard> findAllByChild(Child child);

    @Query("SELECT cp FROM ChildPositiveBoard cp WHERE cp.child = :child ORDER BY cp.keywordCount LIMIT 1")
    ChildPositiveBoard findTopByChild(Child child);

    @Query("SELECT cpb FROM ChildPositiveBoard cpb " +
            "WHERE cpb.child = :child " +
            "AND cpb.period BETWEEN :startDate AND :endDate " +
            "ORDER BY cpb.period ASC")
    List<ChildPositiveBoard> findByChildAndPeriodBetween(
            @Param("child") Child child,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT cp FROM ChildPositiveBoard cp WHERE cp.child = :child and cp.keyword = :keyword and cp.period = :today")
    Optional<ChildPositiveBoard> findByChildAndKeyword(Child child, String keyword, LocalDate today);

    @Query("SELECT c FROM ChildPositiveBoard c WHERE c.child = :child ORDER BY c.period DESC LIMIT 1")
    ChildPositiveBoard findTopByChildOrderByPeriodDesc(@Param("child") Child child);
}
