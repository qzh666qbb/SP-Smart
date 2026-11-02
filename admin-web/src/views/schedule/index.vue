<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>排课管理</h2>
        <p class="page-desc">管理课程排课安排，包括时间、教师和课程设置</p>
      </div>
      <el-button type="primary" class="btn-primary" @click="handleAdd">
        <el-icon><el-icon-plus /></el-icon>
        新增排课
      </el-button>
    </div>
    
    <div class="page-card">
      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="搜索课程名称" class="search-input" @keyup.enter="handleSearch" />
        <el-date-picker v-model="searchForm.date" type="date" placeholder="选择日期" class="search-date" />
        <el-button type="primary" class="btn-search" @click="handleSearch">搜索</el-button>
        <el-button class="btn-reset" @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="tableData" class="custom-table" :row-class-name="tableRowClassName" style="width: 100%;">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="scheduleName" label="排课名称" min-width="120" />
        <el-table-column label="课程名称" min-width="100">
          <template #default="{ row }">{{ row.courseName || getCourseName(row.courseId) }}</template>
        </el-table-column>
        <el-table-column label="授课老师" min-width="180">
          <template #default="{ row }">
            <div>主教：{{ row.teacherName || getTeacherName(row.teacherId) }}</div>
            <div v-if="row.assistantTeacherId1 || row.assistantTeacherId2" class="assistant-text">
              助教：{{ getAssistantNames(row) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column prop="startTime" label="开始时间" width="80" />
        <el-table-column prop="endTime" label="结束时间" width="80" />
        <el-table-column prop="scheduleType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag class="role-tag">{{ row.scheduleType === 'SINGLE' ? '单次' : '周期' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" class="btn-edit" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" class="btn-delete" @click.stop="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrap">
        <el-pagination 
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50]"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
        />
      </div>
    </div>

    <el-dialog v-model="showAddModal" :title="isEdit ? '编辑排课' : '新增排课'" width="560px" class="custom-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" class="modal-form" label-width="110px">
        <el-form-item prop="seriesName" label="排课名称">
          <el-input v-model="form.seriesName" placeholder="请输入排课名称" />
        </el-form-item>
        <el-form-item prop="courseId" label="课程">
          <el-select v-model="form.courseId" placeholder="请选择课程" filterable style="width: 100%">
            <el-option v-for="course in courses" :key="course.id" :label="course.courseName || course.name" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item prop="teacherId" label="主教">
          <el-select v-model="form.teacherId" placeholder="请选择 1 位主教" filterable style="width: 100%" @change="handlePrimaryTeacherChange">
            <el-option v-for="u in teachers" :key="u.profileId" :label="u.profileName || u.username" :value="u.profileId" />
          </el-select>
        </el-form-item>
        <el-form-item prop="assistantTeacherIds" label="助教">
          <el-select v-model="form.assistantTeacherIds" multiple :multiple-limit="2" placeholder="可选择最多 2 位助教" filterable style="width: 100%">
            <el-option v-for="u in assistantTeacherOptions" :key="u.profileId" :label="u.profileName || u.username" :value="u.profileId" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" prop="studentIds" label="上课学员">
          <el-select v-model="form.studentIds" multiple filterable collapse-tags placeholder="请选择学员" style="width: 100%">
            <el-option v-for="s in students" :key="s.id" :label="`${s.name}（${s.studentNo || s.id}）`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item prop="recurrenceType" label="排课类型">
          <el-select v-model="form.recurrenceType" placeholder="请选择排课类型" style="width: 100%" @change="v => { if (v === 'NONE') form.weekDays = [] }">
            <el-option label="单次" value="NONE" />
            <el-option label="周循环" value="WEEKLY" />
          </el-select>
        </el-form-item>
        <el-form-item prop="weekDays" label="星期" v-if="form.recurrenceType === 'WEEKLY'">
          <el-select v-model="form.weekDays" multiple placeholder="请选择星期" style="width: 100%">
            <el-option label="周一" value="MONDAY" />
            <el-option label="周二" value="TUESDAY" />
            <el-option label="周三" value="WEDNESDAY" />
            <el-option label="周四" value="THURSDAY" />
            <el-option label="周五" value="FRIDAY" />
            <el-option label="周六" value="SATURDAY" />
            <el-option label="周日" value="SUNDAY" />
          </el-select>
        </el-form-item>
        <el-form-item prop="startDate" label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" placeholder="请选择开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="endDate" label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" placeholder="请选择结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="startTime" label="开始时间">
          <el-time-picker v-model="form.startTime" format="HH:mm" value-format="HH:mm" placeholder="请选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="endTime" label="结束时间">
          <el-time-picker v-model="form.endTime" format="HH:mm" value-format="HH:mm" placeholder="请选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="consumeHours" label="消耗课时">
          <el-input-number v-model="form.consumeHours" :min="0.5" :max="20" :step="0.5" :precision="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { scheduleTimeError } from '@/utils/scheduleTime'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSchedules, createSchedule, updateSchedule, deleteSchedule, getAllCourses, getAllUsers, getAllStudents } from '@/api'

const tableData = ref([])
const courses = ref([])
const teachers = ref([])
const students = ref([])
const searchForm = reactive({ keyword: '', date: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showAddModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: '',
  seriesName: '',
  courseId: '',
  teacherId: '',
  assistantTeacherIds: [],
  studentIds: [],
  classGroupId: null,
  recurrenceType: 'NONE',
  weekDays: [],
  startDate: '',
  endDate: '',
  startTime: '',
  endTime: '',
  consumeHours: 1.5
})

const rules = {
  seriesName: [{ required: true, message: '请输入排课名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择授课老师', trigger: 'change' }],
  studentIds: [{ type: 'array', required: true, min: 1, message: '请至少选择一名学员', trigger: 'change' }],
  recurrenceType: [{ required: true, message: '请选择排课类型', trigger: 'change' }],
  weekDays: [{ validator: (_rule, value, callback) => {
    if (form.recurrenceType === 'WEEKLY' && (!Array.isArray(value) || value.length === 0)) callback(new Error('请选择星期'))
    else callback()
  }, trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ validator: (_rule, value, callback) => {
    if (form.recurrenceType === 'WEEKLY' && !value) callback(new Error('请选择结束日期'))
    else callback()
  }, trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: (_rule, value, callback) => {
      const error = scheduleTimeError(form.startTime, value)
      callback(error ? new Error(error) : undefined)
    }, trigger: 'change' }
  ],
  consumeHours: [{ required: true, message: '请输入消耗课时', trigger: 'blur' }]
}

watch(() => form.startTime, () => {
  if (showAddModal.value && form.endTime) {
    formRef.value?.validateField('endTime').catch(() => {})
  }
})

const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex % 2 === 1) return 'row-stripe'
  return ''
}

