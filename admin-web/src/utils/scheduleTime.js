export function scheduleTimeError(startTime, endTime) {
  if (!startTime || !endTime) return ''
  const toSeconds = value => {
    const match = /^(\d{2}):(\d{2})(?::(\d{2}))?$/.exec(value)
    if (!match) return NaN
    const [, hour, minute, second = '0'] = match
    if (+hour > 23 || +minute > 59 || +second > 59) return NaN
    return +hour * 3600 + +minute * 60 + +second
  }
  const start = toSeconds(startTime)
  const end = toSeconds(endTime)
  if (!Number.isFinite(start) || !Number.isFinite(end)) return '请填写有效的上课时间'
  return end <= start ? '结束时间必须晚于开始时间' : ''
}
