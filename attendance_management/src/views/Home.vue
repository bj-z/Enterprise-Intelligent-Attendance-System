<template>
  <div class="dashboard">
    <!-- 第一行：4 个统计卡片 -->
    <div class="stat-grid">
      <div v-for="item in dashboardStats" :key="item.label" class="stat-card" :class="{ 'stat-clickable': item.label === '待审批请假' || item.label === '异常预警' }" @click="handleStatClick(item)">
        <div class="stat-icon-box" :style="{ backgroundColor: item.color }">
          <component :is="item.icon" class="stat-icon" />
        </div>
        <div class="stat-body">
          <span class="stat-label">{{ item.label }}</span>
          <span class="stat-value">
            {{ item.value }}
            <small class="stat-unit">{{ item.unit }}</small>
          </span>
          <span class="stat-trend">{{ item.trend }} {{ item.trendLabel }}</span>
        </div>
      </div>
    </div>

    <!-- 第二行：左窄右宽固定两栏 -->
    <div class="content-grid">
      <!-- 左侧列：考勤概览 + 待办 -->
      <div class="side-col">
        <div class="panel-card">
          <div class="panel-header">
            <div class="panel-title-group">
              <div class="panel-title">今日考勤概览</div>
              <div class="panel-line"></div>
            </div>
          </div>
          
          <!-- 新增：顶部汇总进度条 -->
          <div class="overview-summary">
            <div class="summary-total">
              <span>应到：{{ attendanceTotal.total }}人</span>
              <span>实到：{{ attendanceTotal.actual }}人</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: attendanceTotal.rate + '%' }"></div>
            </div>
            <div class="progress-text">今日出勤率 {{ attendanceTotal.rate }}%</div>
          </div>

          <!-- 明细列表 -->
          <div class="overview-list">
            <div class="overview-row" v-for="item in attendanceOverview" :key="item.label">
              <span class="overview-text">{{ item.label }}</span>
              <el-tag :type="item.tag" size="small">{{ item.value }}</el-tag>
            </div>
          </div>
        </div>

        <div class="panel-card">
          <div class="panel-header">
            <div class="panel-title-group">
              <div class="panel-title">待办事项</div>
              <div class="panel-line"></div>
            </div>
          </div>
          <div class="todo-list">
            <div class="todo-item" v-for="(todo, idx) in todos" :key="idx">
              <span class="todo-dot" :class="'dot-' + todo.color"></span>
              <div class="todo-body">
                <span class="todo-time">{{ todo.time }}</span>
                <span class="todo-text">{{ todo.text }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧列：趋势图表 + 异常预警 -->
      <div class="main-col">
        <div class="panel-card">
          <div class="panel-header">
            <div class="panel-title-group">
              <div class="panel-title">近 7 日出勤趋势</div>
              <div class="panel-sub">展示整体出勤率与迟到人数变化</div>
            </div>
          </div>
          <div ref="chartRef" class="chart-wrap"></div>
        </div>

        <div class="panel-card">
          <div class="panel-header">
            <div class="panel-title-group">
              <div class="panel-title">异常预警清单</div>
            </div>
          </div>
          <div class="alert-list" v-if="alertRecords.length > 0">
            <div v-for="item in alertRecords" :key="item.alertNo" class="alert-row">
              <div class="alert-head">
                <span class="alert-id">{{ item.alertNo }}</span>
                <el-tag :type="alertTagType[item.level]" size="small">{{ item.level }}风险</el-tag>
              </div>
              <div class="alert-desc">{{ item.employeeName }}：{{ item.summary }}</div>
              <div class="alert-ai">AI建议：{{ item.aiSuggestion }}</div>
            </div>
          </div>
          <div class="empty-state" v-else>
            <el-empty description="暂无异常预警数据" :image-size="60" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import api from '../api/index.js'
import { dashboardStats as mockStats, attendanceTrend as mockTrend, attendanceOverview as mockOverview, alertRecords as mockAlerts } from '../mock/attendance.js'

const chartRef = ref()
let chartInstance
const router = useRouter()
const alertTagType = { 高: 'danger', 中: 'warning', 低: 'info' }

const handleStatClick = (item) => {
  if (item.label === '待审批请假') router.push('/leave')
  if (item.label === '异常预警') router.push('/alerts')
}

const dashboardStats = ref([])
const attendanceTotal = ref({ total: 0, actual: 0, rate: 0 })
const attendanceOverview = ref([])
const attendanceTrend = ref({ dates: [], rates: [], lateCounts: [] })
const alertRecords = ref([])

const todos = [
  { time: '09:00', text: '确认今日各部门人员到岗情况', color: 'blue' },
  { time: '09:30', text: '审批 2 条紧急病假申请', color: 'warning' },
  { time: '11:00', text: '导出本周考勤日报并同步主管', color: 'blue' },
  { time: '15:00', text: '跟进连续迟到员工约谈记录', color: 'danger' },
  { time: '17:30', text: '生成日终异常预警分析报告', color: 'green' },
]

const useMockFallback = () => {
  dashboardStats.value = mockStats
  attendanceTrend.value = mockTrend
  // 从 mock 数据计算考勤概览
  const total = 128
  const absent = mockOverview.find(i => i.label === '缺卡')?.value || 0
  attendanceTotal.value = { total, actual: total - absent, rate: Math.round((total - absent) / total * 1000) / 10 }
  attendanceOverview.value = mockOverview
  alertRecords.value = mockAlerts.slice(0, 3).map(a => ({
    alertNo: a.id,
    level: a.level,
    employeeName: a.employee,
    summary: a.summary,
    aiSuggestion: a.aiSuggestion,
  }))
  renderChart()
}

const fetchDashboardData = async () => {
  try {
    const [statsRes, overviewRes, trendRes, alertsRes] = await Promise.all([
      api.get('/dashboard/stats'),
      api.get('/dashboard/attendance-overview'),
      api.get('/dashboard/attendance-trend'),
      api.get('/dashboard/alerts'),
    ])
    if (statsRes.code === 200) {
      const s = statsRes.data
      dashboardStats.value = [s.totalEmployees, s.attendanceRate, s.pendingLeave, s.alerts].filter(Boolean)
    }
    if (overviewRes.code === 200) {
      const o = overviewRes.data
      attendanceTotal.value = { total: o.total, actual: o.actual, rate: o.rate }
      attendanceOverview.value = o.details || []
    }
    if (trendRes.code === 200) {
      attendanceTrend.value = trendRes.data
      renderChart()
    }
    if (alertsRes.code === 200) {
      alertRecords.value = (alertsRes.data.alerts || []).slice(0, 3)
    }
  } catch {
    useMockFallback()
  }
}

const renderChart = () => {
  if (!chartInstance) return
  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e5e7eb',
      padding: 12,
      textStyle: { fontSize: 12, color: '#333' }
    },
    legend: {
      data: ['出勤率', '迟到人数'],
      textStyle: { color: '#666', fontSize: 12 },
      top: 0, // 图例置顶
      right: 0 // 图例靠右，避免和标题重叠
    },
    grid: {
      left: 36,
      right: 20,
      top: 48, // 增大顶部间距，彻底解决图例和图表重叠
      bottom: 30
    },
    xAxis: {
      type: 'category',
      data: attendanceTrend.value.dates,
      axisLine: { lineStyle: { color: '#e8eaed' } },
      axisTick: { show: false },
      axisLabel: { color: '#888', fontSize: 12 }
    },
    yAxis: [
      {
        type: 'value',
        min: 90,
        max: 100,
        axisLabel: { formatter: '{value}%', color: '#888', fontSize: 12 },
        splitLine: { lineStyle: { color: '#f2f3f5' } },
        axisLine: { show: false },
        axisTick: { show: false }
      },
      {
        type: 'value',
        axisLabel: { color: '#888', fontSize: 12 },
        splitLine: { show: false },
        axisLine: { show: false },
        axisTick: { show: false }
      }
    ],
    series: [
      {
        name: '出勤率',
        type: 'line',
        smooth: true,
        data: attendanceTrend.value.rates,
        itemStyle: { color: '#3b7dd8' },
        lineStyle: { width: 2 },
        symbol: 'circle',
        symbolSize: 6
      },
      {
        name: '迟到人数',
        type: 'bar',
        yAxisIndex: 1,
        data: attendanceTrend.value.lateCounts,
        itemStyle: { color: '#d1d5db', borderRadius: [6, 6, 0, 0] },
        barWidth: 24
      }
    ]
  })
}

