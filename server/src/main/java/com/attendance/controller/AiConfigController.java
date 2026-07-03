package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.domain.AiConfig;
import com.attendance.service.AiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI 配置管理")
@RestController
@RequestMapping("/api/ai-config")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigService aiConfigService;

    @Operation(summary = "保存 AI 配置（API Key、请求地址）")
    @PostMapping
    public Result<AiConfig> saveConfig(@RequestBody Map<String, String> body) {
        String apiKey = body.get("apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            return Result.badRequest("API Key 不能为空");
        }
        return Result.success("保存成功", aiConfigService.saveConfig(
                apiKey.trim(),
                body.get("apiUrl")
        ));
    }

    @Operation(summary = "获取 AI 配置（API Key 脱敏）")
    @GetMapping
    public Result<AiConfig> getConfig() {
        AiConfig config = aiConfigService.getMaskedConfig();
        return Result.success(config);
    }

    @Operation(summary = "删除 AI 配置")
    @DeleteMapping
    public Result<String> deleteConfig() {
        aiConfigService.deleteApiKey();
        return Result.success("AI 配置已删除");
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
        return Result.success("阈值已更新", threshold);
    }
}
