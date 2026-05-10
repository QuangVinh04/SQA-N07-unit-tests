package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/** Entity đại diện cho khóa học TOEIC. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "course")
public class CourseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrackID")
    private TrackEntity track;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ModuleEntity> modules;
}
