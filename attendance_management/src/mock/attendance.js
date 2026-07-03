export const dashboardStats = [
    { label: '员工总数', value: 50, unit: '人', trend: '+4', trendLabel: '本月新增', color: '#409eff', icon: 'User' },
    { label: '今日出勤率', value: 96.8, unit: '%', trend: '+1.2%', trendLabel: '较昨日', color: '#67c23a', icon: 'CircleCheck' },
    { label: '待审批请假', value: 9, unit: '条', trend: '3条', trendLabel: '紧急单据', color: '#e6a23c', icon: 'Tickets' },
    { label: '异常预警', value: 6, unit: '条', trend: '-2', trendLabel: '较昨日减少', color: '#f56c6c', icon: 'WarningFilled' }
]

export const attendanceTrend = {
    dates: ['06-14', '06-15', '06-16', '06-17', '06-18', '06-19', '06-20'],
    rates: [94.2, 95.1, 96.5, 95.9, 97.1, 96.7, 96.8],
    lateCounts: [14, 11, 9, 10, 7, 8, 6]
}

export const attendanceOverview = [
    { label: '正常出勤', value: 48, tag: 'success' },
    { label: '迟到', value: 6, tag: 'warning' },
    { label: '请假', value: 7, tag: 'info' },
    { label: '缺卡', value: 3, tag: 'danger' }
]

export const employees = [
    { id: 1001, name: '张晓敏', department: '技术部', position: '后端开发', phone: '13800010001', status: '在职', attendanceRate: 98.2 },
    { id: 1002, name: '李志鹏', department: '技术部', position: '前端开发', phone: '13800010002', status: '在职', attendanceRate: 95.4 },
    { id: 1003, name: '陈嘉怡', department: '市场部', position: '运营专员', phone: '13800010003', status: '在职', attendanceRate: 93.6 },
    { id: 1004, name: '黄思颖', department: '人事部', position: 'HRBP', phone: '13800010004', status: '试用', attendanceRate: 97.3 },
    { id: 1005, name: '刘文涛', department: '财务部', position: '会计', phone: '13800010005', status: '在职', attendanceRate: 99.1 }
]

export const departments = [
    { id: 1, name: '技术部', manager: '赵文远', members: 8, lateRate: '2.1%', overtimeHours: 168, status: '稳定' },
    { id: 2, name: '市场部', manager: '陈思琪', members: 6, lateRate: '3.4%', overtimeHours: 96, status: '关注' },
    { id: 3, name: '人事部', manager: '林雨桐', members: 5, lateRate: '1.3%', overtimeHours: 38, status: '稳定' },
    { id: 4, name: '财务部', manager: '郑明杰', members: 6, lateRate: '1.8%', overtimeHours: 52, status: '稳定' },
    { id: 5, name: '研发部', manager: '赵文远', members: 8, lateRate: '2.5%', overtimeHours: 200, status: '稳定' },
    { id: 6, name: '产品部', manager: '李若涵', members: 5, lateRate: '2.0%', overtimeHours: 72, status: '稳定' },
    { id: 7, name: '销售部', manager: '陈思琪', members: 7, lateRate: '5.2%', overtimeHours: 120, status: '关注' },
    { id: 8, name: '行政部', manager: '林雨桐', members: 5, lateRate: '1.5%', overtimeHours: 24, status: '稳定' }
]

export const attendanceRecords = [
    { id: 1, date: '2026-06-20', name: '张晓敏', department: '技术部', checkIn: '08:52', checkOut: '18:11', workHours: 8.5, result: '正常' },
    { id: 2, date: '2026-06-20', name: '李志鹏', department: '技术部', checkIn: '09:13', checkOut: '18:24', workHours: 8.1, result: '迟到' },
    { id: 3, date: '2026-06-20', name: '陈嘉怡', department: '市场部', checkIn: '08:47', checkOut: '17:56', workHours: 8.0, result: '正常' },
    { id: 4, date: '2026-06-20', name: '黄思颖', department: '人事部', checkIn: '--', checkOut: '--', workHours: 0, result: '请假' },
    { id: 5, date: '2026-06-20', name: '刘文涛', department: '财务部', checkIn: '08:58', checkOut: '--', workHours: 4.2, result: '缺卡' }
]