let fetchRequestId = 0
const fetchData = async () => {
  const requestId = ++fetchRequestId
  try {
    const res = await getSchedules({ ...searchForm, ...pagination })
    if (requestId !== fetchRequestId) return
    tableData.value = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
    pagination.total = typeof res?.total === 'number' ? res.total : tableData.value.length
  } catch (err) {
    console.error(err)
    if (requestId !== fetchRequestId) return
    tableData.value = []
    pagination.total = 0
  }
}

const fetchCourses = async () => {
  try {
    courses.value = await getAllCourses()
  } catch (err) {
    console.error(err)
    courses.value = []
  }
}

const fetchTeachers = async () => {
  try {
    const all = await getAllUsers({ role: 'TEACHER' })
    teachers.value = all
      .filter(u => u.role === 'TEACHER' && u.profileId)
      .sort((a, b) => String(a.profileName || a.username || '').localeCompare(String(b.profileName || b.username || ''), 'zh-CN'))
  } catch (err) {
    console.error(err)
    teachers.value = []
  }
}

const fetchStudents = async () => {
  try {
    const list = await getAllStudents()
    students.value = list.sort((a, b) => String(a.name || '').localeCompare(String(b.name || ''), 'zh-CN'))
  } catch (err) {
    console.error(err)
    students.value = []
  }
}

const getCourseName = (id) => courses.value.find(c => String(c.id) === String(id))?.courseName || `课程 #${id || '-'}`
const getTeacherName = (id) => teachers.value.find(t => String(t.profileId) === String(id))?.profileName || `教师 #${id || '-'}`
const assistantTeacherOptions = computed(() => teachers.value.filter(t => String(t.profileId) !== String(form.teacherId)))
const getAssistantNames = row => [row.assistantTeacherId1, row.assistantTeacherId2]
  .filter(Boolean).map(getTeacherName).join('、')
