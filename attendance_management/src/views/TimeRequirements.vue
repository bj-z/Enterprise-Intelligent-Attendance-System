<template>
    <div class="page-card">
        <!-- 总体设置区域 -->
        <div class="global-section">
            <div class="section-header">
                <h3 class="section-title">
                    <el-icon class="section-icon"><Clock /></el-icon>
                    总体时间设置
                </h3>
                <span class="section-desc">统一设置所有部门的打卡时间要求，也可单独调整各部门</span>
            </div>
            <div class="global-time-row">
                <div v-for="day in weekdays" :key="day.key" class="time-item">
                    <span class="day-label">{{ day.label }}</span>
                    <el-time-picker
                        v-model="globalTimes[day.key]"
                        format="HH:mm"
                        value-format="HH:mm"
                        placeholder="上班时间"
                        class="time-picker"
                        :clearable="false"
                    />
                </div>
                <el-button type="primary" @click="applyGlobalToAll" class="apply-btn">
                    <el-icon class="btn-icon"><Select /></el-icon>
                    应用到所有部门
                </el-button>
            </div>
        </div>

        <el-divider />

        <!-- 各部门时间要求 -->
        <div class="department-section">
            <div class="section-header">
                <h3 class="section-title">
                    <el-icon class="section-icon"><OfficeBuilding /></el-icon>
                    各部门打卡时间要求
                </h3>
                <span class="section-desc">各部门可独立设置周一至周五的上班打卡截止时间</span>
            </div>

            <el-table :data="departmentTimeList" border stripe class="data-table"
                :header-cell-style="{background:'#f8fafc',color:'#64748b',fontWeight:'500'}"
                row-key="deptId">
                <el-table-column prop="deptName" label="部门" width="130" align="center" fixed />
                <el-table-column v-for="day in weekdays" :key="day.key" :label="day.label" align="center" min-width="130">
                    <template #default="scope">
                        <el-time-picker
                            v-model="scope.row.times[day.key]"
                            format="HH:mm"
                            value-format="HH:mm"
                            placeholder="选择时间"
                            style="width:110px"
                            :clearable="false"
                        />
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center" fixed="right">
                    <template #default="scope">
                        <el-button type="primary" size="small" @click="saveDepartmentTime(scope.row)">
                            <el-icon class="btn-icon"><Check /></el-icon>保存
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 保存全部按钮 -->
        <div class="save-all-bar">
            <el-button type="primary" size="large" @click="saveAll" :loading="saving">
                <el-icon class="btn-icon"><DocumentChecked /></el-icon>
                保存全部设置
            </el-button>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Select, OfficeBuilding, Check, DocumentChecked } from '@element-plus/icons-vue'
import api from '../api/index.js'

const weekdays = [
    { key: 'monday', label: '周一', field: 'mondayTime' },
    { key: 'tuesday', label: '周二', field: 'tuesdayTime' },
    { key: 'wednesday', label: '周三', field: 'wednesdayTime' },
    { key: 'thursday', label: '周四', field: 'thursdayTime' },
    { key: 'friday', label: '周五', field: 'fridayTime' }
]

// 总体设置的时间
const globalTimes = reactive({
    monday: '09:00',
    tuesday: '09:00',
    wednesday: '09:00',
    thursday: '09:00',
    friday: '09:00'
})

const departmentTimeList = ref([])
const saving = ref(false)

// 后端数据字段 -> 前端 times 对象
const toFrontendTimes = (item) => ({
    monday: item.mondayTime || '09:00',
    tuesday: item.tuesdayTime || '09:00',
    wednesday: item.wednesdayTime || '09:00',
    thursday: item.thursdayTime || '09:00',
    friday: item.fridayTime || '09:00'
})

// 前端 times 对象 -> 后端请求体
const toBackendBody = (row) => ({
    mondayTime: row.times.monday,
    tuesdayTime: row.times.tuesday,
    wednesdayTime: row.times.wednesday,
    thursdayTime: row.times.thursday,
    fridayTime: row.times.friday
})

// 从后端加载数据
const loadData = async () => {
    try {
        const res = await api.get('/time-requirements')
        if (res.code === 200 && res.data) {
            departmentTimeList.value = res.data.map(item => ({
                deptId: item.deptId,
                deptName: item.deptName,
                times: toFrontendTimes(item)
            }))
        }
    } catch {
        ElMessage.warning('加载时间要求失败，请稍后重试')
    }
}

// 将总体设置应用到所有部门
const applyGlobalToAll = () => {
    ElMessageBox.confirm('确认将当前总体时间设置应用到所有部门吗？', '确认操作', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            const body = {
                mondayTime: globalTimes.monday,
                tuesdayTime: globalTimes.tuesday,
                wednesdayTime: globalTimes.wednesday,
                thursdayTime: globalTimes.thursday,
                fridayTime: globalTimes.friday
            }
            await api.post('/time-requirements/apply-global', body)
            ElMessage.success('已应用到所有部门')
            loadData()
        } catch {
            ElMessage.error('应用失败')
        }
    }).catch(() => {})
}

// 保存单个部门时间
const saveDepartmentTime = async (row) => {
    try {
        await api.put(`/time-requirements/${row.deptId}`, toBackendBody(row))
        ElMessage.success(`「${row.deptName}」时间要求保存成功`)
    } catch {
        ElMessage.error('保存失败')
    }
}

// 保存全部
const saveAll = async () => {
    saving.value = true
    let failed = 0
    for (const dept of departmentTimeList.value) {
        try {
            await api.put(`/time-requirements/${dept.deptId}`, toBackendBody(dept))
        } catch {
            failed++
        }
    }
    saving.value = false
    if (failed === 0) {
        ElMessage.success('全部部门时间要求保存成功')
    } else {
        ElMessage.warning(`部分保存失败（${failed} 个部门）`)
    }
}

onMounted(() => {
    loadData()
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
    margin-bottom: 16px;
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
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
    margin-top: 4px;
    display: block;
}

.global-section {
    margin-bottom: 8px;
}

.global-time-row {
    display: flex;
    align-items: flex-end;
    gap: 16px;
    flex-wrap: wrap;
}

.time-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.day-label {
    font-size: 13px;
    color: #666;
    font-weight: 500;
}

.time-picker {
    width: 130px;
}

.apply-btn {
    margin-left: 8px;
    height: 32px;
}

.btn-icon {
    margin-right: 4px;
}

.department-section {
    margin-top: 8px;
}

.data-table {
    margin-top: 16px;
}

.save-all-bar {
    display: flex;
    justify-content: center;
    margin-top: 28px;
    padding-top: 20px;
    border-top: 1px solid #f1f3f6;
}
</style>
