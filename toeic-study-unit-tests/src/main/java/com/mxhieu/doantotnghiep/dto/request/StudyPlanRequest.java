package com.mxhieu.doantotnghiep.dto.request;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/** DTO yêu cầu tạo/xem lịch học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyPlanRequest {
    private Integer trackId;
    private Integer studentProfileId;
    private LocalDate startDate;
    private Integer soLuongNgayHoc;
    private List<Integer> ngayHocTrongTuan;
}
