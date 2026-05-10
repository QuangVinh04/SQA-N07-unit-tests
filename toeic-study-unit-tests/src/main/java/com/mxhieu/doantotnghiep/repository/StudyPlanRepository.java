package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.StudyPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Repository quản lý kế hoạch học tập. */
public interface StudyPlanRepository extends JpaRepository<StudyPlanEntity, Integer> {

    List<StudyPlanEntity> findByStudentProfile_Id(Integer studentProfileId);

    List<StudyPlanEntity> findByTrack_IdAndStudentProfile_Id(Integer trackId, Integer studentProfileId);
}
