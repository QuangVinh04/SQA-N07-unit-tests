package com.mxhieu.doantotnghiep.entity;

import com.mxhieu.doantotnghiep.utils.ModuleType;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/** Entity đại diện cho module trong khóa học (gồm các bài học hoặc bài test). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "module")
public class ModuleEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Title")
    private String title;

    @Column(name = "OrderIndex")
    private Long orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type")
    private ModuleType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseID")
    private CourseEntity course;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LessonEntity> lessons;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TestEntity> tests;
}
