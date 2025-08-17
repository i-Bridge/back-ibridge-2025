package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private PeriodType type;

    @Column(nullable = false)
    private String period;

    @Column(nullable = false)
    private Emotion emotion;

    @Column(nullable = false)
    private Long answerCount;
}
