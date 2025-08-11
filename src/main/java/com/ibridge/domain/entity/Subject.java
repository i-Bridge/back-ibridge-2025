package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isAnswer;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isCompleted;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "child")
    private Child child;

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL)
    private List<Question> questions;
}
