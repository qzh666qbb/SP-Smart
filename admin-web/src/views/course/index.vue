<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>课程管理</h2>
        <p class="page-desc">管理课程信息，包括课程名称、描述和课时设置</p>
      </div>
      <el-button type="primary" class="btn-primary" @click="handleAdd">
        <el-icon><el-icon-plus /></el-icon>
        新增课程
      </el-button>
    </div>
    
    <div class="page-card">
      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="搜索课程名称" class="search-input" @keyup.enter="handleSearch" />
        <el-button type="primary" class="btn-search" @click="handleSearch">搜索</el-button>
        <el-button class="btn-reset" @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="tableData" class="custom-table" :row-class-name="tableRowClassName" style="width: 100%;">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="courseName" label="课程名称" min-width="120" />
        <el-table-column prop="description" label="课程描述" />
        <el-table-column prop="duration" label="课时(分钟)" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="140" />
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

    <el-dialog v-model="showAddModal" :title="isEdit ? '编辑课程' : '新增课程'" width="520px" class="custom-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" class="modal-form" label-width="110px">
        <el-form-item prop="courseCode" label="课程编码">
          <el-input v-model="form.courseCode" placeholder="如：BASKET-001" maxlength="32" />
        </el-form-item>
        <el-form-item prop="courseName" label="课程名称">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item prop="courseType" label="课程类型">
          <el-select v-model="form.courseType" placeholder="请选择课程类型" style="width: 100%">
            <el-option label="一对一" value="ONE_TO_ONE" />
            <el-option label="小班课" value="SMALL_CLASS" />
            <el-option label="团体课" value="GROUP" />
          </el-select>
        </el-form-item>
        <el-form-item prop="defaultDurationMinutes" label="课时(分钟)">
          <el-input-number v-model="form.defaultDurationMinutes" :min="1" :max="300" />
        </el-form-item>
        <el-form-item prop="defaultConsumeHours" label="默认消耗课时">
          <el-input-number v-model="form.defaultConsumeHours" :min="0.5" :max="20" :step="0.5" :precision="1" />
        </el-form-item>
        <el-form-item prop="description" label="课程描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入课程描述" :rows="3" />
        </el-form-item>
        <el-form-item prop="status" label="状态">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourses, createCourse, updateCourse, deleteCourse } from '@/api'

const tableData = ref([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showAddModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: '',
  courseCode: '',
  courseName: '',
  courseType: 'SMALL_CLASS',
  defaultDurationMinutes: 90,
  defaultConsumeHours: 1.5,
  description: '',
  status: 'ENABLED',
  duration: 90
})

const rules = {
  courseCode: [{ required: true, message: '请输入课程编码', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  defaultDurationMinutes: [{ required: true, message: '请输入课时(分钟)', trigger: 'blur' }],
  defaultConsumeHours: [{ required: true, message: '请输入默认消耗课时', trigger: 'blur' }]
}

const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex % 2 === 1) {
    return 'row-stripe'
  }
  return ''
}

const fetchData = async () => {
  try {
    const res = await getCourses({ ...searchForm, ...pagination })
    tableData.value = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
    pagination.total = typeof res?.total === 'number' ? res.total : tableData.value.length
  } catch (err) {
    console.error(err)
    tableData.value = []
    pagination.total = 0
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.keyword = ''
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

const resetForm = () => {
  form.id = ''
  form.courseCode = ''
  form.courseName = ''
  form.courseType = 'SMALL_CLASS'
  form.defaultDurationMinutes = 90
  form.defaultConsumeHours = 1.5
  form.description = ''
  form.status = 'ENABLED'
}

const handleEdit = (row) => {
  isEdit.value = true
  resetForm()
  Object.assign(form, row || {})
  if (row && typeof row.defaultDurationMinutes === 'number') form.duration = row.defaultDurationMinutes
  showAddModal.value = true
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  showAddModal.value = true
}

const handleDelete = async (row) => {
  const target = row?.courseName || row?.name ? `课程 ${row.courseName || row.name}` : '该课程'
  try {
    await ElMessageBox.confirm(`确定删除${target}？删除后已引用该课程的排课系列将无法再匹配课时，请确认已先清理相关排课。`, '删除确认', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', distinguishCancelAndClose: true
    })
  } catch (_) { return }
  try {
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const payload = {
        courseCode: form.courseCode,
        courseName: form.courseName,
        courseType: form.courseType,
        defaultDurationMinutes: Number(form.defaultDurationMinutes) || 0,
        defaultConsumeHours: Number(form.defaultConsumeHours) || 0,
        description: form.description,
        status: form.status || 'ENABLED'
      }
      if (isEdit.value) {
        await updateCourse(form.id, payload)
        ElMessage.success('更新成功')
      } else {
        await createCourse(payload)
        ElMessage.success('创建成功')
      }
      showAddModal.value = false
      fetchData()
    } catch (err) {
      ElMessage.error(err?.message || '操作失败')
    }
  })
}

fetchData()
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
.modal-form :deep(.el-select__wrapper) {
  border-radius: 6px;
  border-color: #e5e7eb;
}
</style>
