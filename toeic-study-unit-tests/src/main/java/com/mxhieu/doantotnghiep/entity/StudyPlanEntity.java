package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Entity lưu kế hoạch học tập (study plan) của học viên theo track. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "studyplan")
public class StudyPlanEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "GeneratedAt")
    private LocalDateTime generatedAt;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "Status")
    private Integer status;

    /** Tổng số ngày học dự kiến. */
    @Column(name = "SoLuongNgayHoc")
    private Integer soLuongNgayHoc;

    /** Danh sách ngày trong tuần (1=Thứ Hai, 7=Chủ Nhật). */
    @Column(name = "NgayHocTrongTuan")
    private List<Integer> ngayHocTrongTuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentProfileID")
    private StudentProfileEntity studentProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrackID")
    private TrackEntity track;

    @OneToMany(mappedBy = "studyPlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<StudyPlanItemEntity> studyPlanItems;
}
