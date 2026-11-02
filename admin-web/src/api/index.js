import axios from 'axios'
import { ElMessage } from 'element-plus'
import { apiErrorMessage } from '@/utils/apiError'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const ENABLE_MOCK_FALLBACK = import.meta.env.DEV && import.meta.env.VITE_ENABLE_MOCK === 'true'

let _authRedirectInProgress = false
const cleanseStaleMockToken = () => {
  try {
    const t = localStorage.getItem('token')
    if (!t) return
    if (t.includes('admin-token') || t.includes('teacher-token') || t.includes('test-token') || t.includes('mock')) {
      localStorage.removeItem('token'); localStorage.removeItem('userInfo')
      return
    }
    const parts = String(t).split('.')
    if (parts.length !== 3) { localStorage.removeItem('token'); localStorage.removeItem('userInfo') }
  } catch (_) { /* noop */ }
}
cleanseStaleMockToken()

const triggerAuthRedirect = (msg) => {
  if (_authRedirectInProgress) return
  _authRedirectInProgress = true
  try { localStorage.removeItem('token') } catch (_) { /* noop */ }
  try { localStorage.removeItem('userInfo') } catch (_) { /* noop */ }
  if (typeof window !== 'undefined' && window.location && window.location.pathname !== '/login') {
    ElMessage.error(msg || '登录已过期，请重新登录')
    setTimeout(() => {
      _authRedirectInProgress = false
      try { window.location.href = '/login' } catch (_) { /* noop */ }
    }, 800)
  } else {
    setTimeout(() => { _authRedirectInProgress = false }, 800)
  }
}

