import request from '@/utils/request'
export const resetUserPassword = (id, data) => request.post(`/admin/users/${id}/reset-password`, data)
