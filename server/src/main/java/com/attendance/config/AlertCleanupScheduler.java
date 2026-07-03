package com.attendance.config;

import com.attendance.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时清理超过15天的已处理预警记录
 * 每天凌晨2点执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertCleanupScheduler {

    private final AlertService alertService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldAlerts() {
        log.info("开始清理超过15天的已处理预警记录...");
        int deleted = alertService.cleanupOldProcessed();
        log.info("清理完成，共删除 {} 条过期预警记录", deleted);
    }
}
