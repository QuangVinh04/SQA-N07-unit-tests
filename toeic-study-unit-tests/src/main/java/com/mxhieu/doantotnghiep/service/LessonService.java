package com.mxhieu.doantotnghiep.service;

import com.mxhieu.doantotnghiep.dto.request.LessonOrTestAroundRequest;
import com.mxhieu.doantotnghiep.dto.response.LessonOrTestAroundResponse;

/**
 * Interface nghiệp vụ xử lý bài học (lesson):
 * kiểm tra trạng thái, tính sao hoàn thành, điều hướng trước/sau.
 */
public interface LessonService {

    /** Kiểm tra học viên đã hoàn thành bài học chưa (process == 2). */
    Boolean isCompletedLesson(Integer lessonId, Integer studentProfileId);

    /** Kiểm tra bài học có đang bị khóa với học viên không. */
    Boolean isLockLesson(Integer lessonId, Integer studentProfileId);

    /**
     * Tính số sao hoàn thành bài học (0-3 sao).
     * 0: chưa làm, 1: <50%, 2: 50-79%, 3: >=80% hoặc không có exercise.
     */
    int completedStar(Integer lessonId, Integer userId);

    /** Lấy bài học/test liền kề PHÍA SAU của lesson/test hiện tại. */
    LessonOrTestAroundResponse getNextLessonOrTest(LessonOrTestAroundRequest request);

    /** Lấy bài học/test liền kề PHÍA TRƯỚC của lesson/test hiện tại. */
    LessonOrTestAroundResponse getPreviousLessonID(LessonOrTestAroundRequest request);
}
