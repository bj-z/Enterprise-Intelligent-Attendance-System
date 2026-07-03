package com.attendance.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "time_requirement")
public class TimeRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "dept_name", length = 100, nullable = false)
    private String deptName;

    @Column(name = "monday_time", length = 10)
    private String mondayTime = "09:00";

    @Column(name = "tuesday_time", length = 10)
    private String tuesdayTime = "09:00";

    @Column(name = "wednesday_time", length = 10)
    private String wednesdayTime = "09:00";

    @Column(name = "thursday_time", length = 10)
    private String thursdayTime = "09:00";

    @Column(name = "friday_time", length = 10)
    private String fridayTime = "09:00";

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
