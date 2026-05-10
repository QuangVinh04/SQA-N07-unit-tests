package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;

/** Entity lưu tiến trình học bài học của học viên. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "lessonprogress")
public class LessonProgressEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonID")
    private LessonEntity lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentProfileID")
    private StudentProfileEntity studentProfile;

    /** Phần trăm video đã xem (0-100). */
    @Column(name = "PercentageWatched")
    private Integer percentageWatched;

    /**
     * Trạng thái tiến trình:
     * 0 = đã mở khóa (UNLOCK), 1 = đang học (IN_PROGRESS), 2 = hoàn thành (DONE).
     */
    @Column(name = "Process")
    private int process;
}
