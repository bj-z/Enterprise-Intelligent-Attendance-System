<template>
    <div class="page-card">
        <div class="page-toolbar">
            <div class="toolbar-left">
                <span class="toolbar-title">异常预警</span>
                <span class="toolbar-desc">AI智能识别考勤异常，辅助管理员快速跟进</span>
            </div>
            <div class="toolbar-right">
                <div class="threshold-module">
                    <span class="threshold-label">预警阈值</span>
                    <el-select v-model="threshold" placeholder="阈值" style="width:130px" @change="onThresholdChange">
                        <el-option v-for="t in thresholdOptions" :key="t.value" :label="t.label" :value="t.value" />
                    </el-select>
                    <span class="threshold-desc">分以下</span>
                </div>
                <el-select v-model="filterLevel" placeholder="风险等级" clearable style="width:130px" @change="searchAlerts">
                    <el-option v-for="l in levels" :key="l" :label="l+'风险'" :value="l" />
                </el-select>
                <el-button type="primary" @click="startAiAnalysis" :loading="analyzing">
                    <el-icon class="btn-icon" v-if="!analyzing"><MagicStick /></el-icon>AI 批量分析
                </el-button>
            </div>
        </div>

        <el-tabs v-model="activeTab" class="alert-tabs" @tab-change="onTabChange">
            <el-tab-pane label="待处理" name="pending">
                <el-table
                    :data="displayList"
                    class="data-table"
                    :header-cell-style="{background:'#f8fafc',color:'#64748b',fontWeight:'500'}"
                    :cell-style="{padding:'16px 0'}"
                    width="100%"
                    border
                    stripe
                >
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="alertNo" label="预警编号" width="100" align="center" />
                    <el-table-column prop="employeeName" label="员工" width="100" align="center" />
                    <el-table-column prop="departmentName" label="部门" width="100" align="center" />
                    <el-table-column prop="position" label="职位" width="120" align="center" />
                    <el-table-column prop="phone" label="手机号" width="130" align="center" />
                    <el-table-column prop="level" label="风险等级" width="130" align="center">
                        <template #header>
                            <div class="col-header">
                                <span>风险等级</span>
                                <el-icon class="clear-col-icon" @click.stop="resetColumn('level')" title="重置本页风险等级"><Refresh /></el-icon>
                            </div>
                        </template>
                        <template #default="{row}">
                            <el-icon v-if="isLoadingCol(row.id, 'level')" class="cell-loading"><Loading /></el-icon>
                            <el-tag v-else-if="row.level" :type="levelTags[row.level]" size="small">{{row.level}}风险</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="summary" label="异常概述" min-width="200">
                        <template #header>
                            <div class="col-header">
                                <span>异常概述</span>
                                <el-icon class="clear-col-icon" @click.stop="resetColumn('summary')" title="重置本页异常概述"><Refresh /></el-icon>
                            </div>
                        </template>
                        <template #default="{row}">
                            <el-icon v-if="isLoadingCol(row.id, 'summary')" class="cell-loading"><Loading /></el-icon>
                            <span v-else-if="row.summary">{{ row.summary }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="aiSuggestion" label="AI建议" min-width="220">
                        <template #header>
                            <div class="col-header">
                                <span>AI建议</span>
                                <el-icon class="clear-col-icon" @click.stop="resetColumn('aiSuggestion')" title="重置本页AI建议"><Refresh /></el-icon>
                            </div>
                        </template>
                        <template #default="{row}">
                            <el-icon v-if="isLoadingCol(row.id, 'aiSuggestion')" class="cell-loading"><Loading /></el-icon>
                            <span v-else-if="row.aiSuggestion">{{ row.aiSuggestion }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="预警时间" width="170" align="center" />
                    <el-table-column label="操作" width="120" align="center" fixed="right">
                        <template #default="{row}">
                            <el-button type="warning" size="small" @click="markHandled(row)">
                                <el-icon class="btn-icon"><CircleCheck /></el-icon>标记已处理
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="pagination-bar">
                    <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[5,10,20]" layout="total,sizes,prev,pager,next" :total="total" @current-change="fetchAlerts" />
                </div>
            </el-tab-pane>

            <el-tab-pane label="已处理" name="processed">
                <el-table
                    :data="displayList"
                    class="data-table"
                    :header-cell-style="{background:'#f8fafc',color:'#64748b',fontWeight:'500'}"
                    :cell-style="{padding:'16px 0'}"
                    width="100%"
                    border
                    stripe
                >
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="alertNo" label="预警编号" width="140" align="center" />
                    <el-table-column prop="employeeName" label="员工" width="100" align="center" />
                    <el-table-column prop="departmentName" label="部门" width="100" align="center" />
                    <el-table-column prop="level" label="风险等级" width="100" align="center">
                        <template #default="{row}"><el-tag :type="levelTags[row.level]" size="small">{{row.level}}风险</el-tag></template>
                    </el-table-column>
                    <el-table-column prop="summary" label="异常概述" min-width="200" show-overflow-tooltip />
                    <el-table-column prop="handler" label="处理人" width="120" align="center" />
                    <el-table-column prop="handleTime" label="处理时间" width="170" align="center" />
                </el-table>
                <div class="pagination-bar">
                    <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[5,10,20]" layout="total,sizes,prev,pager,next" :total="total" @current-change="fetchAlerts" />
                </div>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Refresh, Loading } from '@element-plus/icons-vue'
import api from '../api/index.js'

const activeTab = ref('pending')
const filterLevel = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const levels = ['高','中','低']
const levelTags = {高:'danger',中:'warning',低:'info'}
const alertList = ref([])
const total = ref(0)
const analyzing = ref(false)
const threshold = ref(80)
const thresholdOptions = [
    { label: '60分', value: 60 },
    { label: '70分', value: 70 },
    { label: '80分', value: 80 }
]
// { [rowId]: { summary: bool, aiSuggestion: bool, level: bool } }
const loadingMap = ref({})

const isLoadingCol = (rowId, col) => {
  return !!loadingMap.value[rowId]?.[col]
}

const fetchAlerts = async () => {
  try {
    const status = activeTab.value === 'pending' ? 'pending' : '已关闭'
    const res = await api.get('/alerts', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        status: status,
        level: filterLevel.value || undefined,
        threshold: threshold.value
      }
    })
    if (res.code === 200) {
      alertList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {}
}

const fetchThreshold = async () => {
  try {
    const res = await api.get('/alerts/threshold')
    if (res.code === 200 && res.data != null) {
      threshold.value = res.data
    }
  } catch {}
}

const onThresholdChange = async (val) => {
  try {
    await api.put('/alerts/threshold', { threshold: val })
    ElMessage.success(`预警阈值已更新为 ${val} 分`)
  } catch {
    ElMessage.error('阈值更新失败')
  }
  currentPage.value = 1
  fetchAlerts()
}

const searchAlerts = () => { currentPage.value = 1; fetchAlerts() }
const onTabChange = () => { currentPage.value = 1; filterLevel.value = ''; fetchAlerts() }
const displayList = computed(() => alertList.value)

// 重置当前页指定列
const resetColumn = async (column) => {
  const labels = { summary: '异常概述', aiSuggestion: 'AI建议', level: '风险等级' }
  try {
    await ElMessageBox.confirm(`确定要清空当前页所有「${labels[column]}」吗？`, '重置确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
  } catch { return }

  const ids = alertList.value.map(r => r.id)
  try {
    const res = await api.post('/alerts/reset-column', { ids, column })
    if (res.code === 200) {
      fetchAlerts()
      ElMessage.success(`已重置${labels[column]}`)
    }
  } catch {}
}

// 检查 AI API 是否已配置（Key + 地址 缺一不可）
const checkAiConfig = async () => {
  try {
    const res = await api.get('/ai-config')
    if (res.code === 200 && res.data && res.data.apiKey && res.data.apiUrl) {
      return true
    }
    return false
  } catch {
    return false
  }
}

// SSE 流式 AI 批量分析（只生成空字段）
const startAiAnalysis = async () => {
  if (activeTab.value !== 'pending') return

  // 检查 AI API 是否已配置
  const hasConfig = await checkAiConfig()
  if (!hasConfig) {
    ElMessageBox.alert('尚未配置 AI API Key，AI 功能暂不可用。请先前往「AI 管理」页面配置 API Key 和请求地址。', '提示', {
      confirmButtonText: '我知道了',
      type: 'warning'
    })
    return
  }

  const records = alertList.value
  const ids = records.map(r => r.id)
  if (ids.length === 0) return

  analyzing.value = true

  // 构建 loadingMap：只对当前为空的列标记 loading
  const map = {}
  for (const r of records) {
    const cols = {}
    if (!r.summary) cols.summary = true
    if (!r.aiSuggestion) cols.aiSuggestion = true
    if (!r.level) cols.level = true
    if (Object.keys(cols).length > 0) {
      map[r.id] = cols
    }
  }
  loadingMap.value = map

  const token = localStorage.getItem('token')

  try {
    const response = await fetch('/api/alerts/ai-analyze', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify(ids)
    })

    if (!response.ok) {
      const errText = await response.text()
      loadingMap.value = {}
      analyzing.value = false
      ElMessage.error(errText || '请求失败')
      return
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim()
        if (!line.startsWith('data:')) continue
        const jsonStr = line.substring(5).trim()
        if (!jsonStr) continue

        try {
          const eventData = JSON.parse(jsonStr)
          const eventLine = i > 0 ? lines[i-1].trim() : ''
          const eventName = eventLine.startsWith('event:') ? eventLine.substring(6).trim() : ''

          if (eventName === 'progress') {
            handleProgress(eventData)
          } else if (eventName === 'done') {
            loadingMap.value = {}
            analyzing.value = false
            fetchAlerts()
            ElMessage.success('AI 分析完成')
          } else if (eventName === 'error') {
            loadingMap.value = {}
            analyzing.value = false
            ElMessage.error(eventData.message || 'AI 分析失败')
          }
        } catch {}
      }
    }
  } catch (e) {
    loadingMap.value = {}
    analyzing.value = false
    ElMessage.error('AI 分析请求失败')
  }
}

