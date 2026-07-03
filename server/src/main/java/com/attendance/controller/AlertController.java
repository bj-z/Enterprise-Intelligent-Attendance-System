package com.attendance.controller;

import com.attendance.common.PageResult;
import com.attendance.common.Result;
import com.attendance.domain.AttendanceAlert;
import com.attendance.service.AiAnalysisService;
import com.attendance.service.AiConfigService;
import com.attendance.service.AlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.Executor;

@Slf4j
@Tag(name = "异常预警（AI）")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AiConfigService aiConfigService;
    private final AiAnalysisService aiAnalysisService;
    private final ObjectMapper objectMapper;

    @Autowired
    @Qualifier("aiTaskExecutor")
    private Executor aiTaskExecutor;

    @Operation(summary = "分页查询异常预警（支持按状态和等级筛选，含阈值过滤）")
    @GetMapping
    public Result<PageResult<AttendanceAlert>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "pending") String status,
            @RequestParam(required = false) String level,
            @RequestParam(required = false, defaultValue = "-1") int threshold) {
        if (threshold >= 0) {
            return Result.success(alertService.listByPageWithThreshold(page, pageSize, status, level, threshold));
        }
        return Result.success(alertService.listByPage(page, pageSize, status, level));
    }

    @Operation(summary = "标记预警为已处理")
    @PutMapping("/{id}/handle")
    public Result<Void> markHandled(@PathVariable Long id, @RequestParam(defaultValue = "系统管理员") String handler) {
        alertService.markHandled(id, handler);
        return Result.success();
    }

    @Operation(summary = "AI 流式生成预警分析（仅当前页、仅空字段）")
    @PostMapping(value = "/ai-analyze", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter aiAnalyze(@RequestBody List<Long> ids) {
        SseEmitter emitter = new SseEmitter(300_000L);

        aiTaskExecutor.execute(() -> {
            try {
                List<AttendanceAlert> alerts = alertService.findByIds(ids);
                for (int i = 0; i < alerts.size(); i++) {
                    AttendanceAlert alert = alerts.get(i);

                    // 判断哪些字段需要AI生成
                    boolean needSummary = alert.getSummary() == null || alert.getSummary().isBlank();
                    boolean needSuggestion = alert.getAiSuggestion() == null || alert.getAiSuggestion().isBlank();
                    boolean needLevel = alert.getLevel() == null || alert.getLevel().isBlank();

                    Map<String, Object> eventData = new LinkedHashMap<>();
                    eventData.put("id", alert.getId());
                    eventData.put("index", i);
                    eventData.put("total", alerts.size());

                    if (needSummary || needSuggestion || needLevel) {
                        Set<String> fields = new HashSet<>();
                        if (needSummary) fields.add("summary");
                        if (needSuggestion) fields.add("aiSuggestion");
                        if (needLevel) fields.add("level");

                        alertService.generateSelective(alert.getId(), fields);
                        AttendanceAlert saved = alertService.findAlertById(alert.getId());
                        
                        Map<String, Object> data = new LinkedHashMap<>();
                        if (needSummary) data.put("summary", saved.getSummary());
                        if (needSuggestion) data.put("aiSuggestion", saved.getAiSuggestion());
                        if (needLevel) data.put("level", saved.getLevel());
                        eventData.put("data", data);
                    } else {
                        eventData.put("data", Map.of("skipped", true));
                    }

                    emitter.send(SseEmitter.event()
                            .name("progress")
                            .data(objectMapper.writeValueAsString(eventData)));
                }
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("{\"message\":\"完成\"}"));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI预警分析流式处理失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"message\":\"" + (e.getMessage() != null ? e.getMessage().replace("\"", "'") : "未知错误") + "\"}"));
                    emitter.complete();
                } catch (Exception ex) {
                    log.error("发送错误事件失败", ex);
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    @Operation(summary = "重置当前页指定列")
    @PostMapping("/reset-column")
    public Result<?> resetColumn(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        List<Long> ids = idsRaw.stream().map(Long::valueOf).toList();
        String column = (String) body.get("column");
        alertService.clearColumn(column, ids);
        return Result.success();
    }

    @Operation(summary = "AI 生成预警建议")
    @PostMapping("/{id}/ai-suggestion")
    public Result<String> generateAiSuggestion(@PathVariable Long id) {
        String suggestion = alertService.generateAiSuggestion(id);
        if (suggestion == null || suggestion.isEmpty()) {
            return Result.error("AI 生成失败，请检查 API Key 是否有效");
        }
        return Result.success("AI 建议已生成", suggestion);
    }

    @Operation(summary = "AI 完整生成预警概述和建议（支持按需字段）")
    @PostMapping("/{id}/ai-full")
    public Result<AttendanceAlert> generateFullAi(@PathVariable Long id,
            @RequestParam(required = false) String fields) {
        java.util.Set<String> fieldSet;
        if (fields != null && !fields.isEmpty()) {
            fieldSet = new java.util.HashSet<>(java.util.Arrays.asList(fields.split(",")));
        } else {
            fieldSet = java.util.Set.of("summary", "aiSuggestion");
        }
        AttendanceAlert alert = alertService.generateSelective(id, fieldSet);
        return Result.success("AI 分析完成", alert);
    }

    @Operation(summary = "AI 自动判定风险等级")
    @PostMapping("/{id}/auto-level")
    public Result<Void> autoDetermineRiskLevel(@PathVariable Long id) {
        alertService.autoDetermineRiskLevel(id);
        return Result.success();
    }

    @Operation(summary = "获取预警阈值")
    @GetMapping("/threshold")
    public Result<Integer> getThreshold() {
        return Result.success(aiConfigService.getAlertThreshold());
    }

    @Operation(summary = "设置预警阈值")
    @PutMapping("/threshold")
    public Result<Integer> setThreshold(@RequestBody Map<String, Integer> body) {
        Integer threshold = body.get("threshold");
        if (threshold == null || threshold < 0 || threshold > 100) {
            return Result.badRequest("阈值必须在 0-100 之间");
        }
        aiConfigService.setAlertThreshold(threshold);
        alertService.syncFromPerformance(threshold);
        return Result.success("阈值已更新", threshold);
    }

    @Operation(summary = "同步绩效不达标员工到预警列表")
    @PostMapping("/sync-from-performance")
    public Result<Integer> syncFromPerformance(@RequestParam(defaultValue = "80") int threshold) {
        int count = alertService.syncFromPerformance(threshold);
        return Result.success("已同步 " + count + " 条预警", count);
    }
}
