package com.mxhieu.doantotnghiep.dto.request;

import lombok.*;

/** DTO dùng để xác định bài học/test ở vị trí xung quanh (trước/sau) trong khóa học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonOrTestAroundRequest {

    /** ID của lesson hoặc test hiện tại. */
    private Integer id;

    /** Loại: "LESSON" hoặc "TEST". */
    private String type;
}
