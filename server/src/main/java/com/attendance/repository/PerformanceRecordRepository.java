package com.attendance.repository;

import com.attendance.domain.PerformanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceRecordRepository extends JpaRepository<PerformanceRecord, Long> {

    Page<PerformanceRecord> findByDepartmentName(String departmentName, Pageable pageable);

    List<PerformanceRecord> findByIdIn(List<Long> ids);
}
