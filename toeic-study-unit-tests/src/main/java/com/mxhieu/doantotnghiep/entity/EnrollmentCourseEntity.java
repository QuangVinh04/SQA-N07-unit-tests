package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;

/** Entity đại diện cho khóa học trong một enrollment (đăng ký học). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "enrollmentcourse")
public class EnrollmentCourseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    /**
     * Trạng thái: "LOCK", "UNLOCK", "DONE".
     */
    @Column(name = "Status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EnrollmentID")
    private EnrollmentEntity enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseID")
    private CourseEntity course;
}
