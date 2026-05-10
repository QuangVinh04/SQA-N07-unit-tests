package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/** Entity đại diện cho một mục trong lịch học (ngày học cụ thể + lesson/test). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "studyplanitem")
public class StudyPlanItemEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    /** Ngày học được gán cho mục này. */
    @Column(name = "Date")
    private LocalDate date;

    /** Thứ tự buổi học (slot 0, 1, 2,...). */
    @Column(name = "SlotIndex")
    private Integer slotIndex;

    /**
     * Trạng thái: 0 = chưa học, 1 = đang học, 2 = đã hoàn thành.
     */
    @Column(name = "Status")
    private Integer status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudyPlanID")
    private StudyPlanEntity studyPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonID")
    private LessonEntity lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TestID")
    private TestEntity test;
}
