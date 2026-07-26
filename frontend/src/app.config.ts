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
    'pages/student-detail/index',
    'pages/settings/index',
    'pages/parent-home/index',
    'pages/parent-course/index',
    'pages/parent-physical/index'
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#FFFFFF',
    navigationBarTitleText: '排课与体测管理',
    navigationBarTextStyle: 'black',
    backgroundColor: '#F8FAFC'
  },
  tabBar: {
    color: '#9CA3AF',
    selectedColor: '#3B82F6',
    backgroundColor: '#FFFFFF',
    borderStyle: 'white',
    list: [
      {
        pagePath: 'pages/home/index',
        text: '首页',
        iconPath: 'assets/tabbar/home.svg',
        selectedIconPath: 'assets/tabbar/home-selected.svg'
      },
      {
        pagePath: 'pages/schedule/index',
        text: '课程',
        iconPath: 'assets/tabbar/schedule.svg',
        selectedIconPath: 'assets/tabbar/schedule-selected.svg'
      },
      {
        pagePath: 'pages/physical/index',
        text: '体测',
        iconPath: 'assets/tabbar/physical.svg',
        selectedIconPath: 'assets/tabbar/physical-selected.svg'
      },
      {
        pagePath: 'pages/mine/index',
        text: '我的',
        iconPath: 'assets/tabbar/mine.svg',
        selectedIconPath: 'assets/tabbar/mine-selected.svg'
      }
    ]
  }
})
