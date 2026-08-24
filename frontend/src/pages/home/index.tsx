import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Lesson, ParentProfile } from '@/types';
import { getSchedules } from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import { notifyTabBarActive } from '@/utils/tabbar';
import styles from './index.module.scss';

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
  hasCourse: boolean;
  courseCount: number;
}

export default function HomePage() {
  Taro.useDidShow(() => notifyTabBarActive('/pages/home/index'));
  const [schedules, setSchedules] = useState<Lesson[]>([]);
  const [selectedDate, setSelectedDate] = useState(formatDate(new Date()));
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [isLoading, setIsLoading] = useState(true);
  const { user, profile } = useAuthStore();
  const isParent = user?.role === 'PARENT';

  useEffect(() => {
    fetchSchedules();
  }, []);

  const fetchSchedules = async () => {
    setIsLoading(true);
    try {
      const data = await getSchedules();
      setSchedules(data);
    } catch (err) {
      console.error('[HomePage]', err);
    } finally {
      setIsLoading(false);
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
      days.push({ date, isCurrentMonth: false, isToday: false, isSelected: false, hasCourse: false, courseCount: 0 });
    }

    for (let i = 1; i <= lastDay.getDate(); i++) {
      const date = new Date(year, month, i);
      const today = new Date();
      const isToday = date.toDateString() === today.toDateString();
      const dateStr = formatDate(date);
      const isSelected = dateStr === selectedDate;
      const dayCourses = getCoursesForDate(dateStr);
      days.push({ 
        date, 
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
      days.push({ date, isCurrentMonth: false, isToday: false, isSelected: false, hasCourse: false, courseCount: 0 });
    }

    return days;
  };

  const handleDateClick = (date: Date) => {
    setSelectedDate(formatDate(date));
  };

  const handlePrevMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1));
  };

  const handleNextMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1));
  };

  const handleCourseClick = (lesson: Lesson) => {
    Taro.navigateTo({ url: `/pages/course-detail/index?id=${lesson.id}` });
  };

  const selectedCourses = getCoursesForDate(selectedDate);

  return (
    <ScrollView className={styles.homePage} scrollY>
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
          {generateCalendarDays().map((item, index) => (
            <View 
              key={index}
              className={`${styles.day} ${!item.isCurrentMonth ? styles.otherMonth : ''} ${item.isToday ? styles.today : ''} ${item.isSelected ? styles.selected : ''}`}
              onClick={() => item.isCurrentMonth && handleDateClick(item.date)}
            >
              <Text className={styles.dayText}>{item.date.getDate()}</Text>
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
                <Text className={styles.courseName}>体育课</Text>
                {isParent ? (
                  <Text className={styles.courseInfo}>授课老师: 张老师</Text>
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
    </ScrollView>
  );
}
