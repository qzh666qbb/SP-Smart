<template>
  <div class="login-container">
    <div class="login-bg-decoration"></div>
    <div class="login-bg-decoration-2"></div>
    <div class="login-box">
      <div class="login-header">
        <div class="logo-wrapper">
          <el-icon class="logo-icon" size="36">
            <el-icon-s-home />
          </el-icon>
        </div>
        <h1 class="title">体测管理系统</h1>
        <p class="subtitle">管理员后台管理系统</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名" 
            size="large"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            size="large"
            show-password
            class="custom-input"
          />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" size="large" class="custom-select">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" @click="handleLogin" :loading="loading">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  role: 'ADMIN'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(form)
      localStorage.setItem('token', res.token)
      router.push('/dashboard')
    } catch (err) {
      console.error(err)
      alert('登录失败，请检查账号密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #e8f4fd 50%, #f1f5f9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg-decoration {
  position: absolute;
  top: -200px;
  right: -100px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.login-bg-decoration-2 {
  position: absolute;
  bottom: -150px;
  left: -150px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.06) 0%, transparent 70%);
  border-radius: 50%;
}

.login-box {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 
    0 4px 24px rgba(0, 0, 0, 0.04),
    0 8px 32px rgba(37, 99, 235, 0.06),
    0 1px 2px rgba(0, 0, 0, 0.02);
  position: relative;
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-wrapper {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.25);
}

.logo-icon {
  color: #fff;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.login-form {
  margin-top: 8px;
}

.custom-input {
  transition: all 0.2s ease;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  border-color: #e5e7eb;
  box-shadow: none;
  padding: 0 16px;
  height: 48px;
  transition: all 0.2s ease;
  background: #fafafa;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #93c5fd;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.06);
  background: #fff;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
  background: #fff;
}

.custom-input :deep(.el-input__prefix) {
  color: #9ca3af;
  font-size: 16px;
}

.custom-select :deep(.el-select__wrapper) {
  border-radius: 8px;
  border-color: #e5e7eb;
  box-shadow: none;
  padding: 0 16px;
  height: 48px;
  transition: all 0.2s ease;
  background: #fafafa;
}

.custom-select :deep(.el-select__wrapper:hover) {
  border-color: #93c5fd;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.06);
  background: #fff;
}

.custom-select :deep(.el-select__wrapper.is-focus) {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
  background: #fff;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
}

.login-btn:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.35);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}
</style>
