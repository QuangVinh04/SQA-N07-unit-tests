package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;

/** Entity đại diện cho bài tập (exercise) thuộc một bài học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "exercise")
public class ExerciseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonID")
    private LessonEntity lesson;
}
