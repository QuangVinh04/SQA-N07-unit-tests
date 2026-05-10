package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repository quản lý bài tập (exercise). */
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Integer> {

    /** Đếm số exercise trong một lesson. */
    @Query("SELECT COUNT(e) FROM ExerciseEntity e WHERE e.lesson.id = :lessonId")
    int countByLessonId(Integer lessonId);

    /**
     * Kiểm tra học viên đã hoàn thành bài tập chưa
     * (có ít nhất một attempt cho exercise đó không).
     */
    @Query(value = """
        SELECT CASE WHEN COUNT(a.ID) > 0 THEN true ELSE false END
        FROM attempt a
        WHERE a.ExerciseID = :exerciseId AND a.StudentProfileID = :studentId
        """, nativeQuery = true)
    boolean isExerciseCompletedByStudent(Integer exerciseId, Integer studentId);
}
