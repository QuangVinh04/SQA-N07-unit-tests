package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/** Entity đại diện cho học viên (student profile). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "studentprofile")
public class StudentProfileEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "FullName")
    private String fullName;
}
