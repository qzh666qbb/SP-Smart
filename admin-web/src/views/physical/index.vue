<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>体测管理</h2>
        <p class="page-desc">管理学员体测记录，包括测试数据和评估结果</p>
      </div>
      <el-button type="primary" class="btn-primary" @click="showAddModal = true">
        <el-icon><el-icon-plus /></el-icon>
        新增体测记录
      </el-button>
    </div>
    
    <div class="page-card">
      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="搜索学员姓名" class="search-input" @keyup.enter="handleSearch" />
        <el-date-picker v-model="searchForm.date" type="date" placeholder="选择日期" class="search-date" />
        <el-button type="primary" class="btn-search" @click="handleSearch">搜索</el-button>
        <el-button class="btn-reset" @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="tableData" class="custom-table" :row-class-name="tableRowClassName" style="width: 100%;">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="studentName" label="学员姓名" min-width="100" />
        <el-table-column prop="parentName" label="家长姓名" min-width="90" />
        <el-table-column prop="parentPhone" label="家长电话" width="130" />
        <el-table-column prop="testDate" label="测试日期" width="110" />
        <el-table-column prop="totalScore" label="总分" width="70">
          <template #default="{ row }">
            <span class="score-value">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="测试老师" min-width="90" />
        <el-table-column prop="createTime" label="创建时间" width="140" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" class="btn-view" @click="handleView(row)">查看报告</el-button>
              <el-button type="warning" size="small" class="btn-edit" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="showAddModal" :title="isEdit ? '编辑体测记录' : '新增体测记录'" width="520px" class="custom-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" class="modal-form">
        <el-form-item prop="studentId" label="学员">
          <el-select v-model="form.studentId" placeholder="请选择学员">
            <el-option v-for="student in students" :key="student.id" :label="student.name" :value="student.id" />
          </el-select>
        </el-form-item>
        <el-form-item prop="testDate" label="测试日期">
          <el-date-picker v-model="form.testDate" type="date" placeholder="请选择测试日期" />
        </el-form-item>
        <el-form-item prop="teacherName" label="测试老师">
          <el-input v-model="form.teacherName" placeholder="请输入测试老师" />
        </el-form-item>
        <el-form-item prop="totalScore" label="总分">
          <el-input-number v-model="form.totalScore" :min="0" :max="100" placeholder="请输入总分" />
        </el-form-item>
        <el-form-item prop="evaluation" label="评估结果">
          <el-input v-model="form.evaluation" type="textarea" placeholder="请输入评估结果" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showReportModal" title="体测报告" width="620px" class="custom-dialog">
      <div v-if="selectedRecord" class="report-content">
        <div class="report-header">
          <div class="report-title">体测报告</div>
          <div class="report-date">{{ selectedRecord.testDate }}</div>
        </div>
        <div class="report-body">
          <div class="report-info">
            <div class="info-item">
              <span class="info-label">学员姓名</span>
              <span class="info-value">{{ selectedRecord.studentName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">测试老师</span>
              <span class="info-value">{{ selectedRecord.teacherName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">综合得分</span>
              <span class="info-value score">{{ selectedRecord.totalScore }}</span>
            </div>
          </div>
          <div class="report-scores">
            <div class="section-title">各项指标</div>
            <div v-if="selectedRecord.scores" class="scores-list">
              <div v-for="(score, index) in selectedRecord.scores" :key="index" class="score-item">
                <span class="score-name">{{ score.itemName }}</span>
                <span class="score-value">{{ score.score }}</span>
                <span class="score-unit">{{ score.unit }}</span>
              </div>
            </div>
          </div>
          <div class="report-evaluation">
            <div class="section-title">评估结果</div>
            <div class="evaluation-text">{{ selectedRecord.evaluation }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showReportModal = false">关闭</el-button>
        <el-button type="primary" @click="handleDownload">下载报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getPhysicalTests, createPhysicalTest, updatePhysicalTest, deletePhysicalTest, getStudents } from '@/api'

const tableData = ref([])
const students = ref([])
const searchForm = reactive({ keyword: '', date: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showAddModal = ref(false)
const showReportModal = ref(false)
const selectedRecord = ref(null)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: '',
  studentId: '',
  testDate: '',
  teacherName: '',
  totalScore: 0,
  evaluation: '',
  scores: []
})

const rules = {
  studentId: [{ required: true, message: '请选择学员', trigger: 'change' }],
  testDate: [{ required: true, message: '请选择测试日期', trigger: 'change' }],
  totalScore: [{ required: true, message: '请输入总分', trigger: 'blur' }]
}

const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex % 2 === 1) {
    return 'row-stripe'
  }
  return ''
}

const fetchData = async () => {
  try {
    const res = await getPhysicalTests({ ...searchForm, ...pagination })
    tableData.value = res.list || res
    pagination.total = res.total || tableData.value.length
  } catch (err) {
    console.error(err)
  }
}

const fetchStudents = async () => {
  try {
    const res = await getStudents()
    students.value = res.list || res
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

const handleView = (row) => {
  selectedRecord.value = row
  showReportModal.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  showAddModal.value = true
}

const handleDelete = (row) => {
  if (!confirm(`确定删除体测记录 ?`)) return
  deletePhysicalTest(row.id).then(() => {
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
        await updatePhysicalTest(form.id, form)
        alert('更新成功')
      } else {
        await createPhysicalTest(form)
        alert('创建成功')
      }
      showAddModal.value = false
      fetchData()
    } catch (err) {
      alert('操作失败')
    }
  })
}

const handleDownload = () => {
  alert('报告下载中...')
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

.score-value {
  font-weight: 600;
  color: #2563eb;
  font-size: 16px;
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
  margin-right: 8px;
  padding: 4px 12px;
  transition: all 0.2s ease;
}

.btn-edit:hover {
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
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
.modal-form :deep(.el-date-picker__wrapper) {
  border-radius: 6px;
  border-color: #e5e7eb;
}

.report-content {
  padding: 8px 0;
}

.report-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f3f4f6;
}

.report-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.report-date {
  font-size: 14px;
  color: #6b7280;
  margin-top: 8px;
}

.report-body {
  padding-top: 4px;
}

.report-info {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-label {
  color: #6b7280;
}

.info-value {
  font-weight: 500;
}

.info-value.score {
  color: #2563eb;
  font-size: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 14px;
  color: #111827;
}

.scores-list {
  margin-bottom: 20px;
}

.score-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.score-name {
  flex: 2;
}

.score-value {
  flex: 1;
  text-align: right;
  font-weight: 500;
}

.score-unit {
  flex: 1;
  text-align: right;
  margin-left: 10px;
  color: #6b7280;
}

.report-evaluation {
  margin-top: 4px;
}

.evaluation-text {
  padding: 16px;
  background: #f9fafb;
  border-radius: 6px;
  line-height: 1.8;
  color: #374151;
}
</style>
