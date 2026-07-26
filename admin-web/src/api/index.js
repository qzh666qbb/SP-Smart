import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
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
    const res = response.data
    if (res.code !== 0 && res.code !== 200) {
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data || res
  },
  error => {
    console.warn('[API] Request failed, using mock data:', error.message)
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

const callWithMock = async (apiFn, mockFn, params) => {
  try {
    return await apiFn(params)
  } catch (err) {
    console.warn('[API] Falling back to mock:', err.message)
    return mockFn(params)
  }
}

export const login = (params) => callWithMock((p) => service.post('/auth/login', p), mockLogin, params)

export const getCurrentUser = () => callWithMock(() => service.get('/auth/me'), mockGetCurrentUser)

export const getUsers = (params) => callWithMock((p) => service.get('/business/users', { params }), mockGetUsers, params)

export const createUser = (params) => callWithMock((p) => service.post('/business/users', p), () => Promise.resolve(params), params)

export const updateUser = (id, params) => callWithMock((p) => service.put(`/business/users/${id}`, p), () => Promise.resolve({ ...params, id }), params)

export const deleteUser = (id) => callWithMock(() => service.delete(`/business/users/${id}`), () => Promise.resolve({ id }))

export const getStudents = (params) => callWithMock((p) => service.get('/business/students', { params }), mockGetStudents, params)

export const createStudent = (params) => callWithMock((p) => service.post('/business/students', p), () => Promise.resolve(params), params)

export const updateStudent = (id, params) => callWithMock((p) => service.put(`/business/students/${id}`, p), () => Promise.resolve({ ...params, id }), params)

export const deleteStudent = (id) => callWithMock(() => service.delete(`/business/students/${id}`), () => Promise.resolve({ id }))

export const getCourses = (params) => callWithMock((p) => service.get('/business/courses', { params }), mockGetCourses, params)

export const createCourse = (params) => callWithMock((p) => service.post('/business/courses', p), () => Promise.resolve(params), params)

export const updateCourse = (id, params) => callWithMock((p) => service.put(`/business/courses/${id}`, p), () => Promise.resolve({ ...params, id }), params)

export const deleteCourse = (id) => callWithMock(() => service.delete(`/business/courses/${id}`), () => Promise.resolve({ id }))

export const getSchedules = (params) => callWithMock((p) => service.get('/business/schedules', { params }), mockGetSchedules, params)

export const createSchedule = (params) => callWithMock((p) => service.post('/business/schedules', p), () => Promise.resolve(params), params)

export const updateSchedule = (id, params) => callWithMock((p) => service.put(`/business/schedules/${id}`, p), () => Promise.resolve({ ...params, id }), params)

export const deleteSchedule = (id) => callWithMock(() => service.delete(`/business/schedules/${id}`), () => Promise.resolve({ id }))

export const getLessons = (params) => callWithMock((p) => service.get('/business/lessons', { params }), mockGetLessons, params)

export const startLesson = (id) => callWithMock(() => service.post(`/business/lessons/${id}/start`), () => Promise.resolve({ success: true }))

export const completeLesson = (id) => callWithMock(() => service.post(`/business/lessons/${id}/complete`), () => Promise.resolve({ success: true }))

export const cancelLesson = (id, reason) => callWithMock(() => service.post(`/business/lessons/${id}/cancel`, { reason }), () => Promise.resolve({ success: true }))

export const recordAttendance = (params) => callWithMock((p) => service.post('/business/attendance', p), () => Promise.resolve({ success: true }), params)

export const getPhysicalTests = (params) => callWithMock((p) => service.get('/business/physical-tests', { params }), mockGetPhysicalTests, params)

export const createPhysicalTest = (params) => callWithMock((p) => service.post('/business/physical-tests', p), () => Promise.resolve({ record: params }), params)

export const updatePhysicalTest = (id, params) => callWithMock((p) => service.put(`/business/physical-tests/${id}`, p), () => Promise.resolve({ ...params, id }), params)

export const deletePhysicalTest = (id) => callWithMock(() => service.delete(`/business/physical-tests/${id}`), () => Promise.resolve({ id }))

export const getHourAccounts = (params) => callWithMock((p) => service.get('/business/hour-accounts', { params }), mockGetHourAccounts, params)

export const rechargeHours = (params) => callWithMock((p) => service.post('/business/hour-accounts/recharge', p), () => Promise.resolve({ success: true }), params)

export const refundHours = (params) => callWithMock((p) => service.post('/business/hour-accounts/refund', p), () => Promise.resolve({ success: true }), params)

export const getHourTransactions = (params) => callWithMock((p) => service.get('/business/hour-transactions', { params }), mockGetHourTransactions, params)

export default service
