package com.ibridge.repository;

import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.ChildStat;
import com.ibridge.domain.entity.Emotion;
import com.ibridge.domain.entity.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface ChildStatRepository extends JpaRepository<ChildStat, Long> {
    @Query("SELECT cs FROM ChildStat cs WHERE cs.child = :child and cs.type = 0 and cs.period =: today")
    Optional<ChildStat> findDateStatByChildandToday(@Param("child") Child child, @Param("today") String today);

    @Query("SELECT COALESCE(SUM(cs.answerCount), 0) FROM ChildStat cs WHERE cs.child = :child AND cs.type = :periodType")
    Long findSumByChildAndType(@Param("child") Child child, @Param("periodType") PeriodType periodType);

    @Query("SELECT DISTINCT cs.emotion " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child " +
            "AND cs.type = com.ibridge.domain.entity.PeriodType.DAY " +
            "AND cs.period LIKE CONCAT(:month, '%')" +
            "ORDER BY cs.period")
    List<Emotion> findEmotionsByChildAndMonth(@Param("child") Child child, @Param("month") String month);

    @Query("SELECT cs.answerCount " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child AND cs.type = :periodType " +
            "ORDER BY cs.period ASC")
    List<Long> findAnswerCountsByChildAndType(@Param("child") Child child, @Param("periodType") PeriodType periodType);

    @Query("SELECT cs.answerCount " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child " +
            "AND cs.type = :periodType " +
            "AND cs.period IN :periodList " +
            "ORDER BY cs.period ASC")
    List<Long> findAnswerCountsByChildAndPeriodList(@Param("child") Child child, @Param("periodType") PeriodType periodType, @Param("periodList") List<String> periodList);

    @Query("SELECT cs " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child " +
            "AND cs.type = 2 " +
            "AND cs.period = :today ")
    Optional<ChildStat> findMonthStatByChildandToday(@Param("child") Child child, @Param("today") String today);

    @Query("SELECT cs " +
            "FROM ChildStat cs " +
            "WHERE cs.child = :child " +
            "AND cs.type = 1 " +
            "AND cs.period = :monday ")
    Optional<ChildStat> findWeekStatByChildandToday(@Param("child") Child child, @Param("monday") String monday);

}
