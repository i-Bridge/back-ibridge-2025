package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Timestamp time;

    @ManyToOne
    @JoinColumn(name = "parent")
    private Parent parent;

    @ManyToOne
    @JoinColumn(name = "child")
    private Child child;

    @OneToOne
    private Analysis analysis;
}
