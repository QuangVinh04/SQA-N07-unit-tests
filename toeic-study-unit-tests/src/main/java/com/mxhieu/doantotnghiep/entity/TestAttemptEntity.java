package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;

/** Entity đại diện cho lần thử làm bài test của học viên. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "testattempt")
public class TestAttemptEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "TotalScore")
    private Integer totalScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TestID")
    private TestEntity test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentProfileID")
    private StudentProfileEntity studentProfile;
}
