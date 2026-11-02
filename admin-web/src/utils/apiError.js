export function apiErrorMessage(data, fallback = '请求失败，请稍后重试') {
  if (data?.code === 10001 && data.data && typeof data.data === 'object' && !Array.isArray(data.data)) {
    const messages = [...new Set(Object.values(data.data).filter(value => typeof value === 'string' && value.trim()))]
    if (messages.length) return messages.join('；')
  }
  return typeof data?.message === 'string' && data.message.trim() ? data.message : fallback
}
