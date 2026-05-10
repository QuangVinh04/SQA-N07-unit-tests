package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.EnrollmentCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

/** Repository quản lý quan hệ enrollment-course. */
public interface EnrollmentCourseRepository extends JpaRepository<EnrollmentCourseEntity, Integer> {

    List<EnrollmentCourseEntity> findByCourse_IdAndEnrollment_StudentProfile_Id(
            Integer courseId, Integer studentProfileId);

    /** Lấy trạng thái khóa học của học viên. */
    @Query("SELECT ec.status FROM EnrollmentCourseEntity ec " +
           "WHERE ec.enrollment.studentProfile.id = :studentId AND ec.course.id = :courseId")
    String findStatus(Integer studentId, Integer courseId);

    /** Tìm enrollment-course tiếp theo trong cùng enrollment (ID lớn hơn). */
    Optional<EnrollmentCourseEntity> findTopByIdAfterAndEnrollment_Id(Integer id, Integer enrollmentId);
}
