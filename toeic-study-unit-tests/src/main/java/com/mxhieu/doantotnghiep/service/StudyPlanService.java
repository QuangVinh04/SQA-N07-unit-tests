package com.mxhieu.doantotnghiep.service;

import com.mxhieu.doantotnghiep.dto.request.StudyPlanRequest;
import com.mxhieu.doantotnghiep.dto.response.StudyPlanResponse;
import com.mxhieu.doantotnghiep.dto.response.StudyPlanOverViewResponse;
import com.mxhieu.doantotnghiep.dto.response.InformationOfStudyPlanResponse;

/** Interface cho StudyPlanService — quản lý lịch học của học viên. */
public interface StudyPlanService {
    StudyPlanOverViewResponse getOverviewData(Integer studentId);
    void createStudyPlan(StudyPlanRequest studyPlanRequest);
    StudyPlanResponse verifyInformation(StudyPlanRequest studyPlanRequest);
    Integer checkExistStudyPlan(Integer studentId);
    InformationOfStudyPlanResponse getInformation(Integer studyPlanId);
    int soNgayHocToiThieu(Integer trackId, Integer studentId);
    StudyPlanResponse getStudyPlanDetail(Integer studyPlanId);
}
