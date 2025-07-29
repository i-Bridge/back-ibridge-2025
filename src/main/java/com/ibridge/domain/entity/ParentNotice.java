package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Timestamp;
import java.util.Date;

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

    @Column(nullable = false)
    @ColumnDefault("false")
    boolean isRead;

    @Column(nullable = false)
    Integer type;

    @Column(nullable = false)
    Timestamp send_at;

    @ManyToOne(fetch = FetchType.LAZY)
    Parent receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    Parent sender;

    @ManyToOne(fetch = FetchType.LAZY)
    Child child;
}
