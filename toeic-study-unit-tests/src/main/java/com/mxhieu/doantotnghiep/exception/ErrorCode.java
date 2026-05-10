package com.mxhieu.doantotnghiep.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Enum định nghĩa toàn bộ mã lỗi nghiệp vụ của ứng dụng.
 * Mỗi mã lỗi bao gồm: code (số), message (tiếng Anh/Việt), httpStatusCode.
 */
@Getter
public enum ErrorCode {
    STUDENT_PROFILE_NOT_FOUND(1006, "Student profile not found", HttpStatus.NOT_FOUND),
    LESSON_NOT_FOUND(1011, "Lesson not found", HttpStatus.NOT_FOUND),
    TEST_NOT_FOUND(1016, "test not found", HttpStatus.NOT_FOUND),
    LESSON_PROGRESS_NOT_EXISTS(1017, "Lesson progress not exists", HttpStatus.NOT_FOUND),
    TEST_PROGRESS_NOT_EXISTS(1019, "Test progress not exists", HttpStatus.NOT_FOUND),
    STUDYPLAN_NOT_FOUND(1025, "không tìm thấy studyplan", HttpStatus.NOT_FOUND),
    STUDYPLAN_NOT_ACTIVE(1022, "Không có plan nào hoạt động", HttpStatus.NOT_FOUND),
    LESSON_NOT_HAS_NEXT(1019, "do not has next lesson", HttpStatus.NOT_FOUND),
    LESSON_NOT_HAS_PREVIOUS(1020, "do not has previous lesson", HttpStatus.NOT_FOUND),
    TEST_ATTEMPT_NOT_FOUND(1020, "bai test chưa được làm", HttpStatus.NOT_FOUND),
    TRACK_NOT_FOUND(1012, "Track not found", HttpStatus.NOT_FOUND),
    NEXT_NOT_FOUND(1000, "khong tim thay course tiep theo", HttpStatus.NOT_FOUND),
    LESSON_IS_LOCK(1000, "lesson nay bị khóa", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
