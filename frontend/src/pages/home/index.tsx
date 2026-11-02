import { useState, useEffect, useRef } from 'react';
import { View, Text } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Lesson, ParentProfile } from '@/types';
import { getSchedules } from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import { notifyTabBarActive } from '@/utils/tabbar';
import BottomNav from '@/components/BottomNav';
import styles from './index.module.scss';

interface CalendarDay {
  dateStr: string;
  dayNumber: number;
  year: number;
  month: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
  hasCourse: boolean;
  courseCount: number;
}

export default function HomePage() {
  Taro.useDidShow(() => {
    notifyTabBarActive('/pages/home/index');
    fetchSchedules(currentMonth);
  });
  const [schedules, setSchedules] = useState<Lesson[]>([]);
  const [selectedDate, setSelectedDate] = useState(formatDate(new Date()));
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(false);
  const scheduleRequestId = useRef(0);
  const { user, profile } = useAuthStore();
  const isParent = user?.role === 'PARENT';

  useEffect(() => {
    fetchSchedules(currentMonth);
  }, [currentMonth]);

  const fetchSchedules = async (month: Date) => {
    const requestId = ++scheduleRequestId.current;
    setIsLoading(true);
    setLoadError(false);
    setSchedules([]);
    try {
      const year = month.getFullYear();
      const monthIndex = month.getMonth();
      const start = new Date(year, monthIndex, 1);
      start.setDate(start.getDate() - start.getDay());
      const end = new Date(start);
      end.setDate(start.getDate() + 41);
      const data = await getSchedules({
        startDate: formatDate(start),
        endDate: formatDate(end),
      });
      if (requestId === scheduleRequestId.current) setSchedules(data);
    } catch (err) {
      console.error('[HomePage]', err);
      if (requestId === scheduleRequestId.current) setLoadError(true);
    } finally {
      if (requestId === scheduleRequestId.current) setIsLoading(false);
    }
  };

  const getCoursesForDate = (dateStr: string) => {
    return schedules.filter(s => s.lessonDate === dateStr);
  };

  const generateCalendarDays = () => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const days: CalendarDay[] = [];

    for (let i = firstDay.getDay(); i > 0; i--) {
      const date = new Date(year, month, -i + 1);
      const dateStr = formatDate(date);
      days.push({
        dateStr,
        dayNumber: date.getDate(),
        year: date.getFullYear(),
        month: date.getMonth(),
        isCurrentMonth: false,
        isToday: false,
        isSelected: dateStr === selectedDate,
        hasCourse: getCoursesForDate(dateStr).length > 0,
        courseCount: getCoursesForDate(dateStr).length
      });
    }

    for (let i = 1; i <= lastDay.getDate(); i++) {
      const date = new Date(year, month, i);
      const today = new Date();
      const isToday = date.toDateString() === today.toDateString();
      const dateStr = formatDate(date);
      const isSelected = dateStr === selectedDate;
      const dayCourses = getCoursesForDate(dateStr);
      days.push({ 
        dateStr,
        dayNumber: date.getDate(),
        year: date.getFullYear(),
        month: date.getMonth(),
        isCurrentMonth: true, 
        isToday, 
        isSelected, 
        hasCourse: dayCourses.length > 0,
        courseCount: dayCourses.length
      });
    }

    const remaining = 42 - days.length;
    for (let i = 1; i <= remaining; i++) {
      const date = new Date(year, month + 1, i);
      const dateStr = formatDate(date);
      days.push({
        dateStr,
        dayNumber: date.getDate(),
        year: date.getFullYear(),
        month: date.getMonth(),
        isCurrentMonth: false,
        isToday: false,
        isSelected: dateStr === selectedDate,
        hasCourse: getCoursesForDate(dateStr).length > 0,
        courseCount: getCoursesForDate(dateStr).length
      });
    }

    return days;
  };

  const handleDateClick = (item: CalendarDay) => {
    setSelectedDate(item.dateStr);
    if (!item.isCurrentMonth) {
      setCurrentMonth(new Date(item.year, item.month, 1));
    }
  };

  const handlePrevMonth = () => {
    const month = new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1);
    setCurrentMonth(month);
    setSelectedDate(formatDate(month));
  };

  const handleNextMonth = () => {
    const month = new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1);
    setCurrentMonth(month);
    setSelectedDate(formatDate(month));
  };

  const handleCourseClick = (lesson: Lesson) => {
    Taro.navigateTo({ url: `/pages/course-detail/index?id=${lesson.id}&date=${lesson.lessonDate}` });
  };

  const selectedCourses = getCoursesForDate(selectedDate);

  return (
    <View className={styles.homePage}>
      <Card onClick={() => Taro.navigateTo({ url: '/pages/schedule/index' })}>
        <Text>查看我的一周课表 ›</Text>
      </Card>
      {isParent && (
        <Card className={styles.studentCard}>
          <View className={styles.studentInfo}>
            <Text className={styles.studentName}>{(profile as ParentProfile)?.students?.[0]?.name || '孩子姓名'}</Text>
            <Text className={styles.studentNo}>学号: {(profile as ParentProfile)?.students?.[0]?.studentNo || '--'}</Text>
          </View>
          <Text className={styles.arrow}>›</Text>
        </Card>
      )}

      <Card className={styles.calendarCard}>
        <View className={styles.calendarHeader}>
          <Text className={styles.navBtn} onClick={handlePrevMonth}>‹</Text>
          <Text className={styles.monthTitle}>
            {currentMonth.getFullYear()}年{currentMonth.getMonth() + 1}月
          </Text>
          <Text className={styles.navBtn} onClick={handleNextMonth}>›</Text>
        </View>

        <View className={styles.weekDays}>
          {['日', '一', '二', '三', '四', '五', '六'].map(day => (
            <Text key={day} className={styles.weekDay}>{day}</Text>
          ))}
        </View>

        <View className={styles.calendarGrid}>
          {generateCalendarDays().map(item => (
            <View 
              key={item.dateStr}
              className={`${styles.day} ${!item.isCurrentMonth ? styles.otherMonth : ''} ${item.isToday ? styles.today : ''} ${item.isSelected ? styles.selected : ''}`}
              hoverClass={styles.dayHover}
              onClick={() => handleDateClick(item)}
            >
              <Text className={styles.dayText}>{item.dayNumber}</Text>
              {item.hasCourse && (
                <View className={styles.courseDot} />
              )}
            </View>
          ))}
        </View>
      </Card>

      <View className={styles.courseList}>
        <View className={styles.listHeader}>
          <Text className={styles.listTitle}>{selectedDate} 的课程</Text>
          <Text className={styles.listCount}>{selectedCourses.length}节</Text>
        </View>

        {isLoading ? (
          <View className={styles.loading}>
            <Text>加载中...</Text>
          </View>
        ) : loadError ? (
          <Card className={styles.emptyCard} onClick={() => fetchSchedules(currentMonth)}>
            <Text className={styles.emptyText}>课程加载失败，点击重试</Text>
          </Card>
        ) : selectedCourses.length === 0 ? (
          <Card className={styles.emptyCard}>
            <Text className={styles.emptyText}>当日暂无课程安排</Text>
          </Card>
        ) : (
          selectedCourses.map(lesson => (
            <Card 
              key={lesson.id} 
              className={styles.courseCard}
              onClick={() => handleCourseClick(lesson)}
            >
              <View className={styles.courseHeader}>
                <View className={styles.courseTime}>
                  <Text className={styles.timeText}>{lesson.startTime} - {lesson.endTime}</Text>
                </View>
                {lesson.status === 'IN_PROGRESS' && <Tag type="warning">进行中</Tag>}
                {lesson.status === 'COMPLETED' && <Tag type="success">已完成</Tag>}
                {lesson.status === 'SCHEDULED' && <Tag type="info">未开始</Tag>}
                {lesson.status === 'CANCELLED' && <Tag type="danger">已取消</Tag>}
              </View>
              <View className={styles.courseBody}>
                <Text className={styles.courseName}>{lesson.courseName || `课程 #${lesson.courseId}`}</Text>
                {isParent ? (
                  <Text className={styles.courseInfo}>授课老师: {lesson.teacherName || `教师 #${lesson.teacherId}`}</Text>
                ) : (
                  <Text className={styles.courseInfo}>课程编号: {lesson.lessonNo}</Text>
                )}
              </View>
              {lesson.cancelReason && (
                <View className={styles.cancelReason}>
                  <Text className={styles.reasonText}>取消原因: {lesson.cancelReason}</Text>
                </View>
              )}
            </Card>
          ))
        )}
      </View>
      <BottomNav />
    </View>
  );
}
