<template>
    <div class="page-card">
        <div class="page-toolbar">
            <div class="toolbar-right">
                <el-button type="warning" @click="judgeRecords" :loading="judging">
                    <el-icon class="btn-icon"><RefreshRight /></el-icon>刷新判定
                </el-button>
                <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" />
                <el-select v-model="filterDept" placeholder="选择部门" clearable style="width:140px">
                    <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
                </el-select>
                <el-button type="primary" @click="searchRecords">查询</el-button>
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
            <el-table-column prop="recordDate" label="日期" width="120" align="center" />
            <el-table-column label="周几" width="70" align="center">
                <template #default="{row}">{{ dayOfWeek(row.recordDate) }}</template>
            </el-table-column>
            <el-table-column prop="clockIn" label="上班打卡" width="120" align="center">
                <template #default="{row}"><span :class="{'late-text':row.isLate}">{{row.clockIn}}</span></template>
            </el-table-column>
            <el-table-column prop="clockOut" label="下班打卡" width="120" align="center">
                <template #default="{row}"><span :class="{'early-text':row.isEarly}">{{row.clockOut}}</span></template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{row}"><el-tag :type="statusTag[row.status]" size="small">{{row.status}}</el-tag></template>
            </el-table-column>
        </el-table>

        <div class="pagination-bar">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" :total="total" @current-change="fetchRecords" />
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api/index.js'

const dateRange = ref([])
const filterDept = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const judging = ref(false)
const departments = ['技术部','市场部','财务部','行政部']
const statusTag = {正常:'success',迟到:'warning',早退:'danger',缺卡:'info'}
const recordList = ref([])
const total = ref(0)

const weekMap = ['周日','周一','周二','周三','周四','周五','周六']
const dayOfWeek = (dateStr) => {
    if (!dateStr) return '--'
    const d = new Date(dateStr)
    return weekMap[d.getDay()]
}

const fetchRecords = async () => {
  try {
    const params = { page: currentPage.value, pageSize: pageSize.value, department: filterDept.value || undefined }
    if (dateRange.value && dateRange.value.length === 2) {
      const fmt = (d) => {
        const date = new Date(d)
        return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0')
      }
      params.startDate = fmt(dateRange.value[0])
      params.endDate = fmt(dateRange.value[1])
    }
    const res = await api.get('/attendance/records', { params })
    if (res.code === 200) {
      recordList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {}
}

const searchRecords = () => { currentPage.value = 1; fetchRecords() }

const judgeRecords = async () => {
    judging.value = true
    try {
        const res = await api.post('/attendance/judge')
        if (res.code === 200) {
            ElMessage.success(res.message || '考勤判定完成')
            fetchRecords()
        }
    } catch {
        ElMessage.error('判定失败，请稍后重试')
    }
    judging.value = false
}

const displayList = computed(() => recordList.value)
onMounted(() => { fetchRecords() })
</script>

<style scoped>
.page-card{background:#fff;border-radius:16px;padding:24px;box-shadow:0 1px 6px rgba(0,0,0,.04);border:1px solid #f1f3f6}
.page-toolbar{display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px}
.toolbar-left{display:flex;flex-direction:column}
.toolbar-title{font-size:18px;font-weight:600;color:#333}
.toolbar-desc{font-size:13px;color:#999;margin-top:2px}
.toolbar-right{display:flex;align-items:center;gap:10px;flex-wrap:wrap}
.data-table{margin-top:16px;--el-table-border-color:#e5e7eb;--el-table-row-hover-bg-color:#eef2ff;--el-table-striped-row-bg-color:#f8fafc;width:100% !important}
.data-table :deep(.el-table__cell){padding:16px 0;font-size:14px;color:#334155}
.data-table :deep(.el-table__header-cell){padding:12px 0;font-size:14px}
.data-table :deep(.el-table__body){width:100% !important}
.data-table :deep(.el-table__header){width:100% !important}
.late-text{color:#f59e0b;font-weight:600}
.early-text{color:#ef4444;font-weight:600}
.pagination-bar{display:flex;justify-content:flex-end;margin-top:24px}
</style>
