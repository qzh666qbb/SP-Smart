<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="logo">
        <div class="logo-icon-wrapper">
          <el-icon size="28"><el-icon-s-home /></el-icon>
        </div>
        <span class="logo-text">体测管理系统</span>
      </div>
      <el-menu :default-active="activeMenu" class="menu" mode="vertical" @select="handleMenuSelect">
        <el-menu-item index="/dashboard">
          <el-icon><el-icon-s-home /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><el-icon-user /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/students">
          <el-icon><el-icon-user-solid /></el-icon>
          <span>学员管理</span>
        </el-menu-item>
        <el-menu-item index="/courses">
          <el-icon><el-icon-book /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/schedules">
          <el-icon><el-icon-calendar /></el-icon>
          <span>排课管理</span>
        </el-menu-item>
        <el-menu-item index="/physical">
          <el-icon><el-icon-data-analysis /></el-icon>
          <span>体测管理</span>
        </el-menu-item>
        <el-menu-item index="/hours">
          <el-icon><el-icon-clock /></el-icon>
          <span>课时管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon class="user-icon"><el-icon-user /></el-icon>
              <span>{{ userInfo.name }}</span>
              <el-icon class="arrow"><el-icon-arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCurrentUser } from '@/api'

const route = useRoute()
const router = useRouter()

const userInfo = ref({ name: '管理员' })
const sidebarWidth = '220px'

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => {
  const titleMap = {
    '/dashboard': '数据概览',
    '/users': '用户管理',
    '/students': '学员管理',
    '/courses': '课程管理',
    '/schedules': '排课管理',
    '/physical': '体测管理',
    '/hours': '课时管理'
  }
  return titleMap[route.path] || ''
})

const handleMenuSelect = (key) => {
  router.push(key)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  }
}

onMounted(() => {
  getCurrentUser().then(res => {
    if (res.profile) {
      userInfo.value = res.profile
    }
  }).catch(() => {
    localStorage.removeItem('token')
    router.push('/login')
  })
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #1a1f2e;
  color: #fff;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  padding: 20px 20px;
  background: #1a1f2e;
}

.logo-icon-wrapper {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.logo-icon-wrapper :deep(.el-icon) {
  color: #fff;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.3px;
}

.menu {
  border-right: none;
  background: #1a1f2e;
}

.menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.55);
  font-size: 14px;
  transition: all 0.2s ease;
  background: transparent;
  position: relative;
  padding-left: 16px;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.9);
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(37, 99, 235, 0.1);
  color: #60a5fa;
}

.menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #2563eb;
  border-radius: 0 2px 2px 0;
}

.menu :deep(.el-menu-item i) {
  margin-right: 10px;
  font-size: 15px;
}

.header {
  background: #fff;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  font-size: 14px;
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #111827;
  font-weight: 500;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: #6b7280;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 20px;
  transition: all 0.2s ease;
}

.user-info:hover {
  background: #f3f4f6;
}

.user-icon {
  margin-right: 8px;
  color: #6b7280;
  font-size: 18px;
}

.user-info span {
  color: #111827;
  font-size: 14px;
}

.arrow {
  margin-left: 4px;
  color: #9ca3af;
  font-size: 12px;
}

.main {
  padding: 24px;
  background: #f3f4f6;
}
</style>
