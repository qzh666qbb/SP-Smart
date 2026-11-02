import test from 'node:test'
import assert from 'node:assert/strict'
import { apiErrorMessage } from '../src/utils/apiError.js'
import { scheduleTimeError } from '../src/utils/scheduleTime.js'

test('排课失败展示具体校验原因', () => {
  assert.equal(apiErrorMessage({ code: 10001, message: '参数校验失败', data: { timeRangeValid: '结束时间必须晚于开始时间' } }), '结束时间必须晚于开始时间')
  assert.equal(apiErrorMessage({ code: 10001, message: '参数校验失败', data: { a: '请选择时间', b: '请选择时间' } }), '请选择时间')
  assert.equal(apiErrorMessage({ code: 10003, message: '教师时间冲突' }), '教师时间冲突')
  assert.equal(apiErrorMessage(null, '网络错误'), '网络错误')
})
test('排课时间支持时分和时分秒，拒绝相等、倒序和无效时间', () => {
  assert.equal(scheduleTimeError('09:00', '10:00'), '')
  assert.equal(scheduleTimeError('09:00:00', '09:01'), '')
  assert.equal(scheduleTimeError('09:00:00', '09:00'), '结束时间必须晚于开始时间')
  assert.equal(scheduleTimeError('18:00', '09:00'), '结束时间必须晚于开始时间')
  assert.equal(scheduleTimeError('09:00', '24:00'), '请填写有效的上课时间')
})