const resizeChart = () => { if (chartInstance) chartInstance.resize() }

onMounted(() => {
  chartInstance = echarts.init(chartRef.value)
  fetchDashboardData()
  window.addEventListener('resize', resizeChart)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  if (chartInstance) chartInstance.dispose()
})
</script>

<style scoped>
/* 页面根容器 */
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 22px;
  background-color: #f8fafc;
  min-height: 100vh;
}

/* 顶部统计卡片：固定4列均分 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 18px;
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f3f6;
  transition: 0.2s ease;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
}
.stat-clickable {
  cursor: pointer;
}
.stat-icon-box {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon {
  width: 26px;
  height: 26px;
  color: #fff;
}
.stat-body {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.stat-label {
  font-size: 14px;
  color: #6b7280;
}
.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: #1f2937;
}
.stat-unit {
  font-size: 14px;
  font-weight: 400;
  color: #999;
  margin-left: 3px;
}
.stat-trend {
  font-size: 12px;
  color: #22c55e;
}

/* 内容区域：固定左1右2比例 */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 18px;
  align-items: start;
}
.side-col {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.main-col {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* 通用面板卡片 */
.panel-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f3f6;
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.panel-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
.panel-line {
  width: 36px;
  height: 2px;
  background: #3b7dd8;
  border-radius: 1px;
}
.panel-sub {
  font-size: 12px;
  color: #888;
}

/* 图表容器 */
.chart-wrap {
  width: 100%;
  min-height: 300px;
}

/* 考勤概览 - 新增汇总区域 */
.overview-summary {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}
.summary-total {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #444;
  font-weight: 500;
  margin-bottom: 12px;
}
.progress-bar {
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b7dd8, #60a5fa);
  border-radius: 4px;
  transition: width 0.5s ease;
}
.progress-text {
  text-align: right;
  font-size: 12px;
  color: #3b7dd8;
  font-weight: 500;
}

/* 考勤概览明细 */
.overview-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
}
.overview-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 14px;
  color: #444;
}
.overview-row:not(:last-child) {
  border-bottom: 1px solid #f3f4f6;
}

/* 待办事项列表 */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 10px;
  transition: 0.2s ease;
}
.todo-item:hover {
  transform: translateX(4px);
  background: #f1f5f9;
}
.todo-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}
.dot-warning { background-color: #f59e0b; }
.dot-blue { background-color: #3b7dd8; }
.dot-danger { background-color: #ef4444; }
.dot-green { background-color: #22c55e; }
.todo-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.todo-time {
  font-size: 12px;
  color: #888;
}
.todo-text {
  font-size: 14px;
  color: #222;
}

/* 异常预警列表 */
.alert-list {
  flex: 1;
}
.alert-row {
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
  transition: 0.2s;
}
.alert-row:hover {
  background-color: #f8fafc;
  padding-left: 8px;
  border-left: 2px solid #3b7dd8;
  margin-left: -8px;
}
.alert-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.alert-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.alert-id {
  font-weight: 600;
  font-size: 13px;
  color: #222;
}
.alert-desc {
  font-size: 14px;
  color: #222;
  margin-bottom: 6px;
}
.alert-ai {
  font-size: 12px;
  color: #666;
  line-height: 1.6;
  background: #f8fafc;
  padding: 6px 10px;
  border-radius: 6px;
}

/* 美化滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: #f1f3f5;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>