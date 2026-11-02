<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>课时管理</h2>
        <p class="page-desc">管理学员课时账户，包括充值和消费记录</p>
      </div>
      <el-button type="primary" class="btn-primary" @click="handleAddHours()">
        <el-icon><el-icon-plus /></el-icon>
        添加学员课时
      </el-button>
    </div>
    
    <div class="page-card">
      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="搜索学员姓名" class="search-input" @keyup.enter="handleSearch" />
        <el-button type="primary" class="btn-search" @click="handleSearch">搜索</el-button>
        <el-button class="btn-reset" @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="tableData" class="custom-table" :row-class-name="tableRowClassName" style="width: 100%;">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="studentName" label="学员姓名" min-width="100" />
        <el-table-column prop="parentName" label="家长姓名" min-width="90" />
        <el-table-column prop="parentPhone" label="家长电话" width="130" />
        <el-table-column prop="studentNo" label="学号" width="90" />
        <el-table-column prop="totalHours" label="总课时" width="70" />
        <el-table-column prop="usedHours" label="已用课时" width="70" />
        <el-table-column prop="remainingHours" label="剩余课时" width="90">
          <template #default="{ row }">
            <el-tag :type="row.remainingHours < 5 ? 'danger' : 'success'" class="role-tag">
              {{ row.remainingHours }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="success" size="small" @click="handleAddHours(row)">添加课时</el-button>
              <el-button type="danger" plain size="small" @click="handleDeductHours(row)">减少课时</el-button>
              <el-button type="warning" size="small" @click="handleManualAttendance(row)">补签消课</el-button>
              <el-button type="primary" size="small" class="btn-view" @click="handleViewTransactions(row)">查看记录</el-button>
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

    <el-dialog v-model="showRechargeModal" title="手动添加学员课时" width="420px" class="custom-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" class="modal-form">
        <el-form-item prop="studentId" label="学员">
          <el-select v-model="form.studentId" placeholder="请选择学员">
            <el-option v-for="student in students" :key="student.id" :label="student.name" :value="student.id" />
          </el-select>
        </el-form-item>
        <el-form-item prop="amount" label="增加课时">
          <el-input-number v-model="form.amount" :min="0.5" :max="1000" :step="0.5" :precision="1" placeholder="请输入增加课时" />
        </el-form-item>
        <el-form-item prop="reason" label="备注">
          <el-input v-model="form.reason" type="textarea" placeholder="请输入备注" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDeductModal" title="减少学员课时" width="420px" class="custom-dialog">
      <el-form :model="deductForm" :rules="deductRules" ref="deductFormRef" class="modal-form">
        <el-form-item label="学员">
          <el-input :model-value="deductStudentName" disabled />
        </el-form-item>
        <el-form-item label="当前剩余">
          <el-input :model-value="`${deductCurrentHours} 课时`" disabled />
        </el-form-item>
        <el-form-item prop="amount" label="减少课时">
          <el-input-number v-model="deductForm.amount" :min="0.5" :max="deductMaxHours" :step="0.5" :precision="1" />
        </el-form-item>
        <el-form-item prop="reason" label="更正原因">
          <el-input v-model="deductForm.reason" type="textarea" placeholder="例如：手动添加课时输入错误" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeductModal = false">取消</el-button>
        <el-button type="danger" @click="submitDeductHours">确认减少</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAttendanceModal" title="管理员补签并消课" width="480px" class="custom-dialog">
      <el-alert title="补签成功后将立即按该课程设置扣减课时，不会在结课时重复扣减。" type="warning" :closable="false" show-icon />
      <el-form :model="attendanceForm" :rules="attendanceRules" ref="attendanceFormRef" class="modal-form attendance-form">
        <el-form-item label="学员">
          <el-input :model-value="attendanceStudentName" disabled />
        </el-form-item>
        <el-form-item prop="date" label="上课日期">
          <el-date-picker v-model="attendanceForm.date" type="date" value-format="YYYY-MM-DD" style="width: 100%" @change="fetchAttendanceLessons" />
        </el-form-item>
        <el-form-item prop="lessonId" label="课程">
          <el-select v-model="attendanceForm.lessonId" filterable placeholder="请选择需要补签的课程" style="width: 100%">
            <el-option v-for="lesson in attendanceLessons" :key="lesson.id" :value="lesson.id"
              :label="`${lesson.startTime || ''} ${lesson.courseName || lesson.lessonNo || '课程'}（${lesson.consumeHours || 0}课时）`" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="attendanceForm.remark" type="textarea" :rows="2" placeholder="请输入补签原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAttendanceModal = false">取消</el-button>
        <el-button type="primary" @click="submitManualAttendance">确认补签并消课</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTransactionsModal" title="课时记录" width="620px" class="custom-dialog">
      <el-table :data="transactions" class="custom-table" :row-class-name="tableRowClassName">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="transactionType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="['RECHARGE', 'ADJUST', 'REFUND'].includes(row.transactionType) ? 'success' : 'danger'" class="role-tag">
              {{ transactionTypeText(row.transactionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hours" label="课时" width="100">
          <template #default="{ row }">
            <span :class="isPositiveTransaction(row) ? 'positive' : 'negative'">
              {{ isPositiveTransaction(row) ? '+' : '-' }}{{ row.hours }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="100" />
        <el-table-column prop="remark" label="备注" width="150" />
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHourAccounts, adjustHours, refundHours, getHourTransactions, getAllStudents, getLessons, recordAttendance } from '@/api'

const tableData = ref([])
const students = ref([])
const transactions = ref([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showRechargeModal = ref(false)
const showDeductModal = ref(false)
const showTransactionsModal = ref(false)
const showAttendanceModal = ref(false)
const formRef = ref(null)
const deductFormRef = ref(null)
const attendanceFormRef = ref(null)
const attendanceLessons = ref([])
const attendanceStudentName = ref('')
const deductStudentName = ref('')
const deductCurrentHours = ref(0)
const deductPaidHours = ref(0)

const form = reactive({
  studentId: '',
  amount: 1,
  reason: ''
})

const rules = {
  studentId: [{ required: true, message: '请选择学员', trigger: 'change' }],
  amount: [{ required: true, message: '请输入充值课时', trigger: 'blur' }]
}

const deductForm = reactive({ studentId: '', amount: 0.5, reason: '' })
const deductMaxHours = computed(() => Math.max(0.5, Number(deductPaidHours.value || 0)))
const deductRules = {
  amount: [{ required: true, message: '请输入减少课时', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入更正原因', trigger: 'blur' }]
}

const localDate = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const attendanceForm = reactive({ studentId: '', date: localDate(), lessonId: '', remark: '' })
const attendanceRules = {
  date: [{ required: true, message: '请选择上课日期', trigger: 'change' }],
  lessonId: [{ required: true, message: '请选择课程', trigger: 'change' }]
}
const transactionTypeText = type => ({ RECHARGE: '充值', ADJUST: '手动添加', CONSUME: '消费', CONSUMPTION: '消费', REFUND: '退回' }[type] || type)
const isPositiveTransaction = row => row.changeAmount != null
  ? Number(row.changeAmount) >= 0
  : ['RECHARGE', 'ADJUST', 'REFUND'].includes(row.transactionType)

const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex % 2 === 1) {
    return 'row-stripe'
  }
  return ''
}

let fetchRequestId = 0
const fetchData = async () => {
  const requestId = ++fetchRequestId
  try {
    const res = await getHourAccounts({ keyword: searchForm.keyword, pageNum: pagination.page, pageSize: pagination.size })
    if (requestId !== fetchRequestId) return
    tableData.value = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
    pagination.total = typeof res?.total === 'number' ? res.total : tableData.value.length
  } catch (err) {
    console.error('[fetchData]', err)
    if (requestId !== fetchRequestId) return
    tableData.value = []
    pagination.total = 0
  }
}

const fetchStudents = async () => {
  try {
    const list = await getAllStudents()
    students.value = list.sort((a, b) => String(a.name || '').localeCompare(String(b.name || ''), 'zh-CN'))
  } catch (err) {
    console.error('[fetchStudents]', err)
    students.value = []
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

const handleViewTransactions = async (row) => {
  try {
    const sid = row.studentId != null ? String(row.studentId) : String(row.id)
    const res = await getHourTransactions(sid)
    transactions.value = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
    showTransactionsModal.value = true
  } catch (err) {
    console.error('[handleViewTransactions]', err)
    transactions.value = []
    ElMessage.error(err?.message || '加载课时记录失败')
  }
}

const handleAddHours = (row) => {
  form.studentId = row ? (row.studentId != null ? row.studentId : row.id) : ''
  form.amount = 1
  form.reason = ''
  showRechargeModal.value = true
}

const handleDeductHours = row => {
  const paidHours = Number(row.paidHours || 0)
  if (paidHours < 0.5) {
    ElMessage.warning('该学员没有可减少的付费课时')
    return
  }
  deductForm.studentId = row.studentId != null ? row.studentId : row.id
  deductForm.amount = 0.5
  deductForm.reason = ''
  deductStudentName.value = row.studentName || ''
  deductCurrentHours.value = Number(row.remainingHours || 0)
  deductPaidHours.value = paidHours
  showDeductModal.value = true
}

const submitDeductHours = async () => {
  if (!deductFormRef.value) return
  await deductFormRef.value.validate(async valid => {
    if (!valid) return
    const amount = Number(deductForm.amount)
    if (amount > deductPaidHours.value) {
      ElMessage.error(`最多可减少 ${deductPaidHours.value} 课时`)
      return
    }
    try {
      await ElMessageBox.confirm(
        `确认将${deductStudentName.value}的课时减少 ${amount} 节？操作后会生成更正记录。`,
        '确认减少课时',
        { type: 'warning', confirmButtonText: '确认减少', cancelButtonText: '取消' }
      )
      await refundHours({
        studentId: Number(deductForm.studentId),
        amount,
        reason: deductForm.reason
      })
      showDeductModal.value = false
      fetchData()
    } catch (err) {
      if (err !== 'cancel' && err !== 'close') {
        ElMessage.error(err?.message || '课时减少失败')
      }
    }
  })
}

const fetchAttendanceLessons = async () => {
  attendanceForm.lessonId = ''
  if (!attendanceForm.date) return
  try {
    const res = await getLessons({ date: attendanceForm.date })
    attendanceLessons.value = (Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : []))
      .filter(lesson => lesson.status !== 'CANCELLED')
      .sort((a, b) => String(a.startTime || '').localeCompare(String(b.startTime || '')))
  } catch (err) {
    attendanceLessons.value = []
    ElMessage.error(err?.message || '加载课程失败')
  }
}

const handleManualAttendance = row => {
  attendanceForm.studentId = row.studentId != null ? row.studentId : row.id
  attendanceStudentName.value = row.studentName || ''
  attendanceForm.date = localDate()
  attendanceForm.lessonId = ''
  attendanceForm.remark = ''
  showAttendanceModal.value = true
  fetchAttendanceLessons()
}

const submitManualAttendance = async () => {
  if (!attendanceFormRef.value) return
  await attendanceFormRef.value.validate(async valid => {
    if (!valid) return
    try {
      await recordAttendance({
        lessonId: Number(attendanceForm.lessonId),
        studentId: Number(attendanceForm.studentId),
        attendanceStatus: 'PRESENT',
        remark: attendanceForm.remark || '管理员手动补签'
      })
      ElMessage.success('补签并消课成功')
      showAttendanceModal.value = false
      fetchData()
    } catch (err) {
      ElMessage.error(err?.message || '补签消课失败')
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const payload = {
        studentId: typeof form.studentId === 'string' ? Number(form.studentId) : form.studentId,
        amount: Number(form.amount),
        reason: form.reason,
        hourSource: 'PAID'
      }
      await adjustHours(payload)
      ElMessage.success('课时添加成功')
      showRechargeModal.value = false
      fetchData()
    } catch (err) {
      ElMessage.error(err?.message || '操作失败')
    }
  })
}

fetchData()
fetchStudents()
</script>

<style scoped>
.page-container {
  padding: 0;
}

.attendance-form {
  margin-top: 18px;
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

.action-buttons {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 6px;
  white-space: nowrap;
}

.action-buttons :deep(.el-button + .el-button) {
  margin-left: 0;
}

.btn-view {
  border-radius: 4px;
  padding: 4px 12px;
  transition: all 0.2s ease;
}

.btn-view:hover {
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
}

.btn-edit {
  border-radius: 4px;
  padding: 4px 12px;
  transition: all 0.2s ease;
}

.btn-edit:hover {
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
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

.positive {
  color: #10b981;
  font-weight: 500;
}

.negative {
  color: #ef4444;
  font-weight: 500;
}
</style>
