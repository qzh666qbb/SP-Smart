import { useState, useEffect } from 'react';
import Taro from '@tarojs/taro';
import { View, Text, ScrollView } from '@tarojs/components';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Lesson, Student } from '@/types';
import {
  getSchedules,
  getLessonAttendance,
  getTeacherStudents,
} from '@/services/api';
import { formatDate } from '@/utils/format';
import styles from './index.module.scss';

interface AttendanceStudent extends Student {
  attendanceStatus: 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';
  signInAt?: string;
}

export default function CourseTransactionPage() {
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showAttendanceModal, setShowAttendanceModal] = useState(false);
  const [selectedLesson, setSelectedLesson] = useState<Lesson | null>(null);
  const [attendanceList, setAttendanceList] = useState<AttendanceStudent[]>([]);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const data = await getSchedules();
      const completedOrToday = Array.isArray(data)
        ? data.filter(l => ['COMPLETED', 'IN_PROGRESS', 'SCHEDULED'].includes(l.status))
            .sort((a, b) => (b.lessonDate || '').localeCompare(a.lessonDate || ''))
        : [];
      setLessons(completedOrToday);
    } catch (err) {
      console.error('[CourseTransactionPage.fetchData]', err);
      Taro.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleLessonClick = async (lesson: Lesson) => {
    setSelectedLesson(lesson);
    try {
      const list = await getLessonAttendance({ lessonId: lesson.id });
      if (Array.isArray(list) && list.length > 0) {
        setAttendanceList(list.map(a => ({
          id: String(a.studentId || a.id),
          name: a.studentName || '未知',
          studentNo: a.studentNo || ('ID:' + (a.studentId || a.id)),
          attendanceStatus: (a.status || 'ABSENT') as any,
          signInAt: a.signInAt || a.time || undefined,
        })));
      } else {
        const { students } = await getTeacherStudents({ pageNum: 1, pageSize: 200 }).catch(() => ({ students: [] as Student[] }));
        setAttendanceList(students.map(s => ({
          ...s,
          attendanceStatus: 'ABSENT',
          signInAt: undefined,
        })) as AttendanceStudent[]);
      }
    } catch (err) {
      console.error('[CourseTransactionPage.attendance]', err);
      setAttendanceList([]);
    }
    setShowAttendanceModal(true);
  };

  const getStatusTag = (status: string) => {
    switch (status) {
      case 'COMPLETED': return <Tag type="success">已完成</Tag>;
      case 'IN_PROGRESS': return <Tag type="warning">进行中</Tag>;
      case 'SCHEDULED': return <Tag type="info">未开始</Tag>;
      case 'CANCELLED': return <Tag type="danger">已取消</Tag>;
      default: return <Tag type="info">未知</Tag>;
    }
  };

  const getAttendanceTag = (status: string) => {
    switch (status) {
      case 'PRESENT': return <Tag type="success">出勤</Tag>;
      case 'ABSENT': return <Tag type="danger">缺勤</Tag>;
      case 'LATE': return <Tag type="warning">迟到</Tag>;
      case 'EXCUSED': return <Tag type="info">请假</Tag>;
      default: return <Tag type="info">未知</Tag>;
    }
  };

  const presentCount = attendanceList.filter(s => s.attendanceStatus === 'PRESENT').length;
  const absentCount = attendanceList.filter(s => s.attendanceStatus === 'ABSENT').length;
  const lateCount = attendanceList.filter(s => s.attendanceStatus === 'LATE').length;
  const excusedCount = attendanceList.filter(s => s.attendanceStatus === 'EXCUSED').length;
  const totalCount = attendanceList.length;
  const monthCount = lessons.filter(l => {
    const d = l.lessonDate || '';
    const now = new Date();
    const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    return d.startsWith(ym);
  }).length;

  return (
    <ScrollView className={styles.transactionPage} scrollY>
      <Card className={styles.summaryCard}>
        <View className={styles.summaryHeader}>
          <Text className={styles.summaryLabel}>课程统计</Text>
        </View>
        <View className={styles.summaryStats}>
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{lessons.filter(l => l.status === 'COMPLETED').length}</Text>
            <Text className={styles.statLabel}>已完成课程</Text>
          </View>
          <View className={styles.statDivider} />
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{monthCount}</Text>
            <Text className={styles.statLabel}>本月课程</Text>
          </View>
          <View className={styles.statDivider} />
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{lessons.length}</Text>
            <Text className={styles.statLabel}>总课程数</Text>
          </View>
        </View>
      </Card>

      <View className={styles.section}>
        <Text className={styles.sectionTitle}>上课记录</Text>

        {isLoading ? (
          <View className={styles.loading}>
            <Text>加载中...</Text>
          </View>
        ) : lessons.length === 0 ? (
          <Card className={styles.emptyCard}>
            <Text className={styles.emptyText}>暂无上课记录</Text>
          </Card>
        ) : (
          lessons.map(lesson => (
            <Card key={lesson.id} className={styles.lessonCard} onClick={() => handleLessonClick(lesson)}>
              <View className={styles.lessonHeader}>
                <View className={styles.lessonInfo}>
                  <Text className={styles.lessonName}>体育课</Text>
                  <Text className={styles.lessonDate}>{formatDate(lesson.lessonDate)} {lesson.startTime} - {lesson.endTime}</Text>
                </View>
                {getStatusTag(lesson.status)}
              </View>
              <View className={styles.lessonFooter}>
                <Text className={styles.lessonNo}>课程编号: {lesson.lessonNo || `ID:${lesson.id}`}</Text>
                <Text className={styles.lessonTeacher}>教师ID: {lesson.teacherId || '-'}</Text>
              </View>
              <View className={styles.lessonAttendance}>
                <Text className={styles.attendanceText}>查看出勤情况</Text>
                <Text className={styles.arrow}>›</Text>
              </View>
            </Card>
          ))
        )}
      </View>

      {showAttendanceModal && selectedLesson && (
        <View className={styles.modalOverlay} onClick={() => setShowAttendanceModal(false)}>
          <View className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <View className={styles.modalHeader}>
              <View className={styles.modalTitleWrap}>
                <Text className={styles.modalTitle}>出勤详情</Text>
                <Text className={styles.modalSubtitle}>
                  {formatDate(selectedLesson.lessonDate)} {selectedLesson.startTime} - {selectedLesson.endTime}
                </Text>
              </View>
              <Text className={styles.modalClose} onClick={() => setShowAttendanceModal(false)}>×</Text>
            </View>

            <View className={styles.attendanceSummary}>
              <View className={styles.summaryItem}>
                <Text className={styles.summaryNum}>{totalCount}</Text>
                <Text className={styles.summaryLabel}>应到</Text>
              </View>
              <View className={styles.summaryItem}>
                <Text className={styles.summaryNum} style={{ color: '#10B981' }}>{presentCount}</Text>
                <Text className={styles.summaryLabel}>出勤</Text>
              </View>
              <View className={styles.summaryItem}>
                <Text className={styles.summaryNum} style={{ color: '#EF4444' }}>{absentCount}</Text>
                <Text className={styles.summaryLabel}>缺勤</Text>
              </View>
              <View className={styles.summaryItem}>
                <Text className={styles.summaryNum} style={{ color: '#F59E0B' }}>{lateCount}</Text>
                <Text className={styles.summaryLabel}>迟到</Text>
              </View>
              <View className={styles.summaryItem}>
                <Text className={styles.summaryNum} style={{ color: '#3B82F6' }}>{excusedCount}</Text>
                <Text className={styles.summaryLabel}>请假</Text>
              </View>
            </View>

            <View className={styles.modalBody}>
              <ScrollView className={styles.studentList} scrollY>
                {attendanceList.length === 0 ? (
                  <Text style={{ color: '#999', padding: 16 }}>暂无出勤数据</Text>
                ) : (
                  attendanceList.map(student => (
                    <View key={student.id} className={styles.studentItem}>
                      <View className={styles.studentAvatar}>
                        <Text className={styles.avatarText}>{student.name[0]}</Text>
                      </View>
                      <View className={styles.studentInfo}>
                        <Text className={styles.studentName}>{student.name}</Text>
                        <Text className={styles.studentNo}>
                          {student.studentNo || ('ID:' + student.id)}
                          {student.signInAt && ` · 签到时间: ${student.signInAt}`}
                        </Text>
                      </View>
                      {getAttendanceTag(student.attendanceStatus)}
                    </View>
                  ))
                )}
              </ScrollView>
            </View>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
