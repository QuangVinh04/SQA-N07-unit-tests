package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/** Repository quản lý bài học. */
public interface LessonRepository extends JpaRepository<LessonEntity, Integer> {

    /** Lấy tất cả bài học trong module, sắp xếp theo orderIndex tăng dần. */
    List<LessonEntity> findByModuleId(Integer moduleId);

    /** Lấy orderIndex lớn nhất trong module (dùng để tính vị trí thêm mới). */
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM LessonEntity l WHERE l.module.id = :moduleId")
    int getMaxOrder(Integer moduleId);

    /** Tăng orderIndex của các lesson có orderIndex >= newIndex (để chèn lesson mới). */
    @Query(value = "UPDATE lesson SET OrderIndex = OrderIndex + 1 WHERE ModuleID = :moduleId AND OrderIndex >= :newIndex", nativeQuery = true)
    void flushOrderIndex(Integer moduleId, Integer newIndex);

    /** Giảm orderIndex của các lesson có orderIndex > oldIndex (sau khi xóa/di chuyển). */
    @Query(value = "UPDATE lesson SET OrderIndex = OrderIndex - 1 WHERE ModuleID = :moduleId AND OrderIndex > :oldIndex", nativeQuery = true)
    void decreaseOrderIndex(Integer moduleId, Integer oldIndex);

    /**
     * Tính tổng điểm của tất cả bài tập (exercise) trong lesson mà student đã hoàn thành.
     */
    @Query(value = """
        SELECT COALESCE(SUM(a.Score), 0)
        FROM attempt a
        JOIN exercise e ON a.ExerciseID = e.ID
        WHERE e.LessonID = :lessonId AND a.StudentProfileID = :studentId
        """, nativeQuery = true)
    int totalScroreOfLesson(Integer lessonId, Integer studentId);
}
