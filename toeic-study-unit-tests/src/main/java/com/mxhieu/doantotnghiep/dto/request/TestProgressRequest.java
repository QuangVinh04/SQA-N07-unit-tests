package com.mxhieu.doantotnghiep.dto.request;

import lombok.*;

/** DTO yêu cầu kiểm tra tiến trình hoàn thành bài test. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestProgressRequest {

    /** ID của bài test cần kiểm tra. */
    private Integer testId;

    /** ID của hồ sơ học viên. */
    private Integer studentprofileId;
}
