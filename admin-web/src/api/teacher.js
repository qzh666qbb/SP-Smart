import request from '@/utils/request'
export const preRegisterTeacher = (data) => request.post('/admin/teacher/pre-register', data)
export const listTeacherPreRegisters = (params) => request.get('/admin/teacher/pre-register', { params })
export const listTeacherApplies = (params) => request.get('/admin/teacher/applies', { params })
export const reviewTeacherApply = (id, data) => request.post(`/admin/teacher/applies/${id}/review`, data)
export const submitTeacherApplyPublic = (data) => request.post('/public/teacher/apply/submit', data)
