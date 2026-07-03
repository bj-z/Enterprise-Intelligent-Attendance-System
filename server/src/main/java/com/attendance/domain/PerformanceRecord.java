package com.attendance.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "performance_record")
public class PerformanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, length = 20)
    private String employeeId;

    @Column(name = "employee_name", length = 50)
    private String employeeName;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(length = 100)
    private String position;

    @Column(name = "analysis_period", length = 20)
    private String analysisPeriod = "本月";

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @Column(name = "late_count")
    private Integer lateCount = 0;

    @Column(name = "early_count")
    private Integer earlyCount = 0;

    @Column(name = "leave_days", precision = 3, scale = 1)
    private BigDecimal leaveDays = BigDecimal.ZERO;

    @Column(name = "attendance_score", precision = 5, scale = 1)
    private BigDecimal attendanceScore;

    @Column(name = "task_score", precision = 5, scale = 1)
    private BigDecimal taskScore;

    @Column(name = "collaboration_score", precision = 5, scale = 1)
    private BigDecimal collaborationScore;

    @Column(name = "final_score", precision = 5, scale = 1)
    private BigDecimal finalScore;

    @Column(name = "score_level", length = 20)
    private String scoreLevel;

    @Column(name = "ai_comment", columnDefinition = "TEXT")
    private String aiComment;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
