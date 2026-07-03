export const menuItems = [
    {
        path: '/dashboard',
        name: 'dashboard',
        label: '首页概览',
        icon: 'DataBoard',
        title: '首页概览',
        desc: '企业员工考勤管理系统'
    },
    {
        path: '/employee',
        name: 'employee',
        label: '员工查询',
        icon: 'User',
        title: '员工查询',
        desc: '企业员工考勤管理系统'
    },
    {
        path: '/department',
        name: 'department',
        label: '部门查询',
        icon: 'OfficeBuilding',
        title: '部门查询',
        desc: '企业员工考勤管理系统'
    },
    {
        path: '/attendance',
        name: 'attendance',
        label: '考勤记录',
        icon: 'Calendar',
        title: '考勤记录',
        desc: '企业员工考勤管理系统'
    },
    {
        path: '/leave',
        name: 'leave',
        label: '请假审批',
        icon: 'Tickets',
        title: '请假审批',
        desc: '企业员工考勤管理系统'
    },
    {
        path: '/alerts',
        name: 'alerts',
        label: '异常预警',
        icon: 'WarningFilled',
        title: '异常预警',
        desc: 'AI智能识别考勤异常，辅助管理员快速跟进'
    },
    {
        path: '/performance',
        name: 'performance',
        label: '绩效分析',
        icon: 'TrendCharts',
        title: '绩效分析',
        desc: '基于考勤数据智能分析员工绩效表现'
    },
    {
        path: '/time-requirements',
        name: 'time-requirements',
        label: '时间要求',
        icon: 'Clock',
        title: '时间要求',
        desc: '设置各部门周一至周五的上班打卡时间要求',
        requireAdmin: true
    },
    {
        path: '/system-users',
        name: 'system-users',
        label: '系统用户',
        icon: 'Setting',
        title: '系统用户',
        desc: '维护后台登录账户、角色与使用状态',
        requireAdmin: true
    },
    {
        path: '/ai-management',
        name: 'ai-management',
        label: 'AI 管理',
        icon: 'Cpu',
        title: 'AI 管理',
        desc: '配置 DeepSeek API Key，管理 AI 智能分析功能',
        requireAdmin: true
    }
]
