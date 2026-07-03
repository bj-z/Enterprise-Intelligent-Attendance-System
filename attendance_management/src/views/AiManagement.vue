<template>
    <div class="page-card">
        <div class="section-header">
            <h3 class="section-title">
                <el-icon class="section-icon"><Cpu /></el-icon>
                AI 服务配置
            </h3>
            <span class="section-desc">配置 AI API 连接信息，支持 DeepSeek / OpenAI / 自定义兼容接口</span>
        </div>

        <el-divider />

        <!-- AI 配置区域 -->
        <div class="config-section">
            <div class="config-card">
                <div class="config-label">API 请求地址</div>
                <div class="config-row">
                    <el-select v-model="apiUrl" placeholder="请选择 API 请求地址" class="url-select" clearable>
                        <el-option label="DeepSeek - https://api.deepseek.com" value="https://api.deepseek.com" />
                    </el-select>
                </div>

                <div class="config-label" style="margin-top:18px">API Key</div>
                <div class="config-row">
                    <el-input v-model="apiKey" placeholder="请输入 API Key" type="password" class="key-input" clearable show-password />
                    <el-button type="primary" @click="saveConfig" :loading="saving">
                        <el-icon class="btn-icon"><Check /></el-icon>保存
                    </el-button>
                </div>

                <div v-if="currentMasked" class="current-key-info">
                    <span class="info-label">已配置：</span>
                    <el-tag type="success" size="small">API Key <code class="masked-key">{{ currentMasked }}</code></el-tag>
                    <el-tag v-if="configuredUrl" type="primary" size="small" class="url-tag">{{ configuredUrl }}</el-tag>
                    <el-popconfirm title="确定要删除 AI 配置吗？删除后 AI 功能将不可用。" @confirm="deleteConfig">
                        <template #reference>
                            <el-button type="danger" size="small" :loading="deleting" class="delete-btn">
                                <el-icon class="btn-icon"><Delete /></el-icon>删除
                            </el-button>
                        </template>
                    </el-popconfirm>
                </div>
                <div v-else class="no-key-tip">
                    <el-icon><WarningFilled /></el-icon>
                    尚未配置 API Key，AI 功能暂不可用
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Cpu, Check, Delete, WarningFilled } from '@element-plus/icons-vue'
import api from '../api/index.js'

const apiKey = ref('')
const apiUrl = ref('')
const currentMasked = ref('')
const configuredUrl = ref('')
const saving = ref(false)
const deleting = ref(false)

// 加载当前配置
const loadConfig = async () => {
    try {
        const res = await api.get('/ai-config')
        if (res.code === 200 && res.data) {
            if (res.data.apiKey) {
                currentMasked.value = res.data.apiKey
            }
            if (res.data.apiUrl) {
                apiUrl.value = res.data.apiUrl
                configuredUrl.value = res.data.apiUrl
            }
        }
    } catch {}
}

// 保存配置
const saveConfig = async () => {
    if (!apiUrl.value) {
        ElMessage.warning('请选择 API 请求地址')
        return
    }
    if (!apiKey.value.trim()) {
        ElMessage.warning('请输入 API Key')
        return
    }
    saving.value = true
    try {
        const body = {
            apiKey: apiKey.value.trim(),
            apiUrl: apiUrl.value
        }
        const res = await api.post('/ai-config', body)
        if (res.code === 200) {
            ElMessage.success('保存成功')
            currentMasked.value = res.data.apiKey || ''
            if (res.data.apiUrl) {
                apiUrl.value = res.data.apiUrl
                configuredUrl.value = res.data.apiUrl
            }
            apiKey.value = ''
        }
    } catch {
        ElMessage.error('保存失败')
    }
    saving.value = false
}

// 删除配置
const deleteConfig = async () => {
    deleting.value = true
    try {
        await api.delete('/ai-config')
        ElMessage.success('已删除')
        currentMasked.value = ''
        configuredUrl.value = ''
    } catch {
        ElMessage.error('删除失败')
    }
    deleting.value = false
}

onMounted(() => {
    loadConfig()
})
</script>

<style scoped>
.page-card {
    background: #fff;
    border-radius: 16px;
    padding: 28px;
    box-shadow: 0 1px 6px rgba(0,0,0,.04);
    border: 1px solid #f1f3f6;
}

.section-header {
    margin-bottom: 8px;
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 4px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.section-icon {
    color: #3b7dd8;
    font-size: 18px;
}

.section-desc {
    font-size: 13px;
    color: #999;
}

.config-section {
    margin: 8px 0;
}

.config-card {
    background: #f8fafc;
    border-radius: 12px;
    padding: 24px;
    border: 1px solid #eef1f5;
}

.config-label {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 10px;
}

.config-row {
    display: flex;
    gap: 12px;
    align-items: center;
}

.url-select {
    width: 400px;
}

.key-input {
    flex: 1;
    max-width: 500px;
}

.url-tag {
    margin-left: 8px;
}

.btn-icon {
    margin-right: 4px;
}

.current-key-info {
    margin-top: 16px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.info-label {
    font-size: 13px;
    color: #666;
}

.masked-key {
    background: transparent;
    padding: 0 4px;
    font-size: 12px;
    color: #3b7dd8;
}

.delete-btn {
    margin-left: 4px;
}

.no-key-tip {
    margin-top: 16px;
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #e6a23c;
}</style>
