<template>
    <div class="page-card">
        <div class="page-toolbar">
            <div class="toolbar-right">
                <el-select v-model="filterDept" placeholder="选择部门" clearable style="width:140px;margin-right:20px">
                    <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
                </el-select>
                <el-button type="primary" :loading="analyzing" @click="startAiAnalysis">
                    <el-icon class="btn-icon"><MagicStick /></el-icon>AI 生成分析报告
                </el-button>
            </div>
        </div>

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
            <el-table-column prop="employeeId" label="工号" width="100" align="center" />
            <el-table-column prop="employeeName" label="姓名" width="100" align="center" />
            <el-table-column prop="departmentName" label="部门" width="100" align="center" />
            <el-table-column prop="position" label="职位" width="120" align="center" />
            <el-table-column prop="attendanceScore" label="考勤得分" width="120" align="center">
                <template #header>
                    <div class="col-header">
                        <span>考勤得分</span>
                        <el-icon class="clear-col-icon" title="重置本页考勤得分" @click="resetColumn('attendanceScore')"><Refresh /></el-icon>
                    </div>
                </template>
                <template #default="{row}">
                    <el-icon v-if="isLoadingCol(row.id, 'attendanceScore')" class="cell-loading"><Loading /></el-icon>
                    <span v-else-if="row.attendanceScore != null">{{ row.attendanceScore }}</span>
                    <span v-else class="cell-empty">-</span>
                </template>
            </el-table-column>
            <el-table-column prop="lateCount" label="本月迟到" width="90" align="center" />
            <el-table-column prop="earlyCount" label="本月早退" width="90" align="center" />
            <el-table-column prop="leaveDays" label="请假天数" width="90" align="center" />
            <el-table-column prop="finalScore" label="综合评分" width="120" align="center">
                <template #header>
                    <div class="col-header">
                        <span>综合评分</span>
                        <el-icon class="clear-col-icon" title="重置本页综合评分" @click="resetColumn('finalScore')"><Refresh /></el-icon>
                    </div>
                </template>
                <template #default="{row}">
                    <el-icon v-if="isLoadingCol(row.id, 'finalScore')" class="cell-loading"><Loading /></el-icon>
                    <el-tag v-else-if="row.finalScore != null" :type="scoreTag[row.scoreLevel]" size="small">{{row.finalScore}}分</el-tag>
                    <span v-else class="cell-empty">-</span>
                </template>
            </el-table-column>
            <el-table-column prop="aiComment" label="AI综合评语" min-width="190" align="center">
                <template #header>
                    <div class="col-header">
                        <span>AI综合评语</span>
                        <el-icon class="clear-col-icon" title="重置本页AI综合评语" @click="resetColumn('aiComment')"><Refresh /></el-icon>
                    </div>
                </template>
                <template #default="{row}">
                    <el-icon v-if="isLoadingCol(row.id, 'aiComment')" class="cell-loading"><Loading /></el-icon>
                    <div v-else-if="row.aiComment" class="ai-text">{{row.aiComment}}</div>
                    <span v-else class="cell-empty">-</span>
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination-bar">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[5,10,20]" layout="total,sizes,prev,pager,next" :total="total" @current-change="fetchPerformance" />
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { MagicStick, Loading, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api/index.js'

const filterDept = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const departments = ['技术部','市场部','财务部','行政部','研发部','产品部','销售部']
const scoreTag = {优秀:'success',良好:'primary',一般:'warning',待改进:'danger'}

const performanceList = ref([])
const analyzing = ref(false)
// { [rowId]: { attendanceScore: bool, finalScore: bool, aiComment: bool } }
const loadingMap = ref({})

const isLoadingCol = (rowId, col) => {
  return !!loadingMap.value[rowId]?.[col]
}

const fetchPerformance = async () => {
  try {
    const params = { page: currentPage.value, pageSize: pageSize.value, department: filterDept.value || undefined }
    const res = await api.get('/performance', { params })
    if (res.code === 200) {
      performanceList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {}
}

const displayList = computed(() => performanceList.value)

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

const startAiAnalysis = async () => {
  // 检查 AI API 是否已配置
  const hasConfig = await checkAiConfig()
  if (!hasConfig) {
    ElMessageBox.alert('尚未配置 AI API Key，AI 功能暂不可用。请先前往「AI 管理」页面配置 API Key 和请求地址。', '提示', {
      confirmButtonText: '我知道了',
      type: 'warning'
    })
    return
  }

  const records = performanceList.value
  const ids = records.map(r => r.id)
  if (ids.length === 0) return

  analyzing.value = true

  // 构建 loadingMap：只对当前为空的列标记 loading
  const map = {}
  for (const r of records) {
    const cols = {}
    if (r.attendanceScore == null) cols.attendanceScore = true
    if (r.finalScore == null) cols.finalScore = true
    if (!r.aiComment) cols.aiComment = true
    if (Object.keys(cols).length > 0) {
      map[r.id] = cols
    }
  }
  loadingMap.value = map

  const token = localStorage.getItem('token')

  try {
    const response = await fetch('/api/performance/ai-analyze', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify(ids)
    })

    if (!response.ok) {
      // HTTP 错误（如 401）
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
            fetchPerformance() // 完成后再从后端拉一次，确保数据一致
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
    // 该行所有字段都已填满，清除该行 loading
    delete loadingMap.value[id]
    loadingMap.value = { ...loadingMap.value }
    return
  }

  // 更新表格行数据
  const idx = performanceList.value.findIndex(r => r.id === id)
  if (idx >= 0) {
    const record = performanceList.value[idx]
    if (data.attendanceScore != null) record.attendanceScore = data.attendanceScore
    if (data.finalScore != null) record.finalScore = data.finalScore
    if (data.scoreLevel != null) record.scoreLevel = data.scoreLevel
    if (data.aiComment != null) record.aiComment = data.aiComment
    performanceList.value = [...performanceList.value]
  }

  // 该行生成完毕，移除 loading
  delete loadingMap.value[id]
  loadingMap.value = { ...loadingMap.value }
}

const resetColumn = async (column) => {
  const labels = { attendanceScore: '考勤得分', finalScore: '综合评分', aiComment: 'AI综合评语' }
  try {
    await ElMessageBox.confirm(`确定要清空当前页所有「${labels[column]}」吗？`, '重置确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
  } catch { return }

  const ids = performanceList.value.map(r => r.id)
  try {
    const res = await api.post('/performance/reset-column', { ids, column })
    if (res.code === 200) {
      fetchPerformance()
      ElMessage.success(`已重置${labels[column]}`)
    }
  } catch {}
}

onMounted(() => { fetchPerformance() })
watch(filterDept, () => { currentPage.value = 1; fetchPerformance() })
</script>

<style scoped>
.page-card{background:#fff;border-radius:16px;padding:24px;box-shadow:0 1px 6px rgba(0,0,0,.04);border:1px solid #f1f3f6}
.page-toolbar{display:flex;justify-content:flex-end;align-items:center;flex-wrap:wrap;gap:10px;margin-bottom:8px}
.data-table{margin-top:16px;--el-table-border-color:#e5e7eb;--el-table-row-hover-bg-color:#eef2ff;--el-table-striped-row-bg-color:#f8fafc;width:100% !important}
.data-table :deep(.el-table__cell){padding:16px 0;font-size:14px;color:#334155}
.data-table :deep(.el-table__header-cell){padding:12px 0;font-size:14px}
.data-table :deep(.el-table__body){width:100% !important}
.data-table :deep(.el-table__header){width:100% !important}
.ai-text{font-size:13px;color:#666;line-height:1.5}
.pagination-bar{display:flex;justify-content:flex-end;margin-top:24px}

.col-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
}
.cell-empty{color:#c0c4cc;font-size:14px}
.cell-loading {
    font-size: 20px;
    color: #409eff;
    animation: rotating 1s linear infinite;
}
@keyframes rotating {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
</style>
