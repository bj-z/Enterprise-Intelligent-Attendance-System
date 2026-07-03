package com.attendance.service.impl;

import com.attendance.common.BusinessException;
import com.attendance.common.PageResult;
import com.attendance.domain.AttendanceAlert;
import com.attendance.domain.PerformanceRecord;
import com.attendance.repository.AttendanceAlertRepository;
import com.attendance.repository.PerformanceRecordRepository;
import com.attendance.service.AiAnalysisService;
import com.attendance.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AttendanceAlertRepository alertRepository;
    private final PerformanceRecordRepository performanceRepository;
    private final AiAnalysisService aiAnalysisService;

    @Override
    public PageResult<AttendanceAlert> listByPage(int page, int pageSize, String status, String level) {
        Page<AttendanceAlert> result;
        boolean hasLevel = level != null && !level.isEmpty();
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "employeeId"));

        if ("已关闭".equals(status)) {
            PageRequest pr = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "employeeId"));
            result = hasLevel
                    ? alertRepository.findByStatusAndLevel("已关闭", level, pr)
                    : alertRepository.findByStatus("已关闭", pr);
        } else {
            // 待处理：状态不为"已关闭"的
            result = hasLevel
                    ? alertRepository.findByStatusNotAndLevel("已关闭", level, pageRequest)
                    : alertRepository.findByStatusNot("已关闭", pageRequest);
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, pageSize);
    }

    @Override
    @Transactional
    public void markHandled(Long id, String handler) {
        AttendanceAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预警记录不存在"));
        alert.setStatus("已关闭");
        alert.setHandleTime(LocalDateTime.now());
        alert.setHandler(handler);
        alertRepository.save(alert);
    }

    @Override
    @Transactional
    public int cleanupOldProcessed() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(15);
        return alertRepository.deleteOldProcessedAlerts(cutoff);
    }

    @Override
    @Transactional
    public String generateAiSuggestion(Long id) {
        AttendanceAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预警记录不存在"));
        String suggestion = aiAnalysisService.generateAlertSuggestion(alert);
        if (suggestion != null && !suggestion.isEmpty()) {
            alert.setAiSuggestion(suggestion);
            alertRepository.save(alert);
        }
        return suggestion;
    }

    @Override
    @Transactional
    public AttendanceAlert generateFullAi(Long id) {
        return generateSelective(id, Set.of("summary", "aiSuggestion"));
    }

    @Override
    @Transactional
    public AttendanceAlert generateSelective(Long id, Set<String> fields) {
        AttendanceAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预警记录不存在"));

        if (fields.contains("summary") && (alert.getSummary() == null || alert.getSummary().isEmpty())) {
            String summary = aiAnalysisService.generateAlertSummary(alert);
            if (summary == null || summary.isEmpty()) {
                throw new BusinessException("AI 生成失败，请检查 AI 管理中的 API Key 是否配置正确");
            }
            alert.setSummary(summary);
        }

        if (fields.contains("aiSuggestion") && (alert.getAiSuggestion() == null || alert.getAiSuggestion().isEmpty())) {
            String suggestion = aiAnalysisService.generateAlertSuggestion(alert);
            if (suggestion != null && !suggestion.isEmpty()) {
                alert.setAiSuggestion(suggestion);
            }
        }

        if (fields.contains("level")) {
            autoDetermineRiskLevel(id);
            alert = alertRepository.findById(id).orElse(alert);
        }

        alertRepository.save(alert);
        return alert;
    }

    @Override
    public PageResult<AttendanceAlert> listByPageWithThreshold(int page, int pageSize, String status, String level, int threshold) {
        // 直接查库，不再动态生成虚拟记录；绩效不达标员工由 syncFromPerformance 写入真实记录
        return listByPage(page, pageSize, status, level);
    }

    private String determineLevelFromScore(BigDecimal score) {
        if (score == null) return "中";
        double s = score.doubleValue();
        if (s < 60) return "高";
        if (s < 75) return "中";
        return "低";
    }

    @Override
    @Transactional
    public int syncFromPerformance(int threshold) {
        List<PerformanceRecord> lowScoreRecords = performanceRepository.findAll().stream()
                .filter(r -> r.getFinalScore() != null && r.getFinalScore().compareTo(BigDecimal.valueOf(threshold)) < 0)
                .collect(Collectors.toList());

        int count = 0;
        for (PerformanceRecord pr : lowScoreRecords) {
            boolean exists = alertRepository.findAll().stream()
                    .anyMatch(a -> a.getEmployeeId().equals(pr.getEmployeeId()) && !"已关闭".equals(a.getStatus()));
            if (exists) continue;

            AttendanceAlert alert = new AttendanceAlert();
            alert.setAlertNo("PERF-" + pr.getEmployeeId() + "-" + System.currentTimeMillis() % 100000);
            alert.setEmployeeId(pr.getEmployeeId());
            alert.setEmployeeName(pr.getEmployeeName());
            alert.setDepartmentName(pr.getDepartmentName());
            alert.setPosition(pr.getPosition());
            alert.setLevel(determineLevelFromScore(pr.getFinalScore()));
            alert.setSummary("绩效综合评分 " + pr.getFinalScore() + " 分，低于阈值 " + threshold + " 分");
            alert.setAiSuggestion("建议关注该员工工作表现，安排绩效面谈");
            alert.setStatus("待处理");
            alertRepository.save(alert);
            count++;
        }
        log.info("已同步 {} 条绩效不达标预警", count);
        return count;
    }

    @Override
    @Transactional
    public int clearColumn(String column, java.util.List<Long> ids) {
        List<AttendanceAlert> alerts;
        if (ids != null && !ids.isEmpty()) {
            alerts = alertRepository.findAllById(ids);
        } else {
            alerts = alertRepository.findAll();
        }
        int count = 0;
        for (AttendanceAlert alert : alerts) {
            boolean changed = false;
            switch (column) {
                case "summary":
                    if (alert.getSummary() != null && !alert.getSummary().isEmpty()) {
                        alert.setSummary("");
                        changed = true;
                    }
                    break;
                case "aiSuggestion":
                    if (alert.getAiSuggestion() != null && !alert.getAiSuggestion().isEmpty()) {
                        alert.setAiSuggestion("");
                        changed = true;
                    }
                    break;
                case "level":
                    if (alert.getLevel() != null && !alert.getLevel().isEmpty()) {
                        alert.setLevel("");
                        changed = true;
                    }
                    break;
            }
            if (changed) {
                alertRepository.save(alert);
                count++;
            }
        }
        log.info("已清空预警列 {}，共 {} 条", column, count);
        return count;
    }

    @Override
    @Transactional
    public void autoDetermineRiskLevel(Long id) {
        AttendanceAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预警记录不存在"));

        // 基于AI分析自动判定风险等级
        String prompt = String.format(
            "你是一个企业考勤管理系统的AI助手。请根据以下员工信息，仅返回一个风险等级（高/中/低）：" +
            "员工姓名：%s，部门：%s，职位：%s，异常概述：%s。只返回一个字：高、中或低。",
            alert.getEmployeeName(), alert.getDepartmentName(),
            alert.getPosition(), alert.getSummary() != null ? alert.getSummary() : ""
        );
        String aiResponse = aiAnalysisService.generateAlertSummary(alert);
        // 从AI返回中提取风险等级关键词
        if (aiResponse != null) {
            if (aiResponse.contains("高")) {
                alert.setLevel("高");
            } else if (aiResponse.contains("低")) {
                alert.setLevel("低");
            } else {
                alert.setLevel("中");
            }
        }
        // 同时基于绩效数据做备份判定
        List<PerformanceRecord> perfList = performanceRepository.findAll().stream()
                .filter(r -> r.getEmployeeId().equals(alert.getEmployeeId()))
                .collect(Collectors.toList());
        if (!perfList.isEmpty()) {
            PerformanceRecord pr = perfList.get(0);
            if (pr.getFinalScore() != null) {
                double score = pr.getFinalScore().doubleValue();
                if (score < 60) alert.setLevel("高");
                else if (score < 75) alert.setLevel("中");
                else alert.setLevel("低");
            }
        }
        alertRepository.save(alert);
        log.info("预警 {} 风险等级自动判定为: {}", alert.getAlertNo(), alert.getLevel());
    }

    @Override
    public List<AttendanceAlert> findByIds(List<Long> ids) {
        return alertRepository.findAllById(ids);
    }

    @Override
    public AttendanceAlert findAlertById(Long id) {
        return alertRepository.findById(id).orElse(null);
    }
}
