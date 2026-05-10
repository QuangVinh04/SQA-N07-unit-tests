package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.StudentProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository quản lý hồ sơ học viên. */
public interface StudentProfileRepository extends JpaRepository<StudentProfileEntity, Integer> {
}