export const leaveRequests = [
    { id: 'LV2026062001', name: '黄思颖', type: '病假', days: 1, period: '2026-06-20 至 2026-06-20', reason: '发烧就医', approver: '林雨桐', status: '待审批' },
    { id: 'LV2026061902', name: '陈嘉怡', type: '事假', days: 2, period: '2026-06-24 至 2026-06-25', reason: '家庭事务', approver: '陈思琪', status: '待审批' },
    { id: 'LV2026061801', name: '李志鹏', type: '调休', days: 0.5, period: '2026-06-22 上午', reason: '加班调休', approver: '赵文远', status: '已通过' },
    { id: 'LV2026061701', name: '张晓敏', type: '年假', days: 3, period: '2026-06-27 至 2026-06-29', reason: '回老家探亲', approver: '赵文远', status: '已通过' },
    { id: 'LV2026061601', name: '赵六', type: '事假', days: 1, period: '2026-06-23 至 2026-06-23', reason: '办理证件', approver: '陈思琪', status: '待审批' },
    { id: 'LV2026061501', name: '周八', type: '病假', days: 2, period: '2026-06-26 至 2026-06-27', reason: '急性肠胃炎', approver: '赵文远', status: '待审批' },
    { id: 'LV2026061401', name: '李四', type: '婚假', days: 5, period: '2026-07-01 至 2026-07-05', reason: '结婚', approver: '李若涵', status: '已通过' },
    { id: 'LV2026061301', name: '王五', type: '调休', days: 1, period: '2026-06-28 至 2026-06-28', reason: '周末加班调休', approver: '陈思琪', status: '已通过' },
    { id: 'LV2026061201', name: '张三', type: '事假', days: 0.5, period: '2026-06-25 上午', reason: '孩子家长会', approver: '赵文远', status: '待审批' },
    { id: 'LV2026061101', name: '孙七', type: '年假', days: 2, period: '2026-06-30 至 2026-07-01', reason: '短途旅行', approver: '林雨桐', status: '已通过' }
]

export const alertRecords = [
    {
        id: 'ALT-001',
        level: '高',
        employee: '李志鹏',
        summary: '',
        aiSuggestion: '',
        status: '待处理'
    },
    {
        id: 'ALT-002',
        level: '中',
        employee: '陈嘉怡',
        summary: '',
        aiSuggestion: '',
        status: '待处理'
    },
    {
        id: 'ALT-003',
        level: '低',
        employee: '刘文涛',
        summary: '',
        aiSuggestion: '',
        status: '已关闭'
    },
    {
        id: 'ALT-004',
        level: '高',
        employee: '周八',
        summary: '',
        aiSuggestion: '',
        status: '待处理'
    },
    {
        id: 'ALT-005',
        level: '中',
        employee: '李四',
        summary: '',
        aiSuggestion: '',
        status: '待处理'
    },
    {
        id: 'ALT-006',
        level: '低',
        employee: '王五',
        summary: '',
        aiSuggestion: '',
        status: '待处理'
    },
    {
        id: 'ALT-007',
        level: '高',
        employee: '赵六',
        summary: '',
        aiSuggestion: '',
        status: '待处理'
    }
]

export const performanceRecords = []

export const systemUsers = [
    { id: 1, employeeId: 'EMP001', username: 'admin', role: '系统管理员', phone: '13800000001', status: '正常', lastLogin: '2026-06-20 08:30' },
    { id: 2, employeeId: 'EMP002', username: 'hr_manager', role: '人事主管', phone: '13800000002', status: '正常', lastLogin: '2026-06-20 09:02' },
    { id: 3, employeeId: 'EMP003', username: 'dept_leader', role: '部门主管', phone: '13800000003', status: '禁用', lastLogin: '2026-06-18 18:15' }
]
