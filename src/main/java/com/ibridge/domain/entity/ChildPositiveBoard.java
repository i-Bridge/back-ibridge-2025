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
public class ChildPositiveBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Child child;

    @Column(nullable = false)
    private LocalDate period;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private Long keywordCount;

    @Column(nullable = false)
    private Long positive;
}
