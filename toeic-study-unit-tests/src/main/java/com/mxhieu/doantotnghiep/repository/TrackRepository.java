package com.mxhieu.doantotnghiep.repository;

import com.mxhieu.doantotnghiep.entity.TrackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository quản lý lộ trình học (track). */
public interface TrackRepository extends JpaRepository<TrackEntity, Integer> {
}
