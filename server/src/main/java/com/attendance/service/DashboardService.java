package com.attendance.service;

import java.util.Map;

public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取今日考勤概览
     */
    Map<String, Object> getAttendanceOverview();

    /**
     * 获取近7日出勤趋势
     */
    Map<String, Object> getAttendanceTrend();

    /**
     * AI异常预警清单
     */
    Map<String, Object> getAlertList();
}
