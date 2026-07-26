import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Lesson, ParentProfile, Student } from '@/types';
import { getLessons, getParentStudents, signIn } from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import styles from './index.module.scss';

export default function ParentCoursePage() {
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const [isSigningIn, setIsSigningIn] = useState(false);
  const { profile } = useAuthStore();
  const parentProfile = profile as ParentProfile;

  useEffect(() => {
    fetchStudents();
  }, []);

  useEffect(() => {
    if (selectedStudent) {
      fetchLessons(selectedStudent.id);
    }
  }, [selectedStudent]);

  const fetchStudents = async () => {
    try {
      const students = await getParentStudents();
      setStudents(students || []);
      if (students && students.length > 0) {
        setSelectedStudent(students[0]);
      }
    } catch (err) {
      console.error('[ParentCoursePage]', err);
      if (parentProfile?.students && parentProfile.students.length > 0) {
        setStudents(parentProfile.students);
        setSelectedStudent(parentProfile.students[0]);
      }
    }
  };

  const fetchLessons = async (studentId: string) => {
    setIsLoading(true);
    try {
      const data = await getLessons({ studentId });
      setLessons(data);
    } catch (err) {
      console.error('[ParentCoursePage]', err);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusTag = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <Tag type="success">已完成</Tag>;
      case 'IN_PROGRESS':
        return <Tag type="warning">进行中</Tag>;
      case 'SCHEDULED':
        return <Tag type="info">未开始</Tag>;
      case 'CANCELLED':
        return <Tag type="danger">已取消</Tag>;
      default:
        return <Tag type="info">未知</Tag>;
    }
  };

  const getAttendanceStatus = (lesson: Lesson) => {
    if (!selectedStudent) return null;
    const attendance = lesson.studentAttendance?.[selectedStudent.id];
    if (attendance) {
      if (attendance === 'PRESENT') return { status: 'PRESENT', label: '已签到', type: 'success' };
      if (attendance === 'ABSENT') return { status: 'ABSENT', label: '缺勤', type: 'danger' };
      if (attendance === 'LATE') return { status: 'LATE', label: '迟到', type: 'warning' };
    }
    if (lesson.status === 'COMPLETED') {
      return { status: 'ABSENT', label: '缺勤', type: 'danger' };
    }
    return null;
  };

  const handleSignIn = async (lesson: Lesson) => {
    if (!selectedStudent || isSigningIn) return;
    
    setIsSigningIn(true);
    try {
      await signIn({ lessonId: lesson.id, studentIds: [selectedStudent.id] });
      Taro.showToast({ title: '签到成功', icon: 'success' });
      fetchLessons(selectedStudent.id);
    } catch (err) {
      console.error('[ParentCoursePage]', err);
      Taro.showToast({ title: '签到失败', icon: 'none' });
    } finally {
      setIsSigningIn(false);
    }
  };

  return (
    <ScrollView className={styles.coursePage} scrollY>
      {students.length > 0 && (
        <Card className={styles.studentCard}>
          <View className={styles.studentInfo}>
            <Text className={styles.studentName}>{selectedStudent?.name || '孩子姓名'}</Text>
            <Text className={styles.studentNo}>学号: {selectedStudent?.studentNo || '--'}</Text>
          </View>
        </Card>
      )}

      <Card className={styles.summaryCard}>
        <View className={styles.summaryHeader}>
          <Text className={styles.summaryLabel}>课程统计</Text>
        </View>
        <View className={styles.summaryStats}>
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{lessons.length}</Text>
            <Text className={styles.statLabel}>总课程数</Text>
          </View>
          <View className={styles.statDivider} />
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{lessons.filter(l => l.status === 'COMPLETED').length}</Text>
            <Text className={styles.statLabel}>已完成</Text>
          </View>
          <View className={styles.statDivider} />
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{lessons.filter(l => l.status === 'SCHEDULED').length}</Text>
            <Text className={styles.statLabel}>待上课</Text>
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
          lessons.map(lesson => {
            const attendance = getAttendanceStatus(lesson);
            const canSignIn = lesson.status === 'IN_PROGRESS' && !attendance;
            
            return (
              <Card key={lesson.id} className={styles.lessonCard}>
                <View className={styles.lessonHeader}>
                  <View className={styles.lessonInfo}>
                    <Text className={styles.lessonName}>{lesson.courseName || '体育课'}</Text>
                    <Text className={styles.lessonDate}>{formatDate(lesson.lessonDate)} {lesson.startTime} - {lesson.endTime}</Text>
                  </View>
                  {getStatusTag(lesson.status)}
                </View>
                <View className={styles.lessonFooter}>
                  <Text className={styles.lessonTeacher}>授课老师: {lesson.teacherName || '张老师'}</Text>
                  {attendance ? (
                    <Tag type={attendance.type as any}>{attendance.label}</Tag>
                  ) : canSignIn ? (
                    <CustomButton type="primary" size="sm" onClick={() => handleSignIn(lesson)} disabled={isSigningIn}>
                      {isSigningIn ? '签到中...' : '签到'}
                    </CustomButton>
                  ) : null}
                </View>
              </Card>
            );
          })
        )}
      </View>
    </ScrollView>
  );
}