const handleProgress = (eventData) => {
  const { id, data } = eventData

  if (!data || data.skipped) {
    delete loadingMap.value[id]
    loadingMap.value = { ...loadingMap.value }
    return
  }

  const idx = alertList.value.findIndex(r => r.id === id)
  if (idx >= 0) {
    const record = alertList.value[idx]
    if (data.summary != null) record.summary = data.summary
    if (data.aiSuggestion != null) record.aiSuggestion = data.aiSuggestion
    if (data.level != null) record.level = data.level
    alertList.value = [...alertList.value]
  }

  delete loadingMap.value[id]
  loadingMap.value = { ...loadingMap.value }
}

const markHandled = async (row) => {
  try {
    const handler = localStorage.getItem('role') || '系统管理员'
    const res = await api.put(`/alerts/${row.id}/handle`, null, { params: { handler } })
    if (res.code === 200) {
      ElMessage.success('已标记为已处理')
      fetchAlerts()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch { ElMessage.error('网络错误，请稍后重试') }
}

onMounted(() => { fetchThreshold(); fetchAlerts() })
</script>

<style scoped>
.page-card{background:#fff;border-radius:16px;padding:24px;box-shadow:0 1px 6px rgba(0,0,0,.04);border:1px solid #f1f3f6}
.page-toolbar{display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px;margin-bottom:8px}
.toolbar-left{display:flex;flex-direction:column}
.toolbar-title{font-size:18px;font-weight:600;color:#333}
.toolbar-desc{font-size:13px;color:#999;margin-top:2px}
.toolbar-right{display:flex;align-items:center;gap:10px}

.threshold-module {
    display: flex;
    align-items: center;
    gap: 6px;
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 0 12px;
    height: 32px;
}
.threshold-label {
    font-size: 13px;
    color: #64748b;
    font-weight: 500;
}
.threshold-module :deep(.el-select) {
    width: 100px;
}
.threshold-module :deep(.el-select .el-input__wrapper) {
    box-shadow: none;
    background: transparent;
    padding: 0;
}
.threshold-module :deep(.el-select .el-input__inner) {
    text-align: center;
    font-weight: 600;
    color: #e6a23c;
}
.threshold-desc {
    font-size: 12px;
    color: #999;
}

.alert-tabs :deep(.el-tabs__header){margin-bottom:16px}
.alert-tabs :deep(.el-tabs__nav-wrap::after){height:1px;background-color:#e5e7eb}
.alert-tabs :deep(.el-tabs__item){font-size:15px;font-weight:500;padding:0 20px}

.data-table{margin-top:8px;--el-table-border-color:#e5e7eb;--el-table-row-hover-bg-color:#eef2ff;--el-table-striped-row-bg-color:#f8fafc;width:100% !important}
.data-table :deep(.el-table__cell){padding:16px 0;font-size:14px;color:#334155}
.data-table :deep(.el-table__header-cell){padding:12px 0;font-size:14px}
.data-table :deep(.el-table__body){width:100% !important}
.data-table :deep(.el-table__header){width:100% !important}

.col-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
}
.clear-col-icon {
    font-size: 14px;
    color: #c0c4cc;
    cursor: pointer;
    transition: color 0.15s;
}
.clear-col-icon:hover {
    color: #f56c6c;
}

.cell-loading {
    font-size: 20px;
    color: #409eff;
    animation: rotating 1s linear infinite;
}
@keyframes rotating {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

.pagination-bar{display:flex;justify-content:flex-end;margin-top:24px}
.btn-icon{margin-right:4px}
</style>
