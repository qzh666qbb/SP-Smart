export default defineAppConfig({
  pages: [
    'pages/login/index',
    'pages/home/index',
    'pages/schedule/index',
    'pages/physical/index',
    'pages/mine/index',
    'pages/course-detail/index',
    'pages/physical-add/index',
    'pages/hour-transaction/index',
    'pages/students/index',
    'pages/teacher-students/index',
    'pages/student-detail/index',
    'pages/settings/index',
    'pages/parent-home/index',
    'pages/parent-course/index',
    'pages/parent-physical/index',
    'pages/parent-register/index',
    'pages/parent-profile-complete/index',
    'pages/agreement/index',
    'pages/parent-children/index',
    'pages/child-add-edit/index'
  ],
  lazyCodeLoading: 'requiredComponents',
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#FFFFFF',
    navigationBarTitleText: '排课与体测管理',
    navigationBarTextStyle: 'black',
    backgroundColor: '#F8FAFC'
  }
})
