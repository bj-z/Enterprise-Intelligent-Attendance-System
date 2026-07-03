<template>
    <div class="page-card">
            <div class="toolbar-right">
                <el-input 
                    v-model="searchKeyword" 
                    placeholder="搜索姓名或工号..." 
                    prefix-icon="Search" 
                    class="search-field" 
                    @input="searchEmployee"
                />
                <el-select v-model="selectedDepartment" placeholder="所有部门" class="filter-select" @change="searchEmployee">
                    <el-option label="所有部门" value="" />
                    <el-option label="研发部" value="研发部" />
                    <el-option label="产品部" value="产品部" />
                    <el-option label="人事部" value="人事部" />
                    <el-option label="销售部" value="销售部" />
                    <el-option label="财务部" value="财务部" />
                    <el-option label="技术部" value="技术部" />
                    <el-option label="市场部" value="市场部" />
                    <el-option label="行政部" value="行政部" />
                </el-select>
                <el-select v-model="selectedStatus" placeholder="所有状态" class="filter-select" @change="searchEmployee">
                    <el-option label="所有状态" value="" />
                    <el-option label="在职" value="在职" />
                    <el-option label="离职" value="离职" />
                </el-select>
            </div>
        <!-- 精确计算列宽总和：100+100+140+140+140+100+100+180 = 1000px，完全填满容器 -->
        <el-table 
            :data="displayList" 
            class="data-table" 
            :header-cell-style="{background:'#f8fafc',color:'#64748b',fontWeight:'500'}" 
            row-key="id"
            :cell-style="{padding:'16px 0'}"
            width="100%"
            border
            stripe
        >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="employeeId" label="工号" width="100" align="center" />
            <el-table-column prop="name" label="姓名" width="100" align="center" />
            <el-table-column prop="departmentName" label="部门" width="140" align="center" />
            <el-table-column prop="position" label="职位" width="140" align="center" />
            <el-table-column prop="phone" label="手机号" width="140" align="center" />
            <el-table-column prop="attendanceRate" label="出勤率" width="100" align="center">
                <template #default="{row}">{{ row.attendanceRate }}%</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{row}">
                    <span :class="['status-tag', row.status==='在职'?'status-active':'status-inactive']">
                        {{ row.status }}
                    </span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="220" align="center">
                <template #default="{row}">
                    <el-button type="primary" size="small" @click="showEmployeeDetail(row)">
                        <el-icon class="btn-icon"><View /></el-icon>详情
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <div class="pagination-bar">
            <el-pagination 
                v-model:current-page="currentPage" 
                v-model:page-size="pageSize" 
                :page-sizes="[5,10,20]" 
                layout="total,sizes,prev,pager,next" 
                :total="total" 
                @current-change="fetchEmployees"
                @size-change="searchEmployee"
            />
        </div>
        <el-dialog v-model="detailVisible" title="员工详情" width="480px">
            <div class="detail-wrap" v-if="detailData">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="工号" :span="2">{{ detailData.employeeId }}</el-descriptions-item>
                    <el-descriptions-item label="姓名">{{ detailData.name }}</el-descriptions-item>
                    <el-descriptions-item label="部门">{{ detailData.departmentName }}</el-descriptions-item>
                    <el-descriptions-item label="职位">{{ detailData.position }}</el-descriptions-item>
                    <el-descriptions-item label="手机号">{{ detailData.phone }}</el-descriptions-item>
                    <el-descriptions-item label="出勤率">{{ detailData.attendanceRate }}%</el-descriptions-item>
                    <el-descriptions-item label="状态" :span="2">
                        <span :class="['status-tag', detailData.status==='在职'?'status-active':'status-inactive']">
                            {{ detailData.status }}
                        </span>
                    </el-descriptions-item>
                </el-descriptions>
            </div>
            <template #footer>
                <el-button @click="detailVisible=false">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api/index.js'

const searchKeyword = ref('')
const selectedDepartment = ref('')
const selectedStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const detailVisible = ref(false)
const detailData = ref(null)
const employeeList = ref([])
const total = ref(0)

const fetchEmployees = async () => {
  try {
    const res = await api.get('/employees', {
      params: { page: currentPage.value, pageSize: pageSize.value, keyword: searchKeyword.value || undefined, department: selectedDepartment.value || undefined, status: selectedStatus.value || undefined }
    })
    if (res.code === 200) {
      employeeList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {}
}

const searchEmployee = () => { currentPage.value = 1; fetchEmployees() }

const filteredList = computed(() => employeeList.value)
const displayList = computed(() => employeeList.value)

const showEmployeeDetail = (row) => {
    detailData.value = row
    detailVisible.value = true
}

onMounted(() => { fetchEmployees() })
</script>

<style scoped>
.page-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
    border: 1px solid #f1f3f6;
}

.page-toolbar {
    display: flex;
    justify-content: end;
    align-items: center;
    margin-bottom: 20px;
}



.toolbar-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
}

.toolbar-desc {
    font-size: 13px;
    color: #999;
    margin-top: 2px;
}

.toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
}

.search-field {
    width: 260px;
    --el-input-border-radius: 8px;
    --el-input-height: 40px;
}

.filter-select {
    width: 140px;
    --el-select-border-radius: 8px;
    --el-input-height: 40px;
}

.btn-icon {
    margin-right: 4px;
}

.data-table {
    margin-top: 16px;
    --el-table-border-color: #e5e7eb;
    --el-table-row-hover-bg-color: #eef2ff;
    --el-table-striped-row-bg-color: #f8fafc;
    width: 100% !important;
}

.data-table :deep(.el-table__cell) {
    padding: 16px 0;
    font-size: 14px;
    color: #334155;
}

.data-table :deep(.el-table__header-cell) {
    padding: 12px 0;
    font-size: 14px;
}

/* 强制表格宽度100%，消除右侧空白 */
.data-table :deep(.el-table__body) {
    width: 100% !important;
}

.data-table :deep(.el-table__header) {
    width: 100% !important;
}

.status-tag {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
}

.status-active {
    background-color: #dcfce7;
    color: #16a34a;
}

.status-inactive {
    background-color: #f1f5f9;
    color: #64748b;
}

.pagination-bar {
    display: flex;
    justify-content: flex-end;
    margin-top: 24px;
}
</style>