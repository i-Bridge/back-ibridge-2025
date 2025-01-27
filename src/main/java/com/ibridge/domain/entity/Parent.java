package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private Date birth;

    @Column
    private Relation relation;

    @ManyToOne
    @JoinColumn(name = "account")
    private Account account;

    @OneToMany(mappedBy = "parent")
    private List<Question> questionList;
}
