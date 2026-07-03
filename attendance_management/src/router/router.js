import { createRouter, createWebHashHistory } from "vue-router";
import { menuItems } from "../config/menu";

const viewMap = {
    'dashboard': () => import('../views/Home.vue'),
    'employee': () => import('../views/EmployeeManagement.vue'),
    'department': () => import('../views/DepartmentManagement.vue'),
    'attendance': () => import('../views/AttendanceRecords.vue'),
    'leave': () => import('../views/LeaveApproval.vue'),
    'alerts': () => import('../views/AttendanceAlerts.vue'),
    'performance': () => import('../views/PerformanceAnalysis.vue'),
    'time-requirements': () => import('../views/TimeRequirements.vue'),
    'system-users': () => import('../views/User.vue'),
    'ai-management': () => import('../views/AiManagement.vue')
}

const routes = [
    {
        path: '/',
        component: () => import('../views/Main.vue'),
        redirect: '/dashboard',
        children: menuItems.map((item) => ({
            path: item.path,
            name: item.name,
            meta: {
                title: item.title,
                desc: item.desc
            },
            component: viewMap[item.name]
        }))
    },
    {
        path: '/login',
        name: 'login',
        meta: {
            title: '登录'
        },
        component: () => import('../views/Login.vue')
    }
]

const router = createRouter(
    {
        history: createWebHashHistory(),
        routes
    }
)

// 路由守卫：未登录时跳转到登录页
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const username = localStorage.getItem('username')
    if (to.name !== 'login' && !token) {
        next({ name: 'login' })
    } else if (to.name === 'login' && token) {
        next({ name: 'dashboard' })
    } else if ((to.name === 'system-users' || to.name === 'time-requirements' || to.name === 'ai-management') && username !== 'admin') {
        next({ name: 'dashboard' })
    } else {
        next()
    }
})

export default router
