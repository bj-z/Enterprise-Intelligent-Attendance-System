package com.attendance.repository;

import com.attendance.domain.AttendanceAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface AttendanceAlertRepository extends JpaRepository<AttendanceAlert, Long> {

    Page<AttendanceAlert> findByLevel(String level, Pageable pageable);

    Page<AttendanceAlert> findByStatus(String status, Pageable pageable);

    Page<AttendanceAlert> findByStatusNot(String status, Pageable pageable);

    Page<AttendanceAlert> findByStatusAndLevel(String status, String level, Pageable pageable);

    Page<AttendanceAlert> findByStatusNotAndLevel(String status, String level, Pageable pageable);

    long countByStatusNot(String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM AttendanceAlert a WHERE a.status = '已关闭' AND a.handleTime < :cutoff")
    int deleteOldProcessedAlerts(@Param("cutoff") LocalDateTime cutoff);
}
