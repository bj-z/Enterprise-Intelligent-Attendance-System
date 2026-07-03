package com.attendance.service.impl;

import com.attendance.common.BusinessException;
import com.attendance.domain.AiConfig;
import com.attendance.repository.AiConfigRepository;
import com.attendance.service.AiConfigService;
import com.attendance.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConfigServiceImpl implements AiConfigService {

    private final AiConfigRepository aiConfigRepository;

    @Override
    @Transactional
    public AiConfig saveConfig(String apiKey, String apiUrl) {
        AiConfig config = aiConfigRepository.findTopByOrderByIdDesc().orElse(new AiConfig());
        // API Key 加密存储
        config.setApiKey(AesUtil.encrypt(apiKey));
        if (apiUrl != null && !apiUrl.isBlank()) {
            config.setApiUrl(apiUrl.trim());
        } else {
            config.setApiUrl(null);
        }
        AiConfig saved = aiConfigRepository.save(config);
        log.info("AI 配置已保存");
        return mask(saved);
    }

    @Override
    public AiConfig getMaskedConfig() {
        AiConfig config = aiConfigRepository.findTopByOrderByIdDesc().orElse(null);
        if (config == null) return null;
        AiConfig masked = mask(config);
        masked.setApiUrl(config.getApiUrl());
        masked.setAlertThreshold(config.getAlertThreshold() != null ? config.getAlertThreshold() : 80);
        return masked;
    }

    @Override
    @Transactional
    public void deleteApiKey() {
        AiConfig config = aiConfigRepository.findTopByOrderByIdDesc().orElse(null);
        if (config != null) {
            aiConfigRepository.delete(config);
            log.info("AI 配置已删除");
        }
    }

    @Override
    public String getApiKey() {
        AiConfig config = aiConfigRepository.findTopByOrderByIdDesc().orElse(null);
        if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new BusinessException("未配置 AI API Key，请在 AI 管理中设置");
        }
        // 解密返回
        return AesUtil.decrypt(config.getApiKey());
    }

    @Override
    public String getApiUrl() {
        AiConfig config = aiConfigRepository.findTopByOrderByIdDesc().orElse(null);
        if (config == null || config.getApiUrl() == null || config.getApiUrl().isBlank()) {
            throw new BusinessException("未配置 API 请求地址，请在 AI 管理中设置");
        }
        return config.getApiUrl();
    }

    @Override
    public int getAlertThreshold() {
        AiConfig config = aiConfigRepository.findTopByOrderByIdDesc().orElse(null);
        if (config == null || config.getAlertThreshold() == null) {
            return 80;
        }
        return config.getAlertThreshold();
    }

    @Override
    @Transactional
    public void setAlertThreshold(int threshold) {
        AiConfig config = aiConfigRepository.findTopByOrderByIdDesc().orElse(new AiConfig());
        config.setAlertThreshold(threshold);
        aiConfigRepository.save(config);
        log.info("预警阈值已更新: {} 分", threshold);
    }

    /**
     * 脱敏规则：后4位明文，往前10位用*替代，其余明文
     */
    private AiConfig mask(AiConfig config) {
        AiConfig masked = new AiConfig();
        masked.setId(config.getId());
        masked.setCreateTime(config.getCreateTime());
        masked.setUpdateTime(config.getUpdateTime());

        String key = config.getApiKey();
        if (key == null || key.isEmpty()) {
            masked.setApiKey("");
        } else {
            // 数据库存的是密文，解密后做脱敏展示
            try {
                String plain = key.length() > 50 ? AesUtil.decrypt(key) : key;
                if (plain.length() <= 14) {
                    masked.setApiKey("****");
                } else {
                    int len = plain.length();
                    String suffix = plain.substring(len - 4);
                    String middle = "*".repeat(Math.min(10, len - 8));
                    String prefix = plain.substring(0, len - 14);
                    masked.setApiKey(prefix + middle + suffix);
                }
            } catch (Exception e) {
                masked.setApiKey("****");
            }
        }
        return masked;
    }
}
