<template>
    <aside class="sidebar">
        <div class="sidebar-logo">
            <div class="logo-icon">考</div>
            <div class="logo-text">
                <span class="logo-title">考勤管理系统</span>
                <span class="logo-sub">Attendance System</span>
            </div>
        </div>
        <nav class="sidebar-nav">
            <router-link
                v-for="item in filteredMenuItems"
                :key="item.path"
                :to="item.path"
                class="nav-item"
                :class="{ active: currentPath === item.path }"
            >
                <component :is="item.icon" class="nav-icon" />
                <span class="nav-label">{{ item.label }}</span>
            </router-link>
        </nav>
        <!-- 修改：使用Element Plus下拉菜单组件实现用户菜单 -->
        <el-dropdown 
            trigger="click" 
            placement="top-start"
            :hide-on-click="true"
            class="user-dropdown"
        >
            <!-- 原用户区域作为下拉触发项 -->
            <div class="sidebar-user">
                <div class="user-avatar">{{ userRole.charAt(0) }}</div>
                <div class="user-info">
                    <span class="user-name">{{ userRole }}</span>
                    <span class="user-role">{{ username }}</span>
                </div>
            </div>
            <!-- 下拉菜单内容 -->
            <template #dropdown>
                <el-dropdown-menu class="user-dropdown-menu">
                    <el-dropdown-item divided class="dropdown-item danger-item" @click="handleLogout">
                        <el-icon class="item-icon"><SwitchButton /></el-icon>
                        <span>退出登录</span>
                    </el-dropdown-item>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </aside>
</template>
<script setup>
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { menuItems } from '../config/menu';
// 导入需要的Element Plus图标
import { User, Switch, SwitchButton } from '@element-plus/icons-vue';

const username = ref(localStorage.getItem('username') || '')
const userRole = ref(localStorage.getItem('role') || '系统管理员')

const filteredMenuItems = computed(() => {
  if (username.value === 'admin') return menuItems
  return menuItems.filter(item => !item.requireAdmin)
})
const route = useRoute();
const router = useRouter();
const currentPath = computed(() => route.path);
// 菜单点击事件处理
const handleProfile = () => {
    // 可在此添加个人信息页面跳转逻辑
    console.log('跳转到个人信息页面');
};
const handleSwitchAccount = () => {
    // 可在此添加切换账号逻辑
    console.log('切换账号');
};
const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    router.push('/login');
};
</script>
<style scoped>
.sidebar {
    width: 220px;
    min-width: 220px;
    height: 100%;
    background-color: #ffffff;
    display: flex;
    flex-direction: column;
    border-right: 1px solid #eef1f5;
    position: relative; /* 为下拉菜单定位提供上下文 */
}
/* ---- Logo 区域 ---- */
.sidebar-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 22px 20px;
}
.logo-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background-color: #3b7dd8;
    color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 700;
    flex-shrink: 0;
}
.logo-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
}
.logo-title {
    font-size: 15px;
    font-weight: 600;
    color: #333333;
    line-height: 1.3;
}
.logo-sub {
    font-size: 11px;
    color: #999999;
    line-height: 1.2;
}
/* ---- 导航菜单 ---- */
.sidebar-nav {
    flex: 1;
    padding: 8px 12px;
    display: flex;
    flex-direction: column;
    gap: 2px;
    overflow-y: auto;
}
.nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 11px 16px;
    border-radius: 10px;
    color: #666666;
    font-size: 14px;
    transition: all 0.15s ease;
    cursor: pointer;
    text-decoration: none;
}
.nav-item:hover {
    background-color: #f0f4ff;
    color: #3b7dd8;
}
.nav-item.active {
    background-color: #e8f0fe;
    color: #3b7dd8;
    font-weight: 500;
}
.nav-icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
}
.nav-label {
    white-space: nowrap;
}
/* ---- 底部用户区域 ---- */
.sidebar-user {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    border-top: 1px solid #eef1f5;
    cursor: pointer;
    transition: background-color 0.15s;
    width:100%
}
.sidebar-user:hover {
    background-color: #f5f7fa;
}
.user-avatar {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    background-color: #3b7dd8;
    color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 600;
    flex-shrink: 0;
}
.user-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
}
.user-name {
    font-size: 13px;
    color: #333333;
    font-weight: 500;
}
.user-role {
    font-size: 11px;
    color: #999999;
}
/* ✅ 修改2：让el-dropdown占满整行宽度，解决右边空位问题 */
.user-dropdown {
    width: 100%;
}
/* ---- 下拉菜单样式（深度选择器修改Element Plus内部样式） ---- */
:deep(.user-dropdown-menu) {
    width: 180px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: none;
    padding: 8px 0;
    margin-bottom: 8px;
}
:deep(.dropdown-item) {
    height: 44px;
    line-height: 44px;
    padding: 0 16px;
    font-size: 14px;
    color: #333333;
    display: flex;
    align-items: center;
    gap: 12px;
}
:deep(.dropdown-item:hover) {
    background-color: #f0f4ff;
    color: #3b7dd8;
}
:deep(.danger-item) {
    color: #f56c6c;
}
:deep(.danger-item:hover) {
    background-color: #fef0f0;
    color: #f56c6c;
}
.item-icon {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
}
</style>