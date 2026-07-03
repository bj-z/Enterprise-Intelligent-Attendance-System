<template>
    <div class="app-layout">
        <CommenAside />
        <div class="app-right">
            <CommonHeader />
            <div class="app-content">
                <router-view />
            </div>
        </div>
    </div>
</template>

<script setup>
import CommonHeader from '../components/CommonHeader.vue';
import CommenAside from '../components/CommenAside.vue';
</script>

<style scoped>
.app-layout {
    display: flex;
    height: 100%;
    background-color: #f8fafc;
}

.app-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.app-content {
    flex: 1;
    overflow-y: auto;
    padding: 24px 28px;
    background-color: #f8fafc;
}
</style>
