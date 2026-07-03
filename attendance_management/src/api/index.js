import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router/router'

// ============================================
// 后端API地址 —— 部署时切换下面注释即可
// ============================================
// 本地开发：前端和后端在本机，Vite dev server 会自动代理 /api 到 localhost:18080
const API_BASE = '/api'

// ECS部署：前端和后端在同一台服务器，Nginx 代理 /api 到后端容器
// const API_BASE = '/api'

// 前后端分离部署：前端在一台服务器，后端在另一台，填写完整地址
// const API_BASE = 'http://你的后端IP:18080/api'
// ============================================

const api = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
})

// 请求拦截器：自动带 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：401 跳转登录
api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 401) {
      // 如果当前已在登录页，不拦截，让 Login.vue 自己处理错误提示
      if (window.location.hash === '#/login' || window.location.hash === '#/' || !window.location.hash) {
        return data
      }
      localStorage.removeItem('token')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error('未授权'))
    }
    return data
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    ElMessage.error('网络错误，请稍后重试')
    return Promise.reject(error)
  }
)

export default api
