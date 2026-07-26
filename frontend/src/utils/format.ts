import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

dayjs.locale('zh-cn');

export const formatDate = (date: string | Date, format: string = 'YYYY-MM-DD') => {
  return dayjs(date).format(format);
};

export const formatDateTime = (date: string | Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm');
};

export const formatTime = (date: string | Date) => {
  return dayjs(date).format('HH:mm');
};

export const formatMonth = (date: string | Date) => {
  return dayjs(date).format('YYYY年MM月');
};

export const formatWeekDay = (date: string | Date) => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  return weekDays[dayjs(date).day()];
};

export const formatDuration = (minutes: number) => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`;
  } else if (hours > 0) {
    return `${hours}小时`;
  } else {
    return `${mins}分钟`;
  }
};

export const formatHours = (hours: number) => {
  return hours.toFixed(1);
};

export const formatScore = (score: number) => {
  return score.toFixed(1);
};

export const getLevelText = (level: string) => {
  const levelMap: Record<string, string> = {
    EXCELLENT: '优秀',
    GOOD: '良好',
    PASS: '及格',
    FAIL: '不及格'
  };
  return levelMap[level] || level;
};

export const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    ENABLED: '启用',
    DISABLED: '禁用',
    SCHEDULED: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    DRAFT: '草稿',
    CONFIRMED: '已确认'
  };
  return statusMap[status] || status;
};

export const getCourseTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    PHYSICAL: '体育',
    DANCE: '舞蹈',
    MUSIC: '音乐',
    ART: '美术'
  };
  return typeMap[type] || type;
};

export const getAttendanceText = (status: string) => {
  const attendanceMap: Record<string, string> = {
    PRESENT: '出勤',
    ABSENT: '缺勤',
    LATE: '迟到',
    EXCUSED: '请假'
  };
  return attendanceMap[status] || status;
};

export const getTransactionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    RECHARGE: '充值',
    CONSUMPTION: '消费',
    REFUND: '退款',
    ADJUSTMENT: '调整'
  };
  return typeMap[type] || type;
};
