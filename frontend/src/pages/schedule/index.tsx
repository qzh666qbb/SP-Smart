import { useState, useEffect, useRef } from 'react';
import { View, Text } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Lesson } from '@/types';
import { getPublicTimetable, getSchedules } from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import styles from '../parent-course/index.module.scss';

export default function SchedulePage() {
  const { user } = useAuthStore();
  const [weekOffset, setWeekOffset] = useState(0);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const requestId = useRef(0);
  const isParent = user?.role === 'PARENT';
  const start = new Date();
  start.setDate(start.getDate() - (start.getDay() + 6) % 7 + weekOffset * 7);
  const dates = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(start);
    date.setDate(start.getDate() + index);
    return formatDate(date);
  });
  const load = async () => {
    const id = ++requestId.current;
    if (!user) { setLoading(false); return; }
    setLoading(true);
    setError(false);
    setLessons([]);
    try {
      const params = { startDate: dates[0], endDate: dates[6] };
      const data = await (isParent ? getPublicTimetable(params) : getSchedules(params));
      if (id === requestId.current) setLessons(data);
    } catch {
      if (id === requestId.current) setError(true);
    } finally {
      if (id === requestId.current) setLoading(false);
    }
  };
  useEffect(() => { load(); }, [weekOffset, user?.role]);
  Taro.useDidShow(() => { load(); });

  return (
    <View className={styles.coursePage}>
      <Card className={styles.summaryCard}>
        <Text className={styles.sectionTitle}>{isParent ? '全部课表' : '我的课表'}</Text>
        <View style={{ display: 'flex', justifyContent: 'space-between', marginTop: '16px' }}>
          <Text onClick={() => setWeekOffset(weekOffset - 1)}>‹ 上周</Text>
          <Text onClick={() => setWeekOffset(0)}>回到本周</Text>
          <Text onClick={() => setWeekOffset(weekOffset + 1)}>下周 ›</Text>
        </View>
        <Text className={styles.lessonDate}>{dates[0]} 至 {dates[6]}</Text>
      </Card>
      {loading ? <Text>加载中...</Text> : error ? (
        <Card onClick={load}><Text>课表加载失败，点击重试</Text></Card>
      ) : dates.map((date, index) => {
        const daily = lessons.filter(lesson => lesson.lessonDate === date)
          .sort((a, b) => a.startTime.localeCompare(b.startTime));
        return <View key={date} className={styles.section}>
          <Text className={styles.sectionTitle}>{date} 周{['一', '二', '三', '四', '五', '六', '日'][index]}</Text>
          {daily.length === 0 ? <Card><Text>暂无排课</Text></Card> : daily.map(lesson => (
            <Card key={lesson.id} className={styles.lessonCard}>
              <View className={styles.lessonHeader}>
                <View className={styles.lessonInfo}>
                  <Text className={styles.lessonName}>{lesson.courseName || '课程'}</Text>
                  <Text className={styles.lessonDate}>{lesson.startTime} - {lesson.endTime}</Text>
                </View>
                <Tag type={lesson.status === 'CANCELLED' ? 'danger' : 'info'}>
                  {({ SCHEDULED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' })[lesson.status] || lesson.status}
                </Tag>
              </View>
              <Text className={styles.lessonTeacher}>授课老师：{lesson.teacherName || '待确认'}</Text>
            </Card>
          ))}
        </View>;
      })}
    </View>
  );
}
