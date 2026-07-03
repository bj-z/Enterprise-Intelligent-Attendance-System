<template>
    <div class="login-page">
        <div class="login-card">
            <div class="login-header">
                <div class="login-logo">考</div>
                <h3 class="login-title">考勤管理系统</h3>
                <p class="login-sub">企业员工考勤管理平台</p>
            </div>
            <el-form v-model="loginForm" class="login-form">
                <el-form-item>
                    <el-input type="input" placeholder="请输入账号" v-model="loginForm.username" prefix-icon="User" size="large"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-input type="password" placeholder="请输入密码" v-model="loginForm.password" prefix-icon="Lock" size="large" @keyup.enter="login"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" class="login-btn" @click="login">登录</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api/index.js';

const router = useRouter()
const loginForm = reactive({username:'',password:''})

const login = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  try {
    const res = await api.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    if (res.code === 200 && res.data.token) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('username', res.data.username)
      localStorage.setItem('role', res.data.role)
      ElMessage.success('登录成功')
      router.push({name:'dashboard'})
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch {
    ElMessage.error('登录失败，请检查网络连接')
  }
}
</script>

<style scoped>
.login-page {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    background-color: #f8fafc;
}

.login-card {
    width: 380px;
    padding: 40px 36px;
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.login-header {
    text-align: center;
    margin-bottom: 32px;
}

.login-logo {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    background-color: #3b7dd8;
    color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: 700;
    margin: 0 auto 16px;
}

.login-title {
    font-size: 22px;
    font-weight: 700;
    color: #333333;
    margin: 0 0 6px;
}

.login-sub {
    font-size: 13px;
    color: #999999;
    margin: 0;
}

.login-form {
    margin-top: 8px;
}

.login-btn {
    width: 100%;
    height: 44px;
    border-radius: 12px;
    font-size: 16px;
    margin-top: 8px;
}
</style>
