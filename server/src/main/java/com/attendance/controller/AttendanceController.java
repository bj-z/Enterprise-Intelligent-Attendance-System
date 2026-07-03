package com.attendance.controller;

import com.attendance.common.PageResult;
import com.attendance.common.Result;
import com.attendance.domain.AttendanceRecord;
import com.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "考勤记录")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "分页查询考勤记录")
    @GetMapping("/records")
    public Result<PageResult<AttendanceRecord>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(attendanceService.listRecords(page, pageSize, department, startDate, endDate));
    }

    @Operation(summary = "基于时间要求重新判定所有考勤记录")
    @PostMapping("/judge")
    public Result<Integer> judgeByTimeRequirements() {
        int updated = attendanceService.judgeByTimeRequirements();
        return Result.success("考勤判定完成，更新 " + updated + " 条记录", updated);
    }
}
