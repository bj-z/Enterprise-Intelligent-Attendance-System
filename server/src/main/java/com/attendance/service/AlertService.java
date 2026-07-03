package com.attendance.service;

import com.attendance.common.PageResult;
import com.attendance.domain.AttendanceAlert;

import java.util.List;

public interface AlertService {

    PageResult<AttendanceAlert> listByPage(int page, int pageSize, String status, String level);

    /**
     * 根据阈值筛选预警（含绩效不达标员工）
     */
    PageResult<AttendanceAlert> listByPageWithThreshold(int page, int pageSize, String status, String level, int threshold);

    void markHandled(Long id, String handler);

    int cleanupOldProcessed();

    String generateAiSuggestion(Long id);

    AttendanceAlert generateFullAi(Long id);

    /**
     * 同步绩效不达标员工到预警列表
     */
    int syncFromPerformance(int threshold);

    /**
     * 清空预警指定列内容（支持按 ID 限定范围）
     */
    int clearColumn(String column, java.util.List<Long> ids);

    /**
     * 按需生成：只生成指定字段
     */
    AttendanceAlert generateSelective(Long id, java.util.Set<String> fields);

    /**
     * AI 自动判定风险等级
     */
    void autoDetermineRiskLevel(Long id);

    /**
     * 按ID列表查询
     */
    List<AttendanceAlert> findByIds(List<Long> ids);

    /**
     * 按ID查询单条
     */
    AttendanceAlert findAlertById(Long id);
}