package com.mxhieu.doantotnghiep.dto.response;

import lombok.*;

/** DTO tổng quan về kế hoạch học tập theo track. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyPlanOverViewResponse {
    private Integer trackId;
    private String trackName;
    private String trackDescription;
    private String overview;
    private String mucTieuDauRa;
    private String thoiGianHocTieuChuan;
}
