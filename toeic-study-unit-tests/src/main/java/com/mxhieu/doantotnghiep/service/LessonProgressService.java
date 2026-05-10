package com.mxhieu.doantotnghiep.service;

import com.mxhieu.doantotnghiep.dto.request.LessonProgressRequest;
import com.mxhieu.doantotnghiep.entity.CourseEntity;
import com.mxhieu.doantotnghiep.entity.StudentProfileEntity;
import com.mxhieu.doantotnghiep.entity.TrackEntity;

/**
 * Interface nghiệp vụ xử lý tiến trình học bài học (lesson progress):
 * kiểm tra điều kiện hoàn thành, mở khóa lesson/test/course/track tiếp theo.
 */
public interface LessonProgressService {

    /**
     * Kiểm tra điều kiện hoàn thành bài học:
     * - Phải xem đủ % video yêu cầu (gatingRules)
     * - Phải hoàn thành tất cả exercise (nếu có)
     * @return true nếu hoàn thành, false nếu chưa
     */
    Boolean checkCompletionCondition(LessonProgressRequest request);

    /** Mở khóa khóa học tiếp theo sau khi học viên hoàn thành khóa học hiện tại. */
    void unLockNextCourse(CourseEntity course, StudentProfileEntity studentProfileEntity);

    /** Mở khóa track tiếp theo sau khi học viên hoàn thành toàn bộ track hiện tại. */
    void unLockNextTrack(TrackEntity trackEntity, StudentProfileEntity studentProfileEntity);
}
