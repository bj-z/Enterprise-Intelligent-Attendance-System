package com.attendance.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "attendance_record", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "record_date"})
})
public class AttendanceRecord {

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

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "clock_in", length = 10)
    private String clockIn;

    @Column(name = "clock_out", length = 10)
    private String clockOut;

    @Column(name = "work_hours", precision = 4, scale = 1)
    private BigDecimal workHours = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "正常";

    @Column(name = "is_late")
    private Integer isLate = 0;

    @Column(name = "is_early")
    private Integer isEarly = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
