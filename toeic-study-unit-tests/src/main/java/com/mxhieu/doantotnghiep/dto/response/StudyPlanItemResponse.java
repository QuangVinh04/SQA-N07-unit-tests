package com.mxhieu.doantotnghiep.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/** DTO phản hồi chi tiết từng mục trong lịch học (theo ngày). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyPlanItemResponse {
    private LocalDate date;
    private String type;
    private List<LessonResponse> lessons;
    private List<TestResponse> tests;
}
