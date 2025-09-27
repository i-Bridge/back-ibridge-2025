package com.ibridge.repository;

import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.ChildStat;
import com.ibridge.domain.entity.PeriodType;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChildStatRepository extends JpaRepository<ChildStat, Long> {
    @Query("SELECT cs FROM ChildStat cs WHERE cs.child = :child and cs.type = 0 and cs.period = :today")
    ChildStat findDateStatByChildandToday(@Param("child") Child child, @Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(cs.answerCount), 0) FROM ChildStat cs WHERE cs.child = :child AND cs.type = :periodType")
    Long findSumByChildAndType(@Param("child") Child child, @Param("periodType") PeriodType periodType);

    @Query("SELECT cs " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child " +
            "AND cs.type = 0 " +
            "AND cs.period BETWEEN :start and :end " +
            "ORDER BY cs.period")
    List<ChildStat> findEmotionsByChildAndMonth(@Param("child") Child child, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT cs.answerCount " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child " +
            "AND cs.period IN :periodList " +
            "ORDER BY cs.period ASC")
    List<Long> findAnswerCountsByChildAndPeriodList(@Param("child") Child child, @Param("periodList") List<LocalDate> periodList);

    @Query("SELECT cs " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child " +
            "AND cs.type = 2 " +
            "AND cs.period = :today ")
    ChildStat findMonthStatByChildandToday(@Param("child") Child child, @Param("today") LocalDate today);

    @Query("SELECT cs " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child " +
            "AND cs.type = 1 " +
            "AND cs.period = :monday ")
    ChildStat findWeekStatByChildandToday(@Param("child") Child child, @Param("monday") LocalDate monday);

    @Query("SELECT cs FROM ChildStat cs WHERE cs.child = :child AND cs.type = 4")
    ChildStat findTotalStatByChild(Child child);
  
    @Query("SELECT cs FROM ChildStat cs WHERE cs.child = :child AND cs.type = com.ibridge.domain.entity.PeriodType.CUMULATIVE")
    Optional<ChildStat> findByType(@Param("child") Child child);

    @Query("SELECT cs FROM ChildStat cs WHERE cs.child = :child")
    List<ChildStat> findAllByChild(Child child);

    List<ChildStat> findByChildAndPeriodBetween(Child child, LocalDate start, LocalDate end);

    @Query("select cs from ChildStat cs where cs.child = :child AND cs.period = :period AND cs.type = com.ibridge.domain.entity.PeriodType.DAY")
    ChildStat findByChildAndPeriod(@Param("child") Child child, @Param("period") LocalDate period);
}