service.interceptors.request.use(
  config => {
    cleanseStaleMockToken()
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res =
      response && response.data != null && typeof response.data === 'object'
        ? response.data
        : { code: -1, message: '服务器返回为空', data: null }
    const code = res?.code ?? -1
    const message = apiErrorMessage(res)
    const body = res?.data ?? res
    if (code === 0 || code === 200) {
      return body
    }
    if (code === 401 || code === 20001 || code === 20002 || code === 20003) {
      triggerAuthRedirect(code === 20003 ? (res?.message || '请先登录') : '登录已过期，请重新登录')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(new Error(message))
  },
  error => {
    const status = error?.response?.status
    const data = error?.response?.data || {}
    const code = typeof data === 'object' ? data?.code : null
    const msg = apiErrorMessage(data, error?.message || '网络错误，请稍后重试')
    error.message = msg
    console.warn('[API] Request failed:', status, code, msg)
    if (status === 401 || code === 20003 || code === 20001 || code === 20002) {
      triggerAuthRedirect(code === 20003 ? (data?.message || '请先登录') : '登录已过期，请重新登录')
    } else if (typeof status === 'number' && status >= 500) {
      ElMessage.error(`后端服务异常 (HTTP ${status}): ${msg}`)
    } else if (!status) {
      ElMessage.error(`无法连接后端服务：${msg}。请确认后端 8080 端口已启动，或运行：cd /Users/bytedance/SP-Smart/backend && export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && java -jar target/training-management-1.0.0.jar`)
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

const mockLogin = (params) => {
  if (params.username === 'admin' && params.password === '123456') {
    return Promise.resolve({ token: 'admin-token', user: { id: '1', username: 'admin', role: 'ADMIN' } })
  }
  if (params.username === 'teacher' && params.password === '123456') {
    return Promise.resolve({ token: 'teacher-token', user: { id: '2', username: 'teacher', role: 'TEACHER' } })
  }
  return Promise.reject(new Error('Invalid credentials'))
}

const mockGetCurrentUser = () => {
  return Promise.resolve({
    user: { id: '1', username: 'admin', role: 'ADMIN', phone: '13800138000' },
    profile: { id: '1', userId: '1', name: '管理员', avatar: '', school: '阳光体育学校', title: '管理员' }
  })
}

const mockGetUsers = () => {
  return Promise.resolve([
    { id: '1', username: 'admin', phone: '13800138000', role: 'ADMIN', createTime: '2024-01-01 00:00:00' },
    { id: '2', username: 'teacher', phone: '13800138001', role: 'TEACHER', createTime: '2024-01-02 00:00:00' },
    { id: '3', username: 'parent', phone: '13800138002', role: 'PARENT', createTime: '2024-01-03 00:00:00' }
  ])
}

const mockGetStudents = () => {
  return Promise.resolve([
    { id: '1', name: '张三', studentNo: 'S001', age: 12, gender: 'MALE', parentName: '张爸爸', parentPhone: '13800138001', createTime: '2024-01-01' },
    { id: '2', name: '李四', studentNo: 'S002', age: 12, gender: 'FEMALE', parentName: '李妈妈', parentPhone: '13800138002', createTime: '2024-01-02' },
    { id: '3', name: '王五', studentNo: 'S003', age: 13, gender: 'MALE', parentName: '王爸爸', parentPhone: '13800138003', createTime: '2024-01-03' },
    { id: '4', name: '赵六', studentNo: 'S004', age: 12, gender: 'FEMALE', parentName: '赵妈妈', parentPhone: '13800138004', createTime: '2024-01-04' },
    { id: '5', name: '钱七', studentNo: 'S005', age: 13, gender: 'MALE', parentName: '钱爸爸', parentPhone: '13800138005', createTime: '2024-01-05' },
    { id: '6', name: '孙八', studentNo: 'S006', age: 12, gender: 'FEMALE', parentName: '孙妈妈', parentPhone: '13800138006', createTime: '2024-01-06' }
  ])
}

const mockGetCourses = () => {
  return Promise.resolve([
    { id: '1', courseName: '篮球训练课', description: '专业篮球技能训练', duration: 90, createTime: '2024-01-01' },
    { id: '2', courseName: '体能训练课', description: '综合体能提升训练', duration: 60, createTime: '2024-01-02' },
    { id: '3', courseName: '足球训练课', description: '足球技巧与战术训练', duration: 90, createTime: '2024-01-03' }
  ])
}

const mockGetSchedules = () => {
  const today = new Date()
  const formatDate = (d) => d.toISOString().split('T')[0]
  return Promise.resolve([
    { id: '1', scheduleName: '周一篮球课', courseName: '篮球训练课', teacherName: '张老师', startDate: formatDate(today), endDate: formatDate(new Date(today.getTime() + 30 * 86400000)), startTime: '09:00', endTime: '10:30', scheduleType: 'RECURRING' },
    { id: '2', scheduleName: '周三体能课', courseName: '体能训练课', teacherName: '李老师', startDate: formatDate(today), endDate: formatDate(new Date(today.getTime() + 30 * 86400000)), startTime: '14:00', endTime: '15:00', scheduleType: 'RECURRING' },
    { id: '3', scheduleName: '周六足球课', courseName: '足球训练课', teacherName: '王老师', startDate: formatDate(today), endDate: formatDate(new Date(today.getTime() + 30 * 86400000)), startTime: '10:00', endTime: '11:30', scheduleType: 'RECURRING' }
  ])
}

const mockGetLessons = () => {
  const today = new Date()
  const formatDate = (d) => d.toISOString().split('T')[0]
  return Promise.resolve([
    { id: '1', lessonNo: 'LSN001', courseName: '篮球训练课', teacherName: '张老师', lessonDate: formatDate(today), startTime: '09:00', endTime: '10:30', status: 'IN_PROGRESS' },
    { id: '2', lessonNo: 'LSN002', courseName: '篮球训练课', teacherName: '张老师', lessonDate: formatDate(new Date(today.getTime() + 86400000)), startTime: '09:00', endTime: '10:30', status: 'SCHEDULED' },
    { id: '3', lessonNo: 'LSN003', courseName: '体能训练课', teacherName: '李老师', lessonDate: formatDate(today), startTime: '14:00', endTime: '15:00', status: 'SCHEDULED' },
    { id: '4', lessonNo: 'LSN004', courseName: '篮球训练课', teacherName: '张老师', lessonDate: formatDate(new Date(today.getTime() - 86400000)), startTime: '09:00', endTime: '10:30', status: 'COMPLETED' }
  ])
}

const mockGetPhysicalTests = () => {
  const today = new Date()
  const formatDate = (d) => d.toISOString().split('T')[0]
  return Promise.resolve([
    { id: '1', studentName: '张三', parentName: '张爸爸', parentPhone: '13800138001', testDate: formatDate(today), totalScore: 85, teacherName: '张老师', createTime: formatDate(today), scores: [{ itemName: '身高', score: 165, unit: 'cm' }, { itemName: '体重', score: 50, unit: 'kg' }, { itemName: '肺活量', score: 3500, unit: 'ml' }], evaluation: '身体状况良好，继续保持锻炼！' },
    { id: '2', studentName: '李四', parentName: '李妈妈', parentPhone: '13800138002', testDate: formatDate(new Date(today.getTime() - 7 * 86400000)), totalScore: 78, teacherName: '李老师', createTime: formatDate(new Date(today.getTime() - 7 * 86400000)), scores: [{ itemName: '身高', score: 158, unit: 'cm' }, { itemName: '体重', score: 45, unit: 'kg' }, { itemName: '肺活量', score: 3200, unit: 'ml' }], evaluation: '建议增加有氧运动，提高心肺功能。' },
    { id: '3', studentName: '王五', parentName: '王爸爸', parentPhone: '13800138003', testDate: formatDate(new Date(today.getTime() - 14 * 86400000)), totalScore: 92, teacherName: '王老师', createTime: formatDate(new Date(today.getTime() - 14 * 86400000)), scores: [{ itemName: '身高', score: 170, unit: 'cm' }, { itemName: '体重', score: 55, unit: 'kg' }, { itemName: '肺活量', score: 4000, unit: 'ml' }], evaluation: '身体素质优秀，继续保持！' }
  ])
}

const mockGetHourAccounts = () => {
  return Promise.resolve([
    { id: '1', studentId: '1', studentName: '张三', parentName: '张爸爸', parentPhone: '13800138001', studentNo: 'S001', totalHours: 100, usedHours: 30, remainingHours: 70, createTime: '2024-01-01' },
    { id: '2', studentId: '2', studentName: '李四', parentName: '李妈妈', parentPhone: '13800138002', studentNo: 'S002', totalHours: 80, usedHours: 25, remainingHours: 55, createTime: '2024-01-02' },
    { id: '3', studentId: '3', studentName: '王五', parentName: '王爸爸', parentPhone: '13800138003', studentNo: 'S003', totalHours: 120, usedHours: 10, remainingHours: 110, createTime: '2024-01-03' },
    { id: '4', studentId: '4', studentName: '赵六', parentName: '赵妈妈', parentPhone: '13800138004', studentNo: 'S004', totalHours: 50, usedHours: 48, remainingHours: 2, createTime: '2024-01-04' }
  ])
}

const mockGetHourTransactions = () => {
  const today = new Date()
  const formatDate = (d) => d.toISOString().split('T')[0]
  return Promise.resolve([
    { id: '1', transactionType: 'RECHARGE', hours: 100, balance: 100, remark: '首次充值', createTime: formatDate(new Date(today.getTime() - 30 * 86400000)) },
    { id: '2', transactionType: 'CONSUMPTION', hours: 1.5, balance: 98.5, remark: '篮球课消费', createTime: formatDate(new Date(today.getTime() - 25 * 86400000)) },
    { id: '3', transactionType: 'CONSUMPTION', hours: 1.5, balance: 97, remark: '篮球课消费', createTime: formatDate(new Date(today.getTime() - 20 * 86400000)) },
    { id: '4', transactionType: 'RECHARGE', hours: 50, balance: 147, remark: '续费充值', createTime: formatDate(new Date(today.getTime() - 15 * 86400000)) },
    { id: '5', transactionType: 'CONSUMPTION', hours: 1, balance: 146, remark: '体能课消费', createTime: formatDate(new Date(today.getTime() - 10 * 86400000)) }
  ])
}

const READ_ONLY_APIS = new Set([
  '/auth/login', '/auth/me',
  '/admin/users', '/business/students', '/business/courses',
  '/business/schedules', '/business/lessons', '/business/physical-tests',
  '/business/hours/accounts/', '/business/hours/transactions/'
])

const isReadOnlyApi = (url, method) => {
  if (method && method !== 'GET') return false
  return [...READ_ONLY_APIS].some(prefix => url.includes(prefix.replace(/\/$/, '')) || url.startsWith(prefix))
}

const normalizeUrl = (configOrUrl) => {
  if (typeof configOrUrl === 'string') return configOrUrl
  if (configOrUrl && configOrUrl.url) return configOrUrl.url
  return String(configOrUrl || '')
}

const callWithMock = async (apiFn, mockFn, params, config = {}) => {
  try {
    const result = await apiFn(params)
    return result
  } catch (err) {
    const url = normalizeUrl(config.url || (apiFn && apiFn.toString ? apiFn.toString() : ''))
    const method = config.method || 'GET'

    const canFallbackMock = ENABLE_MOCK_FALLBACK && isReadOnlyApi(url, method)
    if (!canFallbackMock) {
      ElMessage.error(`操作失败: ${err?.message || err}`)
      return Promise.reject(err)
    }

    const msg = err?.message || (typeof err === 'object' ? JSON.stringify(err) : String(err))
    const shortMsg = typeof msg === 'string' && msg.length > 120 ? msg.slice(0, 120) + '…' : msg
    console.warn('[API] 读接口降级为mock数据:', url, '| 原因:', shortMsg)
    return mockFn(params)
  }
}

const readList = (maybePageResult) => {
  if (Array.isArray(maybePageResult)) return maybePageResult
  if (maybePageResult && typeof maybePageResult === 'object') {
    if (Array.isArray(maybePageResult.list)) return maybePageResult.list
    if (Array.isArray(maybePageResult.records)) return maybePageResult.records
    if (Array.isArray(maybePageResult.rows)) return maybePageResult.rows
    if (Array.isArray(maybePageResult.data)) return readList(maybePageResult.data)
  }
  return []
}

const readTotal = (maybePageResult, fallbackLen = 0) => {
  if (Array.isArray(maybePageResult)) return maybePageResult.length
  if (maybePageResult && typeof maybePageResult === 'object') {
    const t = maybePageResult.total ?? maybePageResult.totalCount ?? maybePageResult.count
    if (typeof t === 'number') return t
    if (typeof t === 'string' && !Number.isNaN(Number(t))) return Number(t)
    if (typeof maybePageResult.data === 'object') return readTotal(maybePageResult.data, fallbackLen)
  }
  return fallbackLen
}

export { readList, readTotal }

const mapPage = (value, mapper) => {
  if (Array.isArray(value)) return value.map(mapper)
  if (!value || typeof value !== 'object') return value
  const key = ['list', 'records', 'rows'].find(k => Array.isArray(value[k]))
  return key ? { ...value, [key]: value[key].map(mapper) } : value
}

const collectAllPages = async (fetchPage, params = {}, pageSize = 500) => {
  const items = []
  let pageNum = 1
  let total = Number.POSITIVE_INFINITY
  while (items.length < total) {
    const page = await fetchPage({ ...params, pageNum, pageSize })
    const rows = readList(page)
    total = readTotal(page, items.length + rows.length)
    items.push(...rows)
    if (rows.length === 0 || items.length >= total) break
    pageNum += 1
  }
  return items
}

const withCreatedAt = (row = {}) => ({ ...row, createTime: row.createTime || row.createdAt })

const callWriteApi = async (apiFn, successMsg = '操作成功') => {
  try {
    const result = await apiFn()
    if (successMsg) ElMessage.success(successMsg)
    return result
  } catch (err) {
    const msg = err?.message || '操作失败，请检查后端服务'
    ElMessage.error(msg)
    return Promise.reject(err)
  }
}

export const login = async (params) => {
  const cleanUser = (resp) => {
    const token = resp?.accessToken || resp?.token
    const user = resp?.user || { username: params.username, role: resp?.role, id: resp?.userId }
    const role = user?.role || resp?.role
    if (token) {
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(user))
    }
    return { token, user, role }
  }
  const loginPayload = { ...(params || {}) }
  delete loginPayload.role
  delete loginPayload.captcha
  try {
    const resp = await service.post('/auth/login', loginPayload)
    const { token, user, role } = cleanUser(resp)
    if (String(role).toUpperCase() !== 'ADMIN') {
      try {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      } catch (_) {}
      throw new Error('当前账号无管理端访问权限')
    }
    return { token, user }
  } catch (err) {
    const msg = (err?.response?.data && (err.response.data.message || err.response.data.msg)) || err?.message || '登录失败'
    const code = err?.response?.data?.code
    if (code === 20005) {
      throw new Error(msg || '用户名或密码错误')
    }
    if (!err?.status && !err?.response) {
      throw new Error(`无法连接后端服务 (8080)：${msg || '连接被拒绝'}。请先确认后端启动：cd /Users/bytedance/SP-Smart/backend && export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && java -jar target/training-management-1.0.0.jar`)
    }
    throw new Error(msg || '登录失败')
  }
}

export const normalizeQueryParams = (params = {}) => {
  const out = {}
  for (const [k, rawV] of Object.entries(params || {})) {
    if (k === 'total' || k === 'disabled') continue
    let key = k
    if (k === 'page') key = 'pageNum'
    else if (k === 'size') key = 'pageSize'
    let v = rawV
    if (typeof v === 'string') {
      v = v.trim()
      if (v === '') continue
    } else if (v === undefined || v === null) {
      continue
    }
    out[key] = v
  }
  return out
}

export const getCurrentUser = () => callWithMock(() => service.get('/auth/me'), mockGetCurrentUser)

export const getUsers = async (params) => {
  const p = normalizeQueryParams(params)
  const data = await callWithMock(() => service.get('/admin/users', { params: p }), mockGetUsers, p, { url: '/admin/users', method: 'GET' })
  return mapPage(data, withCreatedAt)
}
export const getAllUsers = (params) => collectAllPages(getUsers, params)

export const createUser = (params) => callWriteApi(() => service.post('/admin/users', params), '创建用户成功')
export const updateUser = (id, params) => callWriteApi(() => service.put(`/admin/users/${id}`, params), '更新用户成功')
export const deleteUser = (id) => callWriteApi(() => service.delete(`/admin/users/${id}`), '删除用户成功')
export const resetUserPassword = (id, data) => callWriteApi(() => service.post(`/admin/users/${id}/reset-password`, data), '密码重置成功！新密码已生效')

export const getStudents = async (params) => {
  const p = normalizeQueryParams(params)
  const data = await callWithMock(() => service.get('/business/students', { params: p }), mockGetStudents, p, { url: '/business/students', method: 'GET' })
  return mapPage(data, withCreatedAt)
}
export const getAllStudents = (params) => collectAllPages(getStudents, params)

export const createStudent = (params) => callWriteApi(() => service.post('/business/students', params), '创建学员成功')
export const updateStudent = (id, params) => callWriteApi(() => service.put(`/business/students/${id}`, params), '更新学员成功')
export const deleteStudent = (id) => callWriteApi(() => service.delete(`/business/students/${id}`), '删除学员成功')

export const getCourses = async (params) => {
  const p = normalizeQueryParams(params)
  const data = await callWithMock(() => service.get('/business/courses', { params: p }), mockGetCourses, p, { url: '/business/courses', method: 'GET' })
  return mapPage(data, withCreatedAt)
}
export const getAllCourses = (params) => collectAllPages(getCourses, params)

export const createCourse = (params) => callWriteApi(() => service.post('/business/courses', params), '创建课程成功')
export const updateCourse = (id, params) => callWriteApi(() => service.put(`/business/courses/${id}`, params), '更新课程成功')
export const deleteCourse = (id) => callWriteApi(() => service.delete(`/business/courses/${id}`), '删除课程成功')

export const getSchedules = async (params) => {
  const p = normalizeQueryParams(params)
  const data = await callWithMock(() => service.get('/business/schedules', { params: p }), mockGetSchedules, p, { url: '/business/schedules', method: 'GET' })
  return mapPage(data, row => ({
    ...row,
    scheduleName: row.scheduleName || row.seriesName,
    scheduleType: row.scheduleType || (row.recurrenceType === 'NONE' ? 'SINGLE' : 'RECURRING')
  }))
}

export const getSchedule = (id) => service.get(`/business/schedules/${id}`)

export const createSchedule = (params) => callWriteApi(() => service.post('/business/schedules', params), '创建排课成功')
export const updateSchedule = (id, params) => callWriteApi(() => service.put(`/business/schedules/${id}`, params), '更新排课成功')
export const deleteSchedule = (id) => callWriteApi(() => service.delete(`/business/schedules/${id}`), '删除排课成功')

export const getLessons = (params) => {
  const p = normalizeQueryParams(params)
  return callWithMock(() => service.get('/business/lessons', { params: p }), mockGetLessons, p, { url: '/business/lessons', method: 'GET' })
}

export const startLesson = (id) => callWriteApi(() => service.post(`/business/lessons/${id}/start`), '上课成功')
export const completeLesson = (id) => callWriteApi(() => service.post(`/business/lessons/${id}/complete`), '结课成功')
export const cancelLesson = (id, reason) => callWriteApi(() => service.post(`/business/lessons/${id}/cancel`, { reason }), '取消课程成功')

export const recordAttendance = (params) => callWriteApi(() => service.post('/business/attendance', params), '考勤记录成功')

export const getPhysicalTests = async (params) => {
  const p = normalizeQueryParams(params)
  const data = await callWithMock(() => service.get('/business/physical-tests', { params: p }), mockGetPhysicalTests, p, { url: '/business/physical-tests', method: 'GET' })
  return mapPage(data, withCreatedAt)
}
export const getPhysicalItems = () => callWithMock(() => service.get('/business/physical-tests/items'), Promise.resolve([]), {}, { url: '/business/physical-tests/items', method: 'GET' })
export const getPhysicalBatches = () => service.get('/business/physical-tests/batches')
export const createPhysicalBatch = (params) => callWriteApi(() => service.post('/business/physical-tests/batches', params), '创建体测批次成功')
export const getPhysicalReport = (studentId) => service.get('/admin/reports/physical', { params: { studentId } })

export const createPhysicalTest = (params) => callWriteApi(() => service.post('/business/physical-tests', params), '创建体测记录成功')
export const updatePhysicalTest = (id, params) => callWriteApi(() => service.put(`/business/physical-tests/${id}`, params), '更新体测记录成功')
export const deletePhysicalTest = (id) => callWriteApi(() => service.delete(`/business/physical-tests/${id}`), '删除体测记录成功')

export const getHourAccounts = (params) => {
  const p = normalizeQueryParams(params)
  return callWithMock(() => service.get('/business/hours/accounts', { params: p }), mockGetHourAccounts, p, { url: '/business/hours/accounts', method: 'GET' })
}

export const getHourAccount = (studentId) => callWithMock(() => service.get(`/business/hours/accounts/${studentId}`), Promise.resolve({}), {}, { url: `/business/hours/accounts/${studentId}`, method: 'GET' })

export const rechargeHours = (params) => callWriteApi(() => service.post('/business/hours/recharge', params), '充值成功')
export const adjustHours = (params) => callWriteApi(() => service.post('/business/hours/adjust', params), '课时添加成功')
export const refundHours = (params) => callWriteApi(() => service.post('/business/hours/refund', params), '课时减少成功')

export const getHourConsumeStats = (params) => service.get('/admin/hour-stats/consume-stats', { params })

export const getHourTransactions = async (studentId, params = {}) => {
  const p = normalizeQueryParams(params)
  const data = await callWithMock(() => service.get(`/business/hours/transactions/${studentId}`, { params: p }), mockGetHourTransactions, p, { url: `/business/hours/transactions/${studentId}`, method: 'GET' })
  return mapPage(data, row => ({
    ...row,
    hours: row.hours ?? Math.abs(Number(row.changeAmount || 0)),
    balance: row.balance ?? row.afterBalance,
    remark: row.remark || row.reason,
    createTime: row.createTime || row.createdAt
  }))
}

export default service
