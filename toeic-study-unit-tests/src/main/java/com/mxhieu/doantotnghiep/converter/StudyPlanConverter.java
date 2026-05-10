package com.mxhieu.doantotnghiep.converter;

import com.mxhieu.doantotnghiep.dto.response.StudyPlanResponse;
import com.mxhieu.doantotnghiep.entity.StudyPlanEntity;
import org.springframework.stereotype.Component;

/** Converter chuyển đổi StudyPlanEntity sang DTO response. */
@Component
public class StudyPlanConverter {

    /** Chuyển StudyPlanEntity sang StudyPlanResponse tóm tắt (không bao gồm chi tiết items). */
    public StudyPlanResponse toResponseSummery(StudyPlanEntity entity) {
        return StudyPlanResponse.builder()
                .trackId(entity.getTrack() != null ? entity.getTrack().getId() : null)
                .studentProfileId(entity.getStudentProfile() != null ? entity.getStudentProfile().getId() : null)
                .startDate(entity.getStartDate())
                .tongSoBuoiHoc(entity.getSoLuongNgayHoc())
                .ngayHocTrongTuan(entity.getNgayHocTrongTuan())
                .build();
    }
}
