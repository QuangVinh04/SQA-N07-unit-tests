package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.LessonProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Repository quản lý tiến trình học bài học của học viên. */
public interface LessonProgressRepository extends JpaRepository<LessonProgressEntity, Integer> {

    /**
     * Tìm danh sách tiến trình học theo lessonId và studentProfileId.
     * Thường trả về list có 0 hoặc 1 phần tử.
     */
    List<LessonProgressEntity> findByLesson_IdAndStudentProfile_Id(Integer lessonId, Integer studentProfileId);
}
