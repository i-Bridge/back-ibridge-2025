package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Child child;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private PeriodType type;

    @Column(nullable = false)
    private LocalDate period;

    @Column
    private Integer emotion;

    @Column(nullable = false)
    private Long answerCount;
}
