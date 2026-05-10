package com.mxhieu.doantotnghiep.service;

import com.mxhieu.doantotnghiep.dto.request.TestProgressRequest;

/**
 * Interface nghiệp vụ xử lý tiến trình làm bài test (test progress):
 * kiểm tra điều kiện hoàn thành, mở khóa item tiếp theo.
 */
public interface TestProgressService {

    /**
     * Kiểm tra điều kiện hoàn thành bài test:
     * - Học viên phải có ít nhất một lần thử với totalScore >= 50
     * @return true nếu hoàn thành, false nếu chưa
     */
    Boolean checkCompletionCondition(TestProgressRequest request);
}
