<template>
    <div class="department-management">
        
        <!-- 卡片网格布局 -->
        <div class="department-grid">
            <div v-for="dept in departmentList" :key="dept.id" class="department-card">
                <div class="card-header">
                    <h3 class="dept-name">{{ dept.name }}</h3>
                    <el-tag class="member-count-tag">
                        <el-icon><User /></el-icon>
                        {{ dept.memberCount }}
                    </el-tag>
                </div>
                
                <p class="dept-desc">{{ dept.description || '暂无部门描述' }}</p>
                
                <div class="card-divider"></div>
                
                <div class="card-footer">
                    <span class="manager-info">负责人: {{ dept.manager }}</span>
                    <el-button type="primary" link @click="showManagerDetail(dept)">
                        查看详情
                    </el-button>
                </div>
            </div>
        </div>
        
        <!-- 负责人详情对话框 -->
        <el-dialog v-model="detailDialogVisible" title="负责人详情" width="400px">
            <div class="manager-detail">
                <div class="detail-item">
                    <span class="detail-label">部门名称:</span>
                    <span class="detail-value">{{ currentDepartment.name }}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">负责人:</span>
                    <span class="detail-value">{{ currentDepartment.manager }}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">联系电话:</span>
                    <span class="detail-value">{{ currentDepartment.managerPhone || '暂无' }}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">部门人数:</span>
                    <span class="detail-value">{{ currentDepartment.memberCount }} 人</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">部门职责:</span>
                    <span class="detail-value">{{ currentDepartment.description || '暂无描述' }}</span>
                </div>
            </div>
            <template #footer>
                <el-button type="primary" @click="detailDialogVisible=false">关闭</el-button>
            </template>
        </el-dialog>

    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { User } from '@element-plus/icons-vue'
import api from '../api/index.js'

const departmentList = ref([])

const fetchDepartments = async () => {
  try {
    const res = await api.get('/departments')
    if (res.code === 200) departmentList.value = res.data || []
  } catch {}
}

const detailDialogVisible = ref(false)
const currentDepartment = ref({})

const showManagerDetail = (dept) => { currentDepartment.value = dept; detailDialogVisible.value = true }

onMounted(() => { fetchDepartments() })
</script>

<style scoped>
.department-management {
    background-color: #f8fafc;
    min-height: 100vh;
}

.page-toolbar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-bottom: 24px;
}

.toolbar-right {
    display: flex;
    align-items: center;
}

.btn-icon {
    margin-right: 4px;
}

/* 卡片网格布局 */
.department-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
}

.department-card {
    background: #ffffff;
    border-radius: 16px;
    padding: 32px 28px;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
    border: 1px solid #f1f3f6;
    position: relative;
    transition: all 0.3s ease;
    min-height: 240px;
    display: flex;
    flex-direction: column;
}

.department-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
}

.dept-name {
    font-size: 22px;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
}

.member-count-tag {
    background-color: #eff6ff;
    color: #3b82f6;
    border: none;
    font-size: 14px;
    padding: 4px 12px;
    border-radius: 20px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.dept-desc {
    color: #64748b;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 16px;
    flex: 1;
}

.card-divider {
    height: 1px;
    background-color: #f1f5f9;
    margin-bottom: 16px;
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.manager-info {
    color: #334155;
    font-size: 14px;
    font-weight: 500;
}

/* 负责人详情样式 */
.manager-detail {
    padding: 16px 0;
}

.detail-item {
    display: flex;
    margin-bottom: 16px;
    font-size: 14px;
}

.detail-label {
    width: 80px;
    color: #64748b;
    font-weight: 500;
}

.detail-value {
    flex: 1;
    color: #1e293b;
}
</style>