package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository quản lý bài test. */
public interface TestRepository extends JpaRepository<TestEntity, Integer> {
}
