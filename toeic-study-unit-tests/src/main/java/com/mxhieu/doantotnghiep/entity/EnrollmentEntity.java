package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/** Entity đại diện cho quan hệ đăng ký học (enrollment) giữa học viên và track. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "enrollment")
public class EnrollmentEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    /**
     * Trạng thái đăng ký: 1 = ACTIVE, 2 = DONE.
     */
    @Column(name = "Status")
    private Integer status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrackID")
    private TrackEntity track;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentProfileID")
    private StudentProfileEntity studentProfile;

    @OneToMany(mappedBy = "enrollment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EnrollmentCourseEntity> enrollmentCourses;
}
