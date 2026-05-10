package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository quản lý khóa học. */
public interface CourseRepository extends JpaRepository<CourseEntity, Integer> {
}
