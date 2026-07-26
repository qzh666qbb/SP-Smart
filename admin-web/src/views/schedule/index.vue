<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>排课管理</h2>
        <p class="page-desc">管理课程排课安排，包括时间、教师和课程设置</p>
      </div>
      <el-button type="primary" class="btn-primary" @click="showAddModal = true">
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
        <el-table-column prop="courseName" label="课程名称" min-width="100" />
        <el-table-column prop="teacherName" label="授课老师" min-width="90" />
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
              <el-button type="danger" size="small" class="btn-delete" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="showAddModal" :title="isEdit ? '编辑排课' : '新增排课'" width="520px" class="custom-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" class="modal-form">
        <el-form-item prop="scheduleName" label="排课名称">
          <el-input v-model="form.scheduleName" placeholder="请输入排课名称" />
        </el-form-item>
        <el-form-item prop="courseId" label="课程">
          <el-select v-model="form.courseId" placeholder="请选择课程">
            <el-option v-for="course in courses" :key="course.id" :label="course.courseName" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item prop="scheduleType" label="排课类型">
          <el-select v-model="form.scheduleType" placeholder="请选择排课类型">
            <el-option label="单次" value="SINGLE" />
            <el-option label="周期" value="RECURRING" />
          </el-select>
        </el-form-item>
        <el-form-item prop="weekDays" label="星期" v-if="form.scheduleType === 'RECURRING'">
          <el-select v-model="form.weekDays" multiple placeholder="请选择星期">
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
          <el-date-picker v-model="form.startDate" type="date" placeholder="请选择开始日期" />
        </el-form-item>
        <el-form-item prop="endDate" label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" placeholder="请选择结束日期" />
        </el-form-item>
        <el-form-item prop="startTime" label="开始时间">
          <el-time-picker v-model="form.startTime" format="HH:mm" placeholder="请选择开始时间" />
        </el-form-item>
        <el-form-item prop="endTime" label="结束时间">
          <el-time-picker v-model="form.endTime" format="HH:mm" placeholder="请选择结束时间" />
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
import { ref, reactive } from 'vue'
import { getSchedules, createSchedule, updateSchedule, deleteSchedule, getCourses } from '@/api'

const tableData = ref([])
const courses = ref([])
const searchForm = reactive({ keyword: '', date: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showAddModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: '',
  scheduleName: '',
  courseId: '',
  scheduleType: 'SINGLE',
  weekDays: [],
  startDate: '',
  endDate: '',
  startTime: '',
  endTime: ''
})

const rules = {
  scheduleName: [{ required: true, message: '请输入排课名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  scheduleType: [{ required: true, message: '请选择排课类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex % 2 === 1) {
    return 'row-stripe'
  }
  return ''
}

const fetchData = async () => {
  try {
    const res = await getSchedules({ ...searchForm, ...pagination })
    tableData.value = res.list || res
    pagination.total = res.total || tableData.value.length
  } catch (err) {
    console.error(err)
  }
}

const fetchCourses = async () => {
  try {
    const res = await getCourses()
    courses.value = res.list || res
  } catch (err) {
    console.error(err)
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.date = ''
  pagination.page = 1
  fetchData()
}

const handleSizeChange = (size) => {
  pagination.size = size
  fetchData()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchData()
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  showAddModal.value = true
}

const handleDelete = (row) => {
  if (!confirm(`确定删除排课 ${row.scheduleName} ?`)) return
  deleteSchedule(row.id).then(() => {
    alert('删除成功')
    fetchData()
  }).catch(() => alert('删除失败'))
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await updateSchedule(form.id, form)
        alert('更新成功')
      } else {
        await createSchedule(form)
        alert('创建成功')
      }
      showAddModal.value = false
      fetchData()
    } catch (err) {
      alert('操作失败')
    }
  })
}

fetchData()
fetchCourses()
</script>

<style scoped>
.page-container {
  padding: 0;
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
