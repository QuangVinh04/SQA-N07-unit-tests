package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.TestAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Repository quản lý lần thử làm bài test. */
public interface TestAttemptRepository extends JpaRepository<TestAttemptEntity, Integer> {

    /**
     * Tìm lần thử có điểm cao nhất của học viên cho một bài test cụ thể.
     * Dùng để kiểm tra điều kiện hoàn thành test.
     */
    Optional<TestAttemptEntity> findTopByTest_IdAndStudentProfile_IdOrderByTotalScoreDesc(
            Integer testId, Integer studentProfileId);
}
