package com.attendance.controller;

import com.attendance.common.PageResult;
import com.attendance.common.Result;
import com.attendance.domain.PerformanceRecord;
import com.attendance.service.AiAnalysisService;
import com.attendance.service.PerformanceService;
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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Slf4j
@Tag(name = "绩效分析")
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;
    private final AiAnalysisService aiAnalysisService;
    private final ObjectMapper objectMapper;

    @Autowired
    @Qualifier("aiTaskExecutor")
    private Executor aiTaskExecutor;

    @Operation(summary = "分页查询绩效列表")
    @GetMapping
    public Result<PageResult<PerformanceRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String department) {
        return Result.success(performanceService.listByPage(page, pageSize, department));
    }

    @Operation(summary = "AI流式生成绩效分析（仅当前页、仅空字段）")
    @PostMapping(value = "/ai-analyze", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter aiAnalyze(@RequestBody List<Long> ids) {
        SseEmitter emitter = new SseEmitter(300_000L);

        aiTaskExecutor.execute(() -> {
            try {
                List<PerformanceRecord> records = performanceService.findByIds(ids);
                for (int i = 0; i < records.size(); i++) {
                    PerformanceRecord record = records.get(i);

                    Map<String, Object> aiResult = aiAnalysisService.generatePerformanceAnalysis(record);

                    Map<String, Object> eventData = new LinkedHashMap<>();
                    eventData.put("id", record.getId());
                    eventData.put("index", i);
                    eventData.put("total", records.size());

                    if (!aiResult.isEmpty()) {
                        if (aiResult.containsKey("attendanceScore")) {
                            record.setAttendanceScore((BigDecimal) aiResult.get("attendanceScore"));
                        }
                        if (aiResult.containsKey("finalScore")) {
                            record.setFinalScore((BigDecimal) aiResult.get("finalScore"));
                        }
                        if (aiResult.containsKey("scoreLevel")) {
                            record.setScoreLevel((String) aiResult.get("scoreLevel"));
                        }
                        if (aiResult.containsKey("aiComment")) {
                            record.setAiComment((String) aiResult.get("aiComment"));
                        }
                        performanceService.save(record);
                        eventData.put("data", aiResult);
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
                log.error("AI绩效分析流式处理失败", e);
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

        List<PerformanceRecord> records = performanceService.findByIds(ids);
        for (PerformanceRecord r : records) {
            switch (column) {
                case "attendanceScore":
                    r.setAttendanceScore(null);
                    break;
                case "finalScore":
                    r.setFinalScore(null);
                    r.setScoreLevel(null);
                    break;
                case "aiComment":
                    r.setAiComment(null);
                    break;
            }
        }
        performanceService.saveAll(records);
        return Result.success();
    }
}
