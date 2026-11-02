import { useState, useEffect, useRef } from 'react';
import { View, Text, Picker } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Lesson, ParentProfile, Student } from '@/types';
import { getLessons, getParentStudents, parentSignIn } from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import styles from './index.module.scss';

export default function ParentCoursePage() {
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const [isSigningIn, setIsSigningIn] = useState(false);
  const [signedLessonIds, setSignedLessonIds] = useState<string[]>([]);
  const lessonsRequestId = useRef(0);
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
      const nextStudents = await getParentStudents();
      setStudents(nextStudents || []);
      if (nextStudents && nextStudents.length > 0) {
        setSelectedStudent(nextStudents[0]);
      } else {
        lessonsRequestId.current += 1;
        setSelectedStudent(null);
        setLessons([]);
        setIsLoading(false);
      }
    } catch (err) {
      console.error('[ParentCoursePage]', err);
      if (parentProfile?.students && parentProfile.students.length > 0) {
        setStudents(parentProfile.students);
        setSelectedStudent(parentProfile.students[0]);
      } else {
        setIsLoading(false);
      }
    }
  };

  const fetchLessons = async (studentId: string) => {
    const requestId = ++lessonsRequestId.current;
    setIsLoading(true);
    try {
      const now = new Date();
      const start = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate());
      const end = new Date(now.getFullYear(), now.getMonth() + 1, now.getDate());
      const data = await getLessons({
        studentId,
        startDate: formatDate(start),
        endDate: formatDate(end),
      });
      if (requestId === lessonsRequestId.current) setLessons(data);
    } catch (err) {
      console.error('[ParentCoursePage]', err);
    } finally {
      if (requestId === lessonsRequestId.current) setIsLoading(false);
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
    if (signedLessonIds.includes(lesson.id)) {
      return { status: 'PRESENT', label: '已签到', type: 'success' };
    }
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
      await parentSignIn({ lessonId: lesson.id, studentId: selectedStudent.id });
      setSignedLessonIds((ids) => ids.includes(lesson.id) ? ids : [...ids, lesson.id]);
      Taro.showToast({ title: '签到成功', icon: 'success' });
    } catch (err) {
      console.error('[ParentCoursePage]', err);
      Taro.showToast({ title: '签到失败', icon: 'none' });
    } finally {
      setIsSigningIn(false);
    }
  };

  const handleStudentChange = (e: any) => {
    const student = students[Number(e.detail.value)];
    if (student) {
      setLessons([]);
      setSignedLessonIds([]);
      setSelectedStudent(student);
    }
  };

  return (
    <View className={styles.coursePage}>
      {students.length > 0 && (
        <Picker mode="selector" range={students.map((s) => s.name)} onChange={handleStudentChange}>
          <Card className={styles.studentCard}>
            <View className={styles.studentInfo}>
              <Text className={styles.studentName}>{selectedStudent?.name || '孩子姓名'}</Text>
              <Text className={styles.studentNo}>学号: {selectedStudent?.studentNo || '--'}</Text>
            </View>
            <Text className={styles.arrow}>›</Text>
          </Card>
        </Picker>
      )}

      {!isLoading && students.length === 0 && (
        <Card className={styles.emptyCard} onClick={() => Taro.navigateTo({ url: '/pages/child-add-edit/index?isFirst=1' })}>
          <Text className={styles.emptyText}>暂无孩子资料，点击添加</Text>
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
                    <Text className={styles.lessonName}>{lesson.courseName || `课程 #${lesson.courseId}`}</Text>
                    <Text className={styles.lessonDate}>{formatDate(lesson.lessonDate)} {lesson.startTime} - {lesson.endTime}</Text>
                  </View>
                  {getStatusTag(lesson.status)}
                </View>
                <View className={styles.lessonFooter}>
                  <Text className={styles.lessonTeacher}>授课老师: {lesson.teacherName || `教师 #${lesson.teacherId}`}</Text>
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
    </View>
  );
}
