<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>课时管理</h2>
        <p class="page-desc">管理学员课时账户，包括充值和消费记录</p>
      </div>
      <el-button type="primary" class="btn-primary" @click="showRechargeModal = true">
        <el-icon><el-icon-plus /></el-icon>
        课时充值
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
        <el-table-column prop="createTime" label="创建时间" width="140" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <div class="action-buttons">
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

    <el-dialog v-model="showRechargeModal" title="课时充值" width="420px" class="custom-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" class="modal-form">
        <el-form-item prop="studentId" label="学员">
          <el-select v-model="form.studentId" placeholder="请选择学员">
            <el-option v-for="student in students" :key="student.id" :label="student.name" :value="student.id" />
          </el-select>
        </el-form-item>
        <el-form-item prop="amount" label="充值课时">
          <el-input-number v-model="form.amount" :min="1" :max="1000" placeholder="请输入充值课时" />
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

    <el-dialog v-model="showTransactionsModal" title="课时记录" width="620px" class="custom-dialog">
      <el-table :data="transactions" class="custom-table" :row-class-name="tableRowClassName">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="transactionType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.transactionType === 'RECHARGE' ? 'success' : 'danger'" class="role-tag">
              {{ row.transactionType === 'RECHARGE' ? '充值' : '消费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hours" label="课时" width="100">
          <template #default="{ row }">
            <span :class="row.transactionType === 'RECHARGE' ? 'positive' : 'negative'">
              {{ row.transactionType === 'RECHARGE' ? '+' : '-' }}{{ row.hours }}
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getHourAccounts, rechargeHours, getHourTransactions, getStudents } from '@/api'

const tableData = ref([])
const students = ref([])
const transactions = ref([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showRechargeModal = ref(false)
const showTransactionsModal = ref(false)
const formRef = ref(null)

const form = reactive({
  studentId: '',
  amount: 1,
  reason: ''
})

const rules = {
  studentId: [{ required: true, message: '请选择学员', trigger: 'change' }],
  amount: [{ required: true, message: '请输入充值课时', trigger: 'blur' }]
}

const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex % 2 === 1) {
    return 'row-stripe'
  }
  return ''
}

const fetchData = async () => {
  try {
    const res = await getHourAccounts({ keyword: searchForm.keyword, pageNum: pagination.page, pageSize: pagination.size })
    tableData.value = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
    pagination.total = typeof res?.total === 'number' ? res.total : tableData.value.length
  } catch (err) {
    console.error('[fetchData]', err)
    tableData.value = []
    pagination.total = 0
  }
}

const fetchStudents = async () => {
  try {
    const res = await getStudents()
    students.value = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
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

const handleRecharge = (row) => {
  form.studentId = row.studentId != null ? row.studentId : row.id
  form.amount = 1
  form.reason = ''
  showRechargeModal.value = true
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
      await rechargeHours(payload)
      ElMessage.success('充值成功')
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

.btn-view {
  border-radius: 4px;
  margin-right: 8px;
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
