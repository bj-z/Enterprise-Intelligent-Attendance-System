package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "首页仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getDashboardStats());
    }

    @Operation(summary = "获取今日考勤概览")
    @GetMapping("/attendance-overview")
    public Result<Map<String, Object>> getAttendanceOverview() {
        return Result.success(dashboardService.getAttendanceOverview());
    }

    @Operation(summary = "获取近7日出勤趋势")
    @GetMapping("/attendance-trend")
    public Result<Map<String, Object>> getAttendanceTrend() {
        return Result.success(dashboardService.getAttendanceTrend());
    }

    @Operation(summary = "获取异常预警清单（AI）")
    @GetMapping("/alerts")
    public Result<Map<String, Object>> getAlerts() {
        return Result.success(dashboardService.getAlertList());
    }
}
