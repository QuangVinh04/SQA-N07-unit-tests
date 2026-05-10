package com.mxhieu.doantotnghiep.dto.response;

import lombok.*;

/** DTO phản hồi thông tin bài test trong lịch học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestResponse {
    private Integer id;
    private String name;
    private String type;
    private String status;
}
