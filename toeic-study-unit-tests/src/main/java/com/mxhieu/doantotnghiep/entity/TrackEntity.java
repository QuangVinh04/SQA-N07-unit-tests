package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/** Entity đại diện cho lộ trình học (track), ví dụ: "0-300", "300-600". */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "track")
public class TrackEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Code")
    private String code;

    @OneToMany(mappedBy = "track", fetch = FetchType.LAZY)
    private List<CourseEntity> courses;
}
