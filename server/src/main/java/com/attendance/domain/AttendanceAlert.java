package com.attendance.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "attendance_alert")
public class AttendanceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_no", nullable = false, unique = true, length = 30)
    private String alertNo;

    @Column(name = "employee_id", nullable = false, length = 20)
    private String employeeId;

    @Column(name = "employee_name", length = 50)
    private String employeeName;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(length = 100)
    private String position;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String level;

    @Column(length = 500)
    private String summary;

    @Column(name = "ai_suggestion", columnDefinition = "TEXT")
    private String aiSuggestion;

    @Column(length = 20)
    private String status = "待处理";

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(length = 50)
    private String handler;

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
