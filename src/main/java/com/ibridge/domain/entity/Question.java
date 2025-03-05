package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
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
    private String time;

    @ManyToOne
    @JoinColumn(name = "child")
    private Child child;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Analysis analysis;

    @Column(nullable = false)
    private boolean isAnswer;

    @Column(nullable = false)
    private int type;

    @Column(nullable = false)
    private Timestamp date;

    @Column(nullable = true)
    private int period;
}
