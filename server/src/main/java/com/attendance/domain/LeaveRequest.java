package com.attendance.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "leave_request")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_no", nullable = false, unique = true, length = 30)
    private String requestNo;

    @Column(name = "employee_id", length = 20)
    private String employeeId;

    @Column(name = "employee_name", nullable = false, length = 50)
    private String employeeName;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(name = "leave_type", nullable = false, length = 20)
    private String leaveType;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal days;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String reason;

    @Column(length = 50)
    private String approver;

    @Column(length = 20)
    private String status = "pending";

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "approve_comment", length = 500)
    private String approveComment;

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
