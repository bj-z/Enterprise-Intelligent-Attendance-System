<template>
  <div class="page-container">
    <!-- 主内容卡片 -->
    <div class="main-card">
      <!-- 顶部标签页 -->
      <el-tabs v-model="activeTab" class="approval-tabs" @tab-change="fetchLeaveRequests">
        <el-tab-pane :label="'待审批 (' + pendingCount + ')'" name="pending" />
        <el-tab-pane label="已处理" name="processed" />
      </el-tabs>
      
      <!-- 审批列表 -->
      <div class="approval-list">
        <div 
          v-for="item in filteredRequests" 
          :key="item.id" 
          class="approval-item"
        >
          <!-- 左侧申请信息 -->
          <div class="approval-info">
            <div class="info-header">
              <span class="applicant-name">{{ item.employeeName }}</span>
              <el-tag size="small" class="leave-type-tag">{{ item.leaveType }}</el-tag>
              <span class="leave-days">{{ item.days }} 天</span>
            </div>
            <div class="info-detail">
              <span class="time-range">时间：{{ item.startDate }} 至 {{ item.endDate }}</span>
              <span class="leave-reason">原因：{{ item.reason }}</span>
            </div>
          </div>
          
          <!-- 右侧操作/状态 -->
          <div class="approval-action">
            <!-- 待审批状态显示操作按钮 -->
            <template v-if="item.status === 'pending'">
              <el-button 
                type="success" 
                icon="Check" 
                @click="approveRequest(item.id)"
              >
                同意
              </el-button>
              <el-button 
                type="danger" 
                icon="Close" 
                @click="rejectRequest(item.id)"
              >
                拒绝
              </el-button>
            </template>
            
            <!-- 已处理状态显示纯文本状态 -->
            <template v-else>
              <span 
                class="status-text"
                :class="item.status === 'approved' ? 'status-approved' : 'status-rejected'"
              >
                <el-icon :size="16" style="margin-right: 4px">
                  <Check v-if="item.status === 'approved'" />
                  <Close v-else />
                </el-icon>
                {{ item.status === 'approved' ? '已通过' : '已拒绝' }}
              </span>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Check, Close } from '@element-plus/icons-vue'
import api from '../api/index.js'

const activeTab = ref('pending')
const leaveRequests = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const fetchLeaveRequests = async () => {
  try {
    const tab = activeTab.value === 'pending' ? 'pending' : 'processed'
    const res = await api.get(`/leave/list`, { params: { tab, page: currentPage.value, pageSize: pageSize.value } })
    if (res.code === 200) {
      leaveRequests.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {}
}

const pendingCount = computed(() => leaveRequests.value.filter(item => item.status === 'pending').length)
const filteredRequests = computed(() => leaveRequests.value)

const approveRequest = async (id) => {
  try { const res = await api.put(`/leave/${id}/approve`, null, { params: { approver: '系统管理员' } }); if (res.code === 200) fetchLeaveRequests() } catch {}
}
const rejectRequest = async (id) => {
  try { const res = await api.put(`/leave/${id}/reject`, null, { params: { approver: '系统管理员', comment: '审核不通过' } }); if (res.code === 200) fetchLeaveRequests() } catch {}
}

onMounted(() => { fetchLeaveRequests() })
</script>

<style scoped>


.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24px;
}

.main-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f3f6;
}

.approval-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.approval-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #e5e7eb;
  bottom: -4px;
}

.approval-tabs :deep(.el-tabs__active-bar) {
  height: 2px;
}

.approval-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  padding: 0 20px;
}

.approval-list {
  display: flex;
  flex-direction: column;
}

.approval-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f3f4f6;
}

/* 最后一项去掉下边框 */
.approval-item:last-child {
  border-bottom: none;
}

.approval-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.applicant-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.leave-type-tag {
  background-color: #f3f4f6;
  color: #4b5563;
  border: none;
  font-size: 14px;
  padding: 2px 8px;
}

.leave-days {
  font-size: 16px;
  color: #6b7280;
}

.info-detail {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #6b7280;
}

.approval-action {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 操作按钮样式 */
.approval-action :deep(.el-button) {
  padding: 6px 16px;
  border-radius: 8px;
  font-size: 14px;
  border: none;
}

.approval-action :deep(.el-button--success) {
  background-color: #f0fdf4;
  color: #16a34a;
}

.approval-action :deep(.el-button--success:hover) {
  background-color: #dcfce7;
  color: #15803d;
}

.approval-action :deep(.el-button--danger) {
  background-color: #fef2f2;
  color: #dc2626;
}

.approval-action :deep(.el-button--danger:hover) {
  background-color: #fee2e2;
  color: #b91c1c;
}

/* 状态文本样式 */
.status-text {
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
}

.status-approved {
  color: #16a34a;
}

.status-rejected {
  color: #dc2626;
}
</style>