package com.attendance.service;

import com.attendance.domain.AiConfig;

public interface AiConfigService {
    AiConfig saveConfig(String apiKey, String apiUrl);
    AiConfig getMaskedConfig();
    void deleteApiKey();
    String getApiKey();
    String getApiUrl();
    int getAlertThreshold();
    void setAlertThreshold(int threshold);
}
