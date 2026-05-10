package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/** Entity đại diện cho một bài học (lesson) trong module. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "lesson")
public class LessonEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Title")
    private String title;

    @Column(name = "Summary")
    private String summary;

    @Column(name = "DurationMinutes")
    private Integer durationMinutes;

    /** Thứ tự của bài học trong module (bắt đầu từ 1). */
    @Column(name = "OrderIndex")
    private Integer orderIndex;

    /**
     * Ngưỡng % đã xem tối thiểu để coi là hoàn thành bài học.
     * Ví dụ: 80 nghĩa là phải xem ít nhất 80% video.
     */
    @Column(name = "GatingRules")
    private Integer gatingRules;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ModuleID")
    private ModuleEntity module;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaAssetEntity> mediaassets;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseEntity> exercises;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonProgressEntity> lessonProgresses;
}
