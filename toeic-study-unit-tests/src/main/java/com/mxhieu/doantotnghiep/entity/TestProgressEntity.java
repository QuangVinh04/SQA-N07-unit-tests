package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;

/** Entity lưu tiến trình làm bài test của học viên. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "testprogress")
public class TestProgressEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    /**
     * Trạng thái tiến trình:
     * 0 = UNLOCK, 1 = IN_PROGRESS, 2 = DONE.
     */
    @Column(name = "Process")
    private Integer process;

    @Column(name = "TotalScore")
    private Float totalScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentprofileID")
    private StudentProfileEntity studentProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TestID")
    private TestEntity test;
}
