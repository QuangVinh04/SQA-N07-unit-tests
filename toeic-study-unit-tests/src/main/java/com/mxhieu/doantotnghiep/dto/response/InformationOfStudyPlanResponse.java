package com.mxhieu.doantotnghiep.dto.response;

import lombok.*;
import java.util.List;

/** DTO thông tin chi tiết về tiến độ thực hiện lịch học. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InformationOfStudyPlanResponse {
    private Integer soNgayHocConLai;
    private Integer soCupDaDat;
    private Integer soUnitDat2Cup;
    private Integer soUnitDaHoanThanh;
    private Integer soUnitTheoKeHoach;
    private Integer tongSoUnit;
    private List<StudyPlanItemResponse> unitsCanHoanThanh;
}
