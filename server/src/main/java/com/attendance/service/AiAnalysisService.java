package com.attendance.service;

import com.attendance.domain.AttendanceAlert;
import com.attendance.domain.PerformanceRecord;

import java.util.Map;

public interface AiAnalysisService {
    String generateAlertSummary(AttendanceAlert alert);
    String generateAlertSuggestion(AttendanceAlert alert);

    /**
     * 生成绩效分析：考勤得分、综合评分、AI综合评语
     * @param record 绩效记录
     * @return Map，key为attendanceScore/finalScore/scoreLevel/aiComment，仅包含AI生成的字段
     */
    Map<String, Object> generatePerformanceAnalysis(PerformanceRecord record);
}
