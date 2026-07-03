package com.attendance.service.impl;

import com.attendance.domain.AttendanceAlert;
import com.attendance.domain.PerformanceRecord;
import com.attendance.service.AiAnalysisService;
import com.attendance.service.AiConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final AiConfigService aiConfigService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateAlertSummary(AttendanceAlert alert) {
        if (alert == null) return "";

        String prompt = String.format(
            "你是一个企业考勤管理系统的AI助手。请根据以下预警信息，用1-2句中文简洁概括员工的异常情况：" +
            "员工姓名：%s，部门：%s，职位：%s，预警等级：%s。请直接给出概述，不要前缀。",
            alert.getEmployeeName(), alert.getDepartmentName(), alert.getPosition(), alert.getLevel()
        );
        return callAi(prompt);
    }

    @Override
    public String generateAlertSuggestion(AttendanceAlert alert) {
        if (alert == null) return "";

        String prompt = String.format(
            "你是一个企业考勤管理系统的AI助手。请根据以下异常预警信息，用1-2句中文给出管理建议：" +
            "员工姓名：%s，部门：%s，预警等级：%s，异常摘要：%s。请直接给出建议，不要前缀。",
            alert.getEmployeeName(), alert.getDepartmentName(), alert.getLevel(), alert.getSummary()
        );
        return callAi(prompt);
    }

    @Override
    public Map<String, Object> generatePerformanceAnalysis(PerformanceRecord record) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (record == null) return result;

        // 判断哪些字段需要AI生成
        boolean needAttendanceScore = record.getAttendanceScore() == null;
        boolean needFinalScore = record.getFinalScore() == null;
        boolean needAiComment = record.getAiComment() == null || record.getAiComment().isBlank();

        if (!needAttendanceScore && !needFinalScore && !needAiComment) {
            return result; // 无需生成
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个企业绩效分析助手。根据以下员工数据，");
        
        // 告知AI需要生成哪些字段
        List<String> tasks = new ArrayList<>();
        if (needAttendanceScore) tasks.add("考勤得分(0-100的整数)");
        if (needFinalScore) tasks.add("综合评分(0-100的整数)和评分等级(优秀/良好/一般/待改进)");
        if (needAiComment) tasks.add("AI综合评语(1-2句中肯评价)");
        prompt.append("请生成").append(String.join("、", tasks)).append("。\n\n");
        
        prompt.append("员工信息：\n");
        prompt.append("姓名：").append(record.getEmployeeName()).append("\n");
        prompt.append("部门：").append(record.getDepartmentName()).append("\n");
        prompt.append("职位：").append(record.getPosition() != null ? record.getPosition() : "未知").append("\n");
        prompt.append("本月迟到次数：").append(record.getLateCount() != null ? record.getLateCount() : 0).append("\n");
        prompt.append("本月早退次数：").append(record.getEarlyCount() != null ? record.getEarlyCount() : 0).append("\n");
        prompt.append("请假天数：").append(record.getLeaveDays() != null ? record.getLeaveDays() : 0).append("\n");
        if (record.getAttendanceRate() != null) {
            prompt.append("出勤率：").append(record.getAttendanceRate()).append("%\n");
        }
        if (record.getTaskScore() != null) {
            prompt.append("任务完成得分：").append(record.getTaskScore()).append("\n");
        }
        if (record.getCollaborationScore() != null) {
            prompt.append("协作得分：").append(record.getCollaborationScore()).append("\n");
        }

        prompt.append("\n请只返回一个JSON对象，不要任何其他文字。JSON格式如下：{\n");
        if (needAttendanceScore) prompt.append("  \"attendanceScore\": 数字,\n");
        if (needFinalScore) {
            prompt.append("  \"finalScore\": 数字,\n");
            prompt.append("  \"scoreLevel\": \"优秀/良好/一般/待改进\",\n");
        }
        if (needAiComment) prompt.append("  \"aiComment\": \"评语内容\"\n");
        prompt.append("}");

        String response = callAi(prompt.toString());
        
        try {
            JsonNode root = objectMapper.readTree(response);
            if (needAttendanceScore && root.has("attendanceScore")) {
                result.put("attendanceScore", BigDecimal.valueOf(root.get("attendanceScore").asDouble()));
            }
            if (needFinalScore && root.has("finalScore")) {
                result.put("finalScore", BigDecimal.valueOf(root.get("finalScore").asDouble()));
            }
            if (needFinalScore && root.has("scoreLevel")) {
                result.put("scoreLevel", root.get("scoreLevel").asText());
            }
            if (needAiComment && root.has("aiComment")) {
                result.put("aiComment", root.get("aiComment").asText());
            }
        } catch (Exception e) {
            log.warn("解析AI返回的绩效JSON失败: {}", response, e);
        }
        return result;
    }

    private String callAi(String userMessage) {
        String apiUrl = null;
        try {
            String apiKey = aiConfigService.getApiKey();
            apiUrl = aiConfigService.getApiUrl();

            // 自动补全路径：如果用户只填了基础域名，补充 /v1/chat/completions
            if (!apiUrl.contains("/chat/completions")) {
                apiUrl = apiUrl.replaceAll("/+$", "") + "/v1/chat/completions";
            }

            Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                    Map.of("role", "system", "content", "你是一个专业、简洁的企业管理AI助手。" +
                            "你必须严格只返回JSON格式，不要有任何前缀、标签、markdown代码块或多余文字。"),
                    Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", 500,
                "temperature", 0.7
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            log.info("调用 AI API 进行绩效分析: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                log.info("AI 绩效分析返回: {}", content);
                return content.trim();
            }
            log.warn("AI API 返回非 200: {}", response.getStatusCode());
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("404")) {
                log.warn("AI API 调用失败(404): 请求地址不存在，实际请求: {}", apiUrl);
            } else {
                log.warn("AI API 调用失败: {}", msg);
            }
        }
        return "{}";
    }
}
