package com.ibridge.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role; // PARENT, CHILD

    // 부모 정보 (부모 계정 전용)
    private String relation; // 아이와의 관계 (ex: 아빠, 엄마)

    // 아이 정보 (아이 전용)
    private LocalDate birth;
    private int gender; // 남자 0, 여자 1

    //공통
    private Long accountId; // 로그인 계정 ID
}
