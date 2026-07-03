<template>
    <header class="topbar">
        <div class="topbar-left">
            <h2 class="page-title">{{ pageTitle }}</h2>
            <span class="page-desc">{{ pageDesc }}</span>
        </div>
    </header>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const searchText = ref('');

const pageTitle = computed(() => {
    return route.meta.title || '概览仪表盘';
});

const pageDesc = computed(() => {
    return route.meta.desc || '企业员工考勤管理系统';
});

const handleSearch = () => {
    // 后续接入全局搜索
};
</script>

<style scoped>
.topbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 64px;
    padding: 0 28px;
    background-color: #ffffff;
    border-bottom: 1px solid #eef1f5;
    flex-shrink: 0;
}

.topbar-left {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.page-title {
    font-size: 20px;
    font-weight: 700;
    color: #333333;
    margin: 0;
}

.page-desc {
    font-size: 12px;
    color: #999999;
}

/* ---- 搜索框 ---- */
.topbar-right {
    display: flex;
    align-items: center;
    gap: 16px;
}

.search-box {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background-color: #f4f6f9;
    border-radius: 10px;
    width: 260px;
    transition: background-color 0.15s;
}

.search-box:focus-within {
    background-color: #eef0f5;
}

.search-icon {
    color: #999999;
    font-size: 18px;
    flex-shrink: 0;
}

.search-input {
    border: none;
    outline: none;
    background: transparent;
    font-size: 13px;
    color: #333333;
    flex: 1;
    font-family: inherit;
}

.search-input::placeholder {
    color: #bbbbbb;
}

/* ---- 通知按钮 ---- */
.notify-btn {
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 10px;
    transition: background-color 0.15s;
}

.notify-btn:hover {
    background-color: #f4f6f9;
}

.notify-icon {
    font-size: 20px;
    color: #666666;
}

.notify-badge {
    display: flex;
    align-items: center;
}
</style>
