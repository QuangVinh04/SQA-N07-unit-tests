package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.TestProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Repository quản lý tiến trình làm bài test của học viên. */
public interface TestProgressRepository extends JpaRepository<TestProgressEntity, Integer> {

    /** Tìm danh sách tiến trình test theo testId và studentProfileId. */
    List<TestProgressEntity> findByTest_IdAndStudentProfile_Id(Integer testId, Integer studentProfileId);
}
