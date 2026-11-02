<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title-group">
        <h2>教师申请审核</h2>
        <p class="page-desc">审核教师入驻申请，可通过或驳回并填写审核意见</p>
      </div>
    </div>

    <div class="page-card">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form" @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索手机号/姓名" class="search-input" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" class="search-select" clearable>
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="btn-search" @click="handleSearch">查询</el-button>
            <el-button class="btn-reset" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" class="custom-table" :row-class-name="tableRowClassName" style="width: 100%;">
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="phone" label="手机" width="140" />
        <el-table-column prop="realName" label="姓名" min-width="110" />
        <el-table-column prop="wechatOpenid" label="微信OpenID" min-width="160">
          <template #default="{ row }">
            {{ maskOpenId(row.wechatOpenid) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column prop="reviewerId" label="审核人ID" width="100" />
        <el-table-column prop="reviewComment" label="审核备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="reviewedAt" label="审核时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip v-if="row.status !== 'PENDING'" content="已审核" placement="top">
                <span>
                  <el-button type="primary" size="small" disabled>通过</el-button>
                  <el-button type="warning" size="small" disabled style="margin-left: 8px;">驳回</el-button>
                </span>
              </el-tooltip>
              <template v-else>
                <el-button type="primary" size="small" @click="handleReview(row, true)">通过</el-button>
                <el-button type="warning" size="small" @click="handleReview(row, false)">驳回</el-button>
              </template>
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

    <el-dialog v-model="showReviewDialog" :title="reviewApprove ? '通过教师申请' : '驳回教师申请'" width="480px" class="custom-dialog">
      <el-form :model="reviewForm" class="modal-form">
        <el-form-item label="申请人">
          <span>{{ reviewTarget.realName || '-' }}（{{ reviewTarget.phone || '-' }}）</span>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入审核备注（最多 200 字）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReview">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { listTeacherApplies, reviewTeacherApply } from '@/api/teacher'

const tableData = ref([])
const searchForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const showReviewDialog = ref(false)
const reviewApprove = ref(true)
const reviewTarget = reactive({ id: '', realName: '', phone: '' })
const reviewForm = reactive({ comment: '' })

const getStatusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: 'PENDING', APPROVED: 'APPROVED', REJECTED: 'REJECTED' }
  return map[status] || status || '-'
}

const maskOpenId = (openid) => {
  if (!openid || typeof openid !== 'string') return '-'
  if (openid.length <= 8) return openid
  return openid.slice(0, 4) + '*'.repeat(openid.length - 8) + openid.slice(-4)
}

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
    if (searchForm.status) params.status = searchForm.status
    const res = await listTeacherApplies(params)
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
  searchForm.status = ''
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

const handleReview = (row, approve) => {
  reviewTarget.id = row?.id || ''
  reviewTarget.realName = row?.realName || ''
  reviewTarget.phone = row?.phone || ''
  reviewApprove.value = approve
  reviewForm.comment = ''
  showReviewDialog.value = true
}

const handleSubmitReview = async () => {
  try {
    await reviewTeacherApply(reviewTarget.id, {
      approve: reviewApprove.value,
      comment: reviewForm.comment || ''
    })
    ElMessage.success(reviewApprove.value ? '审核通过成功' : '审核驳回成功')
    showReviewDialog.value = false
    fetchData()
  } catch (err) {
    ElMessage.error(err?.message || '操作失败')
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

.search-select {
  width: 140px;
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

.search-select :deep(.el-select__wrapper) {
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
.modal-form :deep(.el-textarea__inner) {
  border-radius: 6px;
  border-color: #e5e7eb;
}
</style>
