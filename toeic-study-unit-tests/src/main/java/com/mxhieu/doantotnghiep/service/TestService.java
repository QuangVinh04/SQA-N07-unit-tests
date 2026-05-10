package com.mxhieu.doantotnghiep.service;

/** Interface cho TestService — dùng để kiểm tra trạng thái bài test. */
public interface TestService {
    Boolean isCompletedTest(Integer testId, Integer studentProfileId);
    Boolean isLock(Integer testId, Integer studentProfileId);
    int commpletedStar(Integer testId, Integer studentProfileId);
}
