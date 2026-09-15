import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Lesson, ParentProfile, Student } from '@/types';
import { getLessons, getParentStudents } from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import { notifyTabBarActive } from '@/utils/tabbar';
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

export default function ParentHomePage() {
  Taro.useDidShow(() => notifyTabBarActive('/pages/parent-home/index'));
  const [schedules, setSchedules] = useState<Lesson[]>([]);
  const [selectedDate, setSelectedDate] = useState(formatDate(new Date()));
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [isLoading, setIsLoading] = useState(true);
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const { profile } = useAuthStore();
  const parentProfile = profile as ParentProfile;

  useEffect(() => {
    fetchStudents();
  }, []);

  useEffect(() => {
    if (selectedStudent) {
      fetchSchedules(selectedStudent.id);
    }
  }, [selectedStudent, selectedDate]);

  const fetchStudents = async () => {
    try {
      const students = await getParentStudents();
      setStudents(students || []);
      if (students && students.length > 0) {
        setSelectedStudent(students[0]);
      }
    } catch (err) {
      console.error('[ParentHomePage]', err);
      if (parentProfile?.students && parentProfile.students.length > 0) {
        setStudents(parentProfile.students);
        setSelectedStudent(parentProfile.students[0]);
      }
    }
  };

  const fetchSchedules = async (studentId: string) => {
    setIsLoading(true);
    try {
      const data = await getLessons({ studentId });
      setSchedules(data);
    } catch (err) {
      console.error('[ParentHomePage]', err);
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
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1));
  };

  const handleNextMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1));
  };

  const handleCourseClick = (lesson: Lesson) => {
    Taro.navigateTo({ url: `/pages/course-detail/index?id=${lesson.id}&studentId=${selectedStudent?.id}` });
  };

  const handleStudentClick = () => {
    Taro.navigateTo({ url: '/pages/students/index' });
  };

  const selectedCourses = getCoursesForDate(selectedDate);

  return (
    <ScrollView className={styles.homePage} scrollY>
      {students.length > 0 ? (
        <Card className={styles.studentCard} onClick={handleStudentClick}>
          <View className={styles.studentInfo}>
            <Text className={styles.studentName}>{selectedStudent?.name || '孩子姓名'}</Text>
            <Text className={styles.studentNo}>学号: {selectedStudent?.studentNo || '--'}</Text>
          </View>
          <Text className={styles.arrow}>›</Text>
        </Card>
      ) : (
        <Card className={styles.studentCard} onClick={handleStudentClick}>
          <View className={styles.studentInfo}>
            <Text className={styles.studentName}>绑定孩子</Text>
            <Text className={styles.studentNo}>点击绑定学生信息</Text>
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
                <Text className={styles.courseInfo}>授课老师: 张老师</Text>
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
