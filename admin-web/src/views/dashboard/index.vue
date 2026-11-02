<template>
  <div class="dashboard">
    <div class="page-title-section">
      <div class="page-title">
        <h2>数据概览</h2>
        <p class="page-subtitle">欢迎回来，{{ userInfo.name }}</p>
      </div>
      <div class="date-display">
        <el-icon><el-icon-calendar /></el-icon>
        <span>{{ currentDate }}</span>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon-wrap users-icon">
          <el-icon size="28"><el-icon-user /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-number">{{ stats.users }}</p>
          <p class="stat-label">用户总数</p>
          <div class="stat-live">实时数据</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrap students-icon">
          <el-icon size="28"><el-icon-user-solid /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-number">{{ stats.students }}</p>
          <p class="stat-label">学员总数</p>
          <div class="stat-live">实时数据</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrap courses-icon">
          <el-icon size="28"><el-icon-book /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-number">{{ stats.courses }}</p>
          <p class="stat-label">课程总数</p>
          <div class="stat-live">实时数据</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon-wrap schedules-icon">
          <el-icon size="28"><el-icon-calendar /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-number">{{ stats.schedules }}</p>
          <p class="stat-label">今日课程</p>
          <div class="stat-live">今日实时数据</div>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">
            <el-icon class="title-icon"><el-icon-list /></el-icon>
            今日课程
          </h3>
          <el-button type="primary" size="small" @click="goToSchedule">查看全部</el-button>
        </div>
        <div class="course-list">
          <div v-for="lesson in todayLessons" :key="lesson.id" class="course-item">
            <div class="course-time">
              <span class="time-start">{{ lesson.startTime }}</span>
              <span class="time-separator">-</span>
              <span class="time-end">{{ lesson.endTime }}</span>
            </div>
            <div class="course-info">
              <h4 class="course-name">{{ lesson.courseName }}</h4>
              <p class="course-teacher">{{ lesson.teacherName }} · 进行中</p>
            </div>
            <el-tag :type="getStatusType(lesson.status)" class="course-status">
              {{ getStatusText(lesson.status) }}
            </el-tag>
          </div>
          <div v-if="todayLessons.length === 0" class="empty-state">
            <el-icon size="48" class="empty-icon"><el-icon-sunny /></el-icon>
            <p>今日暂无课程安排</p>
          </div>
        </div>
      </div>

      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">
            <el-icon class="title-icon"><el-icon-data-analysis /></el-icon>
            最近体测记录
          </h3>
          <el-button type="primary" size="small" @click="goToPhysical">查看全部</el-button>
        </div>
        <div class="physical-list">
          <div v-for="record in recentPhysical" :key="record.id" class="physical-item">
            <div class="physical-score-circle">
              <span class="score-value">{{ record.totalScore }}</span>
              <span class="score-unit">分</span>
            </div>
            <div class="physical-info">
              <h4 class="physical-name">{{ record.studentName }}</h4>
              <p class="physical-date">{{ record.testDate }}</p>
            </div>
            <el-icon class="arrow-icon"><el-icon-arrow-right /></el-icon>
          </div>
        </div>
      </div>

      <div class="content-card full-width">
        <div class="card-header">
          <h3 class="card-title">
            <el-icon class="title-icon"><el-icon-clock /></el-icon>
            课时消耗统计
          </h3>
        </div>
        <div class="chart-container">
          <div class="chart-bars">
            <div v-for="item in hourStats" :key="item.label" class="bar-item">
              <div class="bar-wrapper">
                <div class="bar" :style="{ height: item.percent + '%' }"></div>
              </div>
              <span class="bar-label">{{ item.label }}</span>
              <span class="bar-value">{{ item.value }}h</span>
            </div>
            <div v-if="hourStats.length === 0" class="empty-state">最近 7 天暂无课时消耗</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUsers, getStudents, getCourses, getLessons, getPhysicalTests, getHourConsumeStats, readList, readTotal } from '@/api'

const router = useRouter()

const userInfo = ref({ name: '管理员' })

const stats = reactive({
  users: 0,
  students: 0,
  courses: 0,
  schedules: 0
})

const todayLessons = ref([])
const recentPhysical = ref([])

const hourStats = ref([])

const currentDate = computed(() => {
  const date = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  return date.toLocaleDateString('zh-CN', options)
})

