<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>学员管理</h2>
        <p class="page-desc">管理学员信息，包括基本资料和家长联系方式</p>
      </div>
      <el-button type="primary" class="btn-primary" @click="handleAdd">
        <el-icon><el-icon-plus /></el-icon>
        新增学员
      </el-button>
    </div>
    
    <div class="page-card">
      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="搜索姓名/学号" class="search-input" @keyup.enter="handleSearch" />
        <el-button type="primary" class="btn-search" @click="handleSearch">搜索</el-button>
        <el-button class="btn-reset" @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="tableData" class="custom-table" :row-class-name="tableRowClassName" style="width: 100%;">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="name" label="姓名" min-width="80" />
        <el-table-column prop="studentNo" label="学号" width="100" />
        <el-table-column prop="birthDate" label="出生日期" width="110" />
        <el-table-column label="年龄" width="60">
          <template #default="{ row }">
            <span v-if="row.birthDate">{{ calcAge(row.birthDate) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">
            <el-tag :type="row.gender === 'MALE' ? 'primary' : (row.gender === 'FEMALE' ? 'success' : 'info')" class="role-tag">
              {{ row.gender === 'MALE' ? '男' : (row.gender === 'FEMALE' ? '女' : '未知') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentName" label="家长姓名" min-width="90" />
        <el-table-column prop="parentPhone" label="家长电话" width="130" />
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

    <el-dialog v-model="showAddModal" :title="isEdit ? '编辑学员' : '新增学员'" width="460px" class="custom-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" class="modal-form" label-width="100px">
        <el-form-item prop="name" label="姓名">
          <el-input v-model="form.name" placeholder="请输入学员姓名" />
        </el-form-item>
        <el-form-item prop="studentNo" label="学号">
          <el-input v-model="form.studentNo" placeholder="请输入学员编号" />
        </el-form-item>
        <el-form-item prop="gender" label="性别">
          <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="未知" value="UNKNOWN" />
          </el-select>
        </el-form-item>
        <el-form-item prop="birthDate" label="出生日期">
          <el-date-picker v-model="form.birthDate" type="date" placeholder="请选择出生日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="phone" label="学员联系电话">
          <el-input v-model="form.phone" placeholder="学员本人电话（可为空）" maxlength="11" />
        </el-form-item>
        <el-form-item prop="parentName" label="家长姓名">
          <el-input v-model="form.parentName" placeholder="请输入家长姓名" />
        </el-form-item>
        <el-form-item prop="parentPhone" label="家长电话">
          <el-input v-model="form.parentPhone" placeholder="11位家长联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item prop="parentRelation" label="家长关系">
          <el-select v-model="form.parentRelation" placeholder="请选择家长关系" style="width: 100%" clearable>
            <el-option label="父亲" value="FATHER" />
            <el-option label="母亲" value="MOTHER" />
            <el-option label="监护人" value="GUARDIAN" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item prop="enrollmentDate" label="报名日期">
          <el-date-picker v-model="form.enrollmentDate" type="date" placeholder="请选择报名日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="remark" label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="备注信息" :rows="2" />
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
import { getStudents, createStudent, updateStudent, deleteStudent } from '@/api'

const tableData = ref([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showAddModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: '',
  name: '',
  studentNo: '',
  gender: 'MALE',
  birthDate: '',
  phone: '',
  parentName: '',
  parentPhone: '',
  parentRelation: 'MOTHER',
  primaryTeacherId: null,
  enrollmentDate: '',
  medicalNotes: '',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

const tableRowClassName = ({ rowIndex }) => (rowIndex % 2 === 1 ? 'row-stripe' : '')

const calcAge = (birth) => {
  if (!birth) return ''
  const d = typeof birth === 'string' ? birth : String(birth)
  const [y, m, day] = d.split('-').map(Number)
  if (!y) return ''
  const now = new Date()
  let age = now.getFullYear() - y
  const curM = now.getMonth() + 1
  if (curM < m || (curM === m && now.getDate() < day)) age -= 1
  return Math.max(0, age)
}

const fetchData = async () => {
  try {
    const res = await getStudents({ ...searchForm, ...pagination })
    tableData.value = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
    pagination.total = typeof res?.total === 'number' ? res.total : tableData.value.length
  } catch (err) {
    console.error(err)
    tableData.value = []
    pagination.total = 0
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const resetSearch = () => { searchForm.keyword = ''; pagination.page = 1; fetchData() }
const handleSizeChange = (size) => { pagination.size = size; fetchData() }
const handleCurrentChange = (page) => { pagination.page = page; fetchData() }

const resetForm = () => {
  form.id = ''
  form.name = ''
  form.studentNo = ''
  form.gender = 'MALE'
  form.birthDate = ''
  form.phone = ''
  form.parentName = ''
  form.parentPhone = ''
  form.parentRelation = 'MOTHER'
  form.primaryTeacherId = null
  form.enrollmentDate = ''
  form.medicalNotes = ''
  form.remark = ''
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
  showAddModal.value = true
}

const handleDelete = async (row) => {
  const target = row?.name ? `学员 ${row.name}` : '该学员'
  try {
    await ElMessageBox.confirm(`确定删除${target}？删除后学员档案、课时账户及关联排课记录均会解除绑定。`, '删除确认', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', distinguishCancelAndClose: true
    })
  } catch (_) { return }
  try {
    await deleteStudent(row.id)
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
        name: form.name,
        studentNo: form.studentNo,
        gender: form.gender || 'UNKNOWN',
        birthDate: form.birthDate || null,
        phone: form.phone || null,
        parentName: form.parentName || null,
        parentPhone: form.parentPhone || null,
        parentRelation: form.parentRelation || null,
        primaryTeacherId: form.primaryTeacherId ? Number(form.primaryTeacherId) : null,
        enrollmentDate: form.enrollmentDate || null,
        medicalNotes: form.medicalNotes || null,
        remark: form.remark || null
      }
      if (isEdit.value) {
        await updateStudent(form.id, payload)
        ElMessage.success('更新成功')
      } else {
        await createStudent(payload)
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
.modal-form :deep(.el-select__wrapper) {
  border-radius: 6px;
  border-color: #e5e7eb;
}
</style>
