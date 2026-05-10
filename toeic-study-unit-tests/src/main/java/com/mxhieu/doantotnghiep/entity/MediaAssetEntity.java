package com.mxhieu.doantotnghiep.entity;

import jakarta.persistence.*;
import lombok.*;

/** Entity đại diện cho tài nguyên media (video) của một bài học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "mediaasset")
public class MediaAssetEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Url")
    private String url;

    /** Loại file, ví dụ: "video/mp4". */
    @Column(name = "Type")
    private String type;

    /** Thời lượng video tính bằng giây. */
    @Column(name = "LengthSec")
    private Integer lengthSec;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonID")
    private LessonEntity lesson;
}
