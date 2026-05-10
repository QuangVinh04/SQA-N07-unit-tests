package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/** Entity đại diện cho bài kiểm tra (test) trong module. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "test")
public class TestEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Type")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ModuleID")
    private ModuleEntity module;

    @OneToMany(mappedBy = "test", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TestProgressEntity> testProgresses;
}
