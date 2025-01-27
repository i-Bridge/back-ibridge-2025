package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String answer;

    @Column
    private String video;

    @Column
    private Timestamp date;

    @ManyToOne
    @JoinColumn(name = "child")
    private Child child;

    @OneToOne
    @JoinColumn(name = "question")
    private Question question;
}
