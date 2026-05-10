package com.mxhieu.doantotnghiep.dto.response;

import lombok.*;

/** DTO phản hồi thông tin bài học trong lịch học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonResponse {
    private Integer id;
    private String title;
    private String status;
    private Integer completedStar;
    private Integer progressWatched;
    private Boolean hasExercise;
}
