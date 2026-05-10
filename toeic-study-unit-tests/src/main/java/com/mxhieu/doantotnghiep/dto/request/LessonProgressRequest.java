package com.mxhieu.doantotnghiep.dto.request;

import lombok.*;

/** DTO yêu cầu kiểm tra tiến trình hoàn thành bài học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonProgressRequest {

    /** ID của bài học cần kiểm tra. */
    private Integer lessonId;

    /** ID của hồ sơ học viên. */
    private Integer studentProfileId;

    /** Phần trăm video đã xem (0-100). */
    private Integer percentageWatched;
}
