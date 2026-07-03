package com.attendance.repository;

import com.attendance.domain.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Page<AttendanceRecord> findByDepartmentName(String departmentName, Pageable pageable);

    @Query("SELECT a FROM AttendanceRecord a WHERE " +
            "(:employeeId IS NULL OR a.employeeId = :employeeId) AND " +
            "(:departmentName IS NULL OR a.departmentName = :departmentName) AND " +
            "(:startDate IS NULL OR a.recordDate >= :startDate) AND " +
            "(:endDate IS NULL OR a.recordDate <= :endDate)")
    Page<AttendanceRecord> findByFilters(@Param("employeeId") String employeeId,
                                          @Param("departmentName") String departmentName,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);
}
