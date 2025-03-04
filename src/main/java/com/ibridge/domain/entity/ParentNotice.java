package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(nullable = false)
    @ColumnDefault("false")
    boolean isAccept;

    @ManyToOne(fetch = FetchType.LAZY)
    Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    Notice notice;
}
