import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import Login from '@/views/login/index.vue'
import Dashboard from '@/views/dashboard/index.vue'
import UserManage from '@/views/user/index.vue'
import StudentManage from '@/views/student/index.vue'
import CourseManage from '@/views/course/index.vue'
import ScheduleManage from '@/views/schedule/index.vue'
import PhysicalTest from '@/views/physical/index.vue'
import HourManage from '@/views/hour/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '首页', icon: 'el-icon-s-home' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: UserManage,
        meta: { title: '教师/用户管理', icon: 'el-icon-user' }
      },
      {
        path: 'students',
        name: 'StudentManage',
        component: StudentManage,
        meta: { title: '学员管理', icon: 'el-icon-user-solid' }
      },
      {
        path: 'courses',
        name: 'CourseManage',
        component: CourseManage,
        meta: { title: '课程管理', icon: 'el-icon-book' }
      },
      {
        path: 'schedules',
        name: 'ScheduleManage',
        component: ScheduleManage,
        meta: { title: '排课管理', icon: 'el-icon-calendar' }
      },
      {
        path: 'physical',
        name: 'PhysicalTest',
        component: PhysicalTest,
        meta: { title: '体测管理', icon: 'el-icon-data-analysis' }
      },
      {
        path: 'hours',
        name: 'HourManage',
        component: HourManage,
        meta: { title: '课时管理', icon: 'el-icon-clock' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
