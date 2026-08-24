<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>教师预登记管理</h2>
        <p class="page-desc">管理教师手机号预登记信息，支持新增、查询和状态切换</p>
      </div>
      <el-button type="primary" class="btn-primary" @click="handleAddPreRegister">
        <el-icon><Plus /></el-icon>
        新增预登记
      </el-button>
    </div>

    <div class="page-card">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form" @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索手机号/姓名" class="search-input" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="btn-search" @click="handleSearch">查询</el-button>
            <el-button class="btn-reset" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" class="custom-table" :row-class-name="tableRowClassName" style="width: 100%;">
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="createdBy" label="创建人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" effect="light">
              {{ row.status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.status === 'ACTIVE'"
                size="small"
                type="danger"
                link
                @click="handleToggleStatus(row, 'INACTIVE')"
              >禁用</el-button>
              <el-button
                v-else
                size="small"
                type="primary"
                link
                @click="handleToggleStatus(row, 'ACTIVE')"
              >启用</el-button>
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

    <el-dialog v-model="showAddModal" title="新增预登记" width="420px" class="custom-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" class="modal-form">
        <el-form-item prop="phone" label="手机号">
          <el-input v-model="form.phone" placeholder="请输入 11 位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item prop="name" label="真实姓名">
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { preRegisterTeacher, listTeacherPreRegisters } from '@/api/teacher'

const tableData = ref([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showAddModal = ref(false)
const formRef = ref(null)

const form = reactive({
  phone: '',
  name: ''
})

const rules = computed(() => ({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的 11 位手机号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ]
}))

const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex % 2 === 1) {
    return 'row-stripe'
  }
  return ''
}

const fetchData = async () => {
  try {
    const params = {
      keyword: searchForm.keyword || '',
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    const res = await listTeacherPreRegisters(params)
    tableData.value = Array.isArray(res?.list) ? res.list : (Array.isArray(res?.records) ? res.records : (Array.isArray(res?.rows) ? res.rows : (Array.isArray(res) ? res : [])))
    pagination.total = typeof res?.total === 'number' ? res.total : (typeof res?.totalCount === 'number' ? res.totalCount : tableData.value.length)
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

const handleAddPreRegister = () => {
  form.phone = ''
  form.name = ''
  showAddModal.value = true
}

const handleSubmitAdd = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await preRegisterTeacher({ phone: form.phone.trim(), name: form.name.trim() })
      ElMessage.success('新增预登记成功')
      showAddModal.value = false
      fetchData()
    } catch (err) {
      ElMessage.error(err?.message || '新增失败')
    }
  })
}

const handleToggleStatus = async (row, nextStatus) => {
  const action = nextStatus === 'ACTIVE' ? '启用' : '禁用'
  try {
    await request.put(`/admin/teacher/pre-register/${row.id}`, { status: nextStatus })
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (err) {
    ElMessage.error(err?.message || `${action}失败`)
  }
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

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  align-items: center;
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
