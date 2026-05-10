package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Repository quản lý enrollment (đăng ký học theo track). */
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Integer> {

    List<EnrollmentEntity> findByTrack_IdAndStudentProfile_Id(Integer trackId, Integer studentProfileId);
}
