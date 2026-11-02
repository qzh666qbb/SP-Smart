import { useState, useEffect, useRef } from 'react';
import { View, Text, Picker } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Lesson, Student } from '@/types';
import { getLessons, listParentChildren } from '@/services/api';
import { formatDate } from '@/utils/format';
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

export default function ParentHomePage() {
  Taro.useDidShow(() => {
    notifyTabBarActive('/pages/parent-home/index');
    fetchStudents();
  });
  const [schedules, setSchedules] = useState<Lesson[]>([]);
  const [selectedDate, setSelectedDate] = useState(formatDate(new Date()));
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [isLoading, setIsLoading] = useState(true);
  const [studentsError, setStudentsError] = useState(false);
  const [scheduleError, setScheduleError] = useState(false);
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const scheduleRequestId = useRef(0);

  useEffect(() => {
    fetchStudents();
  }, []);

  useEffect(() => {
    if (selectedStudent) fetchSchedules(selectedStudent.id, currentMonth);
  }, [selectedStudent, currentMonth]);

  const studentsRequestId = useRef(0);
  const fetchStudents = async () => {
    const studentRequestId = ++studentsRequestId.current;
    setIsLoading(true);
    setStudentsError(false);
    try {
      const nextStudents = await listParentChildren();
      if (studentRequestId !== studentsRequestId.current) return;
      setStudents(nextStudents || []);
      if (nextStudents && nextStudents.length > 0) {
        setSelectedStudent(nextStudents.find(s => s.id === selectedStudent?.id) || nextStudents[0]);
      } else {
        scheduleRequestId.current += 1;
        setSelectedStudent(null);
        setSchedules([]);
        setIsLoading(false);
      }
    } catch (err) {
      if (studentRequestId !== studentsRequestId.current) return;
      console.error('[ParentHomePage]', err);
      setStudentsError(true);
      setIsLoading(false);
    }
  };

  const fetchSchedules = async (studentId: string, month: Date) => {
    const requestId = ++scheduleRequestId.current;
    setIsLoading(true);
    setScheduleError(false);
    setSchedules([]);
    try {
      const firstDay = new Date(month.getFullYear(), month.getMonth(), 1);
      const start = new Date(firstDay);
      start.setDate(firstDay.getDate() - firstDay.getDay());
      const end = new Date(start);
      end.setDate(start.getDate() + 41);
      const data = await getLessons({
        studentId,
        startDate: formatDate(start),
        endDate: formatDate(end),
      });
      if (requestId === scheduleRequestId.current) setSchedules(data);
    } catch (err) {
      console.error('[ParentHomePage]', err);
      if (requestId === scheduleRequestId.current) setScheduleError(true);
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
    Taro.navigateTo({ url: `/pages/course-detail/index?id=${lesson.id}&studentId=${selectedStudent?.id}&date=${lesson.lessonDate}` });
  };

  const selectedCourses = getCoursesForDate(selectedDate);

  const handleStudentChange = (e: any) => {
    const index = Number(e.detail.value);
    const student = students[index];
    if (student) {
      setSchedules([]);
      setSelectedStudent(student);
    }
  };

  return (
    <View className={styles.homePage}>
      <Card className={styles.studentCard} onClick={() => Taro.navigateTo({ url: '/pages/schedule/index' })}>
        <View className={styles.studentInfo}>
          <Text className={styles.studentName}>本周全部课表</Text>
          <Text className={styles.studentNo}>查看各时间段课程和授课老师</Text>
        </View>
        <Text className={styles.arrow}>›</Text>
      </Card>
      {students.length > 0 ? (
        <Picker mode="selector" range={students.map((s) => s.name)} onChange={handleStudentChange}>
          <Card className={styles.studentCard}>
            <View className={styles.studentInfo}>
              <Text className={styles.studentName}>{selectedStudent?.name || '选择孩子'}</Text>
              <Text className={styles.studentNo}>学号: {selectedStudent?.studentNo || '--'}</Text>
            </View>
            <Text className={styles.arrow}>›</Text>
          </Card>
        </Picker>
      ) : !isLoading && !studentsError ? (
        <Card className={styles.studentCard} onClick={() => Taro.navigateTo({ url: '/pages/child-add-edit/index?isFirst=1' })}>
          <View className={styles.studentInfo}>
            <Text className={styles.studentName}>还没有孩子资料</Text>
            <Text className={styles.studentNo}>点击添加孩子</Text>
          </View>
          <Text className={styles.arrow}>›</Text>
        </Card>
      ) : null}
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
        ) : studentsError ? (
          <Card className={styles.emptyCard} onClick={fetchStudents}>
            <Text className={styles.emptyText}>孩子资料加载失败，点击重试</Text>
          </Card>
        ) : scheduleError ? (
          <Card className={styles.emptyCard} onClick={() => selectedStudent && fetchSchedules(selectedStudent.id, currentMonth)}>
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
                <Text className={styles.courseInfo}>授课老师: {lesson.teacherName || `教师 #${lesson.teacherId}`}</Text>
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
