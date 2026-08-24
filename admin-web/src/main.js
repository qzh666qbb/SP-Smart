import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(ElementPlus)

for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

const LEGACY_ICON_ALIASES = {
  'el-icon-s-home': 'HomeFilled',
  'el-icon-home': 'Home',
  'el-icon-user': 'User',
  'el-icon-user-solid': 'UserFilled',
  'el-icon-book': 'Notebook',
  'el-icon-calendar': 'Calendar',
  'el-icon-data-analysis': 'DataAnalysis',
  'el-icon-clock': 'Clock',
  'el-icon-trend-chart': 'TrendCharts',
  'el-icon-trend-charts': 'TrendCharts',
  'el-icon-list': 'List',
  'el-icon-sunny': 'Sunny',
  'el-icon-sunrise': 'Sunrise',
  'el-icon-sunset': 'Sunset',
  'el-icon-data-line': 'DataLine',
  'el-icon-data-board': 'DataBoard',
  'el-icon-pie-chart': 'PieChart',
  'el-icon-dashboard': 'DataBoard',
  'el-icon-arrow-down': 'ArrowDown',
  'el-icon-arrow-up': 'ArrowUp',
  'el-icon-arrow-left': 'ArrowLeft',
  'el-icon-arrow-right': 'ArrowRight',
  'el-icon-d-arrow-left': 'DArrowLeft',
  'el-icon-d-arrow-right': 'DArrowRight',
  'el-icon-arrow-down-bold': 'ArrowDownBold',
  'el-icon-arrow-up-bold': 'ArrowUpBold',
  'el-icon-arrow-left-bold': 'ArrowLeftBold',
  'el-icon-arrow-right-bold': 'ArrowRightBold',
  'el-icon-plus': 'Plus',
  'el-icon-edit': 'Edit',
  'el-icon-delete': 'Delete',
  'el-icon-search': 'Search',
  'el-icon-setting': 'Setting',
  'el-icon-view': 'View',
  'el-icon-refresh': 'Refresh',
  'el-icon-warning': 'Warning',
  'el-icon-info': 'InfoFilled',
  'el-icon-success': 'CircleCheckFilled',
  'el-icon-upload': 'Upload',
  'el-icon-download': 'Download',
  'el-icon-menu': 'Menu',
  'el-icon-close': 'Close',
  'el-icon-check': 'Check',
  'el-icon-circle-plus': 'CirclePlus',
  'el-icon-circle-plus-outline': 'CirclePlus',
  'el-icon-s-data': 'DataLine',
  'el-icon-s-management': 'Management',
  'el-icon-s-order': 'Sort',
  'el-icon-document': 'Document',
  'el-icon-goods': 'Goods',
  'el-icon-money': 'Money',
  'el-icon-message': 'Message',
  'el-icon-date': 'Calendar',
  'el-icon-picture': 'Picture',
  'el-icon-loading': 'Loading',
  'el-icon-alarm-clock': 'AlarmClock'
}

for (const [alias, origName] of Object.entries(LEGACY_ICON_ALIASES)) {
  const component = ElementPlusIconsVue[origName]
  if (component) app.component(alias, component)
}

app.use(router)
app.mount('#app')
