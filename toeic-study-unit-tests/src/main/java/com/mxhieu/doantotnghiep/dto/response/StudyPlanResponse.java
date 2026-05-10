package com.mxhieu.doantotnghiep.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/** DTO tóm tắt kế hoạch học tập trả về cho client. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyPlanResponse {
    private Integer trackId;
    private Integer studentProfileId;
    private LocalDate startDate;
    private LocalDate ngayHoanThanh;
    private Integer tongSoBuoiHoc;
    private Integer tongSoUnits;
    private String soUnitsTrenBuoi;
    private List<Integer> ngayHocTrongTuan;
    private List<StudyPlanItemResponse> studyPlanItems;
}
