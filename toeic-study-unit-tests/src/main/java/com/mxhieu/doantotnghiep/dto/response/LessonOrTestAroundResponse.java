package com.mxhieu.doantotnghiep.dto.response;

import lombok.*;

/** DTO trả về thông tin lesson hoặc test liền kề (trước/sau) trong khóa học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonOrTestAroundResponse {

    /** ID của lesson hoặc test kề bên. */
    private Integer Id;

    /** Loại: "LESSON" hoặc "TEST". */
    private String type;
}