const handlePrimaryTeacherChange = id => {
  form.assistantTeacherIds = form.assistantTeacherIds.filter(assistantId => String(assistantId) !== String(id))
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const resetSearch = () => { searchForm.keyword = ''; searchForm.date = ''; pagination.page = 1; fetchData() }
const handleSizeChange = (size) => { pagination.size = size; fetchData() }
const handleCurrentChange = (page) => { pagination.page = page; fetchData() }

const resetForm = () => {
  form.id = ''
  form.seriesName = ''
  form.courseId = ''
  form.teacherId = ''
  form.assistantTeacherIds = []
  form.studentIds = []
  form.classGroupId = null
  form.recurrenceType = 'NONE'
  form.weekDays = []
  form.startDate = ''
  form.endDate = ''
  form.startTime = ''
  form.endTime = ''
  form.consumeHours = 1.5
}

const toPlainDate = (v) => {
  if (!v) return v
  if (typeof v === 'string') return v.slice(0, 10)
  return v
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  showAddModal.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  resetForm()
  Object.assign(form, row || {})
  // 兼容旧列名
  form.seriesName = form.seriesName || row.scheduleName
  form.recurrenceType = form.recurrenceType || (row.scheduleType === 'RECURRING' ? 'WEEKLY' : 'NONE')
  form.startDate = form.startDate || toPlainDate(row.startDate)
  form.endDate = form.endDate || toPlainDate(row.endDate)
  form.weekDays = String(row.weekdays || row.weekDays || '')
    .split(',').map(v => v.trim()).filter(Boolean)
  form.assistantTeacherIds = [row.assistantTeacherId1, row.assistantTeacherId2].filter(Boolean)
  showAddModal.value = true
}

const handleDelete = async (row) => {
  const target = row?.seriesName || row?.scheduleName ? `排课 ${row.seriesName || row.scheduleName}` : '该排课'
  try {
    await ElMessageBox.confirm(`确定删除${target}？未完成课次会被取消，已完成记录会保留。`, '删除确认', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', distinguishCancelAndClose: true
    })
  } catch (_) { return }
  try {
    await deleteSchedule(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = {
      seriesName: form.seriesName,
      courseId: Number(form.courseId) || 0,
      teacherId: Number(form.teacherId) || 0,
      assistantTeacherId1: form.assistantTeacherIds[0] ? Number(form.assistantTeacherIds[0]) : null,
      assistantTeacherId2: form.assistantTeacherIds[1] ? Number(form.assistantTeacherIds[1]) : null,
      studentIds: form.studentIds.map(id => Number(id) || id),
      classGroupId: form.classGroupId ? Number(form.classGroupId) : null,
      recurrenceType: form.recurrenceType,
      weekdays: Array.isArray(form.weekDays) && form.weekDays.length > 0 ? form.weekDays.join(',') : null,
      startDate: form.startDate,
      endDate: form.endDate || null,
      startTime: form.startTime,
      endTime: form.endTime,
      consumeHours: Number(form.consumeHours) || 0
    }
    if (isEdit.value) {
      await updateSchedule(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createSchedule(payload)
      ElMessage.success('创建成功')
    }
    showAddModal.value = false
    fetchData()
  } catch (err) {
    ElMessage.error(err?.message || '操作失败')
  }
}

// 新增排课按钮绑定到 handleAdd
// 模板中 showAddModal=true 但前面模板会替换
fetchData()
fetchCourses()
fetchTeachers()
fetchStudents()
</script>

<style scoped>
.page-container {
  padding: 0;
}

.assistant-text {
  margin-top: 3px;
  color: #6b7280;
  font-size: 12px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title-group h2 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 6px 0;
}

.page-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.page-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f3f4f6;
}

.search-input {
  width: 220px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  border-color: #e5e7eb;
  transition: all 0.2s ease;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #93c5fd;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.06);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.search-date {
  width: 180px;
}

.search-date :deep(.el-date-picker__wrapper) {
  border-radius: 6px;
  border-color: #e5e7eb;
}

.btn-search {
  border-radius: 6px;
  padding: 0 20px;
  transition: all 0.2s ease;
}

.btn-search:hover {
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
}

.btn-reset {
  border-radius: 6px;
  padding: 0 20px;
  transition: all 0.2s ease;
}

.btn-reset:hover {
  background: #f3f4f6;
}

.custom-table {
  width: 100%;
}

.custom-table :deep(.el-table__header-wrapper) {
  border-radius: 6px 6px 0 0;
  overflow: hidden;
}

.custom-table :deep(.el-table__header tr) {
  background: #f9fafb;
}

.custom-table :deep(.el-table__header th) {
  color: #6b7280;
  font-weight: 500;
  border-bottom: 2px solid #e5e7eb;
  padding: 14px 0;
  font-size: 14px;
}

.custom-table :deep(.el-table__body tr:hover) {
  background: #eff6ff;
}

.custom-table :deep(.el-table__body .row-stripe) {
  background: #fafafa;
}

.custom-table :deep(.el-table__body .row-stripe:hover) {
  background: #eff6ff;
}

.custom-table :deep(.el-table__body td) {
  padding: 14px 0;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
}

.role-tag {
  border-radius: 4px;
  font-size: 12px;
  padding: 2px 8px;
}

.btn-edit {
  border-radius: 4px;
  margin-right: 8px;
  padding: 4px 12px;
  transition: all 0.2s ease;
}

.btn-edit:hover {
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
}

.btn-delete {
  border-radius: 4px;
  padding: 4px 12px;
  transition: all 0.2s ease;
}

.btn-delete:hover {
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.pagination :deep(.el-pager li) {
  border-radius: 6px;
}

.pagination :deep(.el-pager li.is-active) {
  background: #2563eb;
  color: #fff;
}

.modal-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.modal-form :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 500;
}

.modal-form :deep(.el-input__wrapper),
.modal-form :deep(.el-select__wrapper),
.modal-form :deep(.el-date-picker__wrapper),
.modal-form :deep(.el-time-picker__wrapper) {
  border-radius: 6px;
  border-color: #e5e7eb;
}
</style>
