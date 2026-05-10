package com.mxhieu.doantotnghiep.service;

/** Interface cho TrackService — dùng trong StudyPlanServiceImpl. */
public interface TrackService {
    /** Tìm trackId đầu tiên chưa hoàn thành và đã mở khóa của học viên. */
    Integer trackDauTienChuaHoanThanhVaMoKhoa(Integer studentId);
}
