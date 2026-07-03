package com.attendance.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "attendance_summary")
public class AttendanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_date", nullable = false, unique = true)
    private LocalDate summaryDate;

    @Column(name = "total_employees")
    private Integer totalEmployees = 0;

    @Column(name = "actual_attendance")
    private Integer actualAttendance = 0;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate = BigDecimal.ZERO;

    @Column(name = "late_count")
    private Integer lateCount = 0;

    @Column(name = "early_count")
    private Integer earlyCount = 0;

    @Column(name = "leave_count")
    private Integer leaveCount = 0;

    @Column(name = "missing_count")
    private Integer missingCount = 0;

    @Column(name = "new_hire_count")
    private Integer newHireCount = 0;

    @Column(name = "pending_leave_count")
    private Integer pendingLeaveCount = 0;

    @Column(name = "emergency_count")
    private Integer emergencyCount = 0;

    @Column(name = "alert_count")
    private Integer alertCount = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