const getStatusType = (status) => {
  const map = {
    SCHEDULED: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    SCHEDULED: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const goToSchedule = () => {
  router.push('/schedules')
}

const goToPhysical = () => {
  router.push('/physical')
}

const fetchStats = async () => {
  const formatLocalDate = date => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const today = formatLocalDate(new Date())
  const end = new Date()
  const start = new Date(end)
  start.setDate(start.getDate() - 6)

  const results = await Promise.allSettled([
    getUsers({ pageNum: 1, pageSize: 1 }),
    getStudents({ pageNum: 1, pageSize: 1 }),
    getCourses({ pageNum: 1, pageSize: 1 }),
    getLessons({ date: today }),
    getPhysicalTests({ pageNum: 1, pageSize: 5 }),
    getHourConsumeStats({ startDate: formatLocalDate(start), endDate: today, courseType: 'ALL' })
  ])

  const [usersResult, studentsResult, coursesResult, lessonsResult, physicalResult, consumeResult] = results

  if (usersResult.status === 'fulfilled') {
    const rows = readList(usersResult.value)
    stats.users = readTotal(usersResult.value, rows.length)
  }
  if (studentsResult.status === 'fulfilled') {
    const rows = readList(studentsResult.value)
    stats.students = readTotal(studentsResult.value, rows.length)
  }
  if (coursesResult.status === 'fulfilled') {
    const rows = readList(coursesResult.value)
    stats.courses = readTotal(coursesResult.value, rows.length)
  }
  if (lessonsResult.status === 'fulfilled') {
    const rows = readList(lessonsResult.value)
    stats.schedules = readTotal(lessonsResult.value, rows.length)
    todayLessons.value = rows.slice(0, 5).map(lesson => ({
      ...lesson,
      courseName: lesson.courseName || '体育课',
      teacherName: lesson.teacherName || '--'
    }))
  }
  if (physicalResult.status === 'fulfilled') {
    recentPhysical.value = readList(physicalResult.value).slice(0, 5).map(record => ({
      ...record,
      studentName: record.studentName || '--',
      teacherName: record.teacherName || '--'
    }))
  }
  if (consumeResult.status === 'fulfilled') {
    const entries = Object.entries(consumeResult.value?.byCourseType || {})
      .map(([label, raw]) => ({ label, value: Number(raw) || 0 }))
    const max = Math.max(0, ...entries.map(item => item.value))
    hourStats.value = entries.map(item => ({
      ...item,
      percent: max > 0 ? Math.max(8, item.value / max * 100) : 0
    }))
  }

  results.forEach(result => {
    if (result.status === 'rejected') console.error('[Dashboard.fetchStats]', result.reason)
  })
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-title-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title h2 {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 6px 0 0 0;
}

.date-display {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 14px;
}

.date-display :deep(.el-icon) {
  margin-right: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04);
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.users-icon {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: #fff;
}

.students-icon {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: #fff;
}

.courses-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  color: #fff;
}

.schedules-icon {
  background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
  color: #fff;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin: 0;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin: 4px 0 8px 0;
}

.stat-live {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #64748b;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  overflow: hidden;
}

.content-card.full-width {
  grid-column: span 2;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.title-icon {
  margin-right: 8px;
  color: #2563eb;
}

.course-list {
  padding: 16px 24px;
}

.course-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.course-item:last-child {
  border-bottom: none;
}

.course-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
}

.time-start {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.time-separator {
  font-size: 12px;
  color: #d1d5db;
  margin: 4px 0;
}

.time-end {
  font-size: 14px;
  color: #6b7280;
}

.course-info {
  flex: 1;
  padding-left: 20px;
}

.course-name {
  font-size: 15px;
  font-weight: 500;
  color: #111827;
  margin: 0;
}

.course-teacher {
  font-size: 13px;
  color: #6b7280;
  margin: 4px 0 0 0;
}

.course-status {
  margin-left: auto;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #6b7280;
}

.empty-icon {
  color: #d1d5db;
  margin-bottom: 12px;
}

.physical-list {
  padding: 16px 24px;
}

.physical-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.physical-item:last-child {
  border-bottom: none;
}

.physical-score-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.score-value {
  font-size: 20px;
  font-weight: 700;
}

.score-unit {
  font-size: 10px;
}

.physical-info {
  flex: 1;
  padding-left: 20px;
}

.physical-name {
  font-size: 15px;
  font-weight: 500;
  color: #111827;
  margin: 0;
}

.physical-date {
  font-size: 13px;
  color: #6b7280;
  margin: 4px 0 0 0;
}

.arrow-icon {
  color: #9ca3af;
}

.chart-container {
  padding: 24px;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200px;
  padding-top: 20px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12%;
}

.bar-wrapper {
  width: 100%;
  height: 160px;
  background: #f9fafb;
  border-radius: 6px;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar {
  width: 100%;
  background: linear-gradient(180deg, #2563eb 0%, #3b82f6 100%);
  border-radius: 6px 6px 0 0;
  transition: height 0.5s ease;
}

.bar-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 12px;
}

.bar-value {
  font-size: 12px;
  color: #374151;
  margin-top: 4px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .content-card.full-width {
    grid-column: span 1;
  }
}
</style>
