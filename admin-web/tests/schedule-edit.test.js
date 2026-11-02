import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'
import { createRequire } from 'node:module'
import { parse, compileScript } from '@vue/compiler-sfc'
import { transformSync } from 'esbuild'

const require = createRequire(import.meta.url)
function page(api) {
  const source = fs.readFileSync(new URL('../src/views/schedule/index.vue', import.meta.url), 'utf8')
  const { descriptor } = parse(source)
  const compiled = compileScript(descriptor, { id: 'schedule-edit-test' })
  const code = transformSync(compiled.content, { format: 'cjs', loader: 'js' }).code
  const module = { exports: {} }
  vm.runInNewContext(code, { module, exports: module.exports, require: name => {
    if (name === '@/api') return {
      getSchedules: async () => ({ list: [], total: 0 }),
      getAllCourses: async () => [], getAllUsers: async () => [], getAllStudents: async () => [], ...api,
    }
    if (name === '@/utils/scheduleTime') return { scheduleTimeError: () => '' }
    if (name === 'element-plus') return { ElMessage: { success() {}, error() {} }, ElMessageBox: {} }
    return require(name)
  }, console })
  return module.exports.default.setup({}, { expose() {} })
}

test('编辑排课读取详情并回填学员，修改后提交新名单', async () => {
  let submitted
  const h = page({
    getSchedule: async id => ({ id, seriesName: '测试排课', teacherId: 3, courseId: 1,
      studentIds: [101, 102], startDate: '2026-11-23', startTime: '09:00:00', endTime: '10:00:00', recurrenceType: 'NONE', consumeHours: 1 }),
    updateSchedule: async (id, body) => { submitted = { id, body } },
  })
  await h.handleEdit({ id: 5 })
  assert.equal(h.isEdit.value, true)
  assert.equal(h.showAddModal.value, true)
  assert.deepEqual(Array.from(h.form.studentIds), [101, 102])
  h.form.studentIds = [102, 103]
  h.formRef.value = { validate: async () => true }
  await h.handleSubmit()
  assert.equal(submitted.id, 5)
  assert.deepEqual(Array.from(submitted.body.studentIds), [102, 103])
})

test('排课详情失败时不打开空名单编辑框', async () => {
  const h = page({ getSchedule: async () => { throw new Error('请求失败') } })
  await h.handleEdit({ id: 5 })
  assert.equal(h.showAddModal.value, false)
})
