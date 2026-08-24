import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Lesson, Student } from '@/types';
import {
  getSchedules,
  signIn,
  completeCourse,
  getLessonAttendance,
} from '@/services/api';
import { formatDate, formatTime } from '@/utils/format';
import styles from './index.module.scss';

interface StudentWithSignIn extends Student {
  signed: boolean;
}

export default function CourseDetailPage() {
  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [students, setStudents] = useState<StudentWithSignIn[]>([]);
  const [selectedStudents, setSelectedStudents] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const options = Taro.getCurrentInstance()?.router?.params;
    if (options?.id) {
      fetchLesson(String(options.id));
    }
  }, []);

  const fetchLesson = async (id: string) => {
    setIsLoading(true);
    try {
      const [schedules, attendance] = await Promise.all([
        getSchedules().catch(() => []),
        getLessonAttendance({ lessonId: id }).catch(() => []),
      ]);
      const found = schedules.find(s => String(s.id) === String(id));
      if (found) {
        setLesson(found);
      } else {
        setLesson({ id, lessonNo: `ID:${id}`, status: 'SCHEDULED' } as any);
      }
      if (Array.isArray(attendance) && attendance.length > 0) {
        setStudents(attendance.map((a: any) => ({
          id: String(a.studentId || a.id),
          name: a.studentName || '未知',
          studentNo: a.studentNo || ('ID:' + (a.studentId || a.id)),
          gender: undefined,
          signed: a.status === 'PRESENT',
          attendanceStatus: a.status,
        } as StudentWithSignIn)));
      }
    } catch (err) {
      console.error('[CourseDetailPage.fetchLesson]', err);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleStudent = (studentId: string) => {
    if (selectedStudents.includes(studentId)) {
      setSelectedStudents(selectedStudents.filter(id => id !== studentId));
    } else {
      setSelectedStudents([...selectedStudents, studentId]);
    }
  };

  const selectAllUnsigned = () => {
    const unsigned = students.filter(s => !s.signed).map(s => s.id);
    if (selectedStudents.length === unsigned.length) {
      setSelectedStudents([]);
    } else {
      setSelectedStudents(unsigned);
    }
  };

  const handleSignIn = async () => {
    if (selectedStudents.length === 0) {
      Taro.showToast({ title: '请选择要签到的学生', icon: 'none' });
      return;
    }

    setIsSubmitting(true);
    try {
      await signIn({ lessonId: lesson!.id, studentIds: selectedStudents });
      setStudents(students.map(s =>
        selectedStudents.includes(s.id) ? { ...s, signed: true } : s
      ));
      setSelectedStudents([]);
      Taro.showToast({ title: `成功签到 ${selectedStudents.length} 人`, icon: 'success' });
    } catch (err) {
      console.error('[CourseDetailPage]', err);
      Taro.showToast({ title: '签到失败', icon: 'none' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleComplete = async () => {
    Taro.showModal({
      title: '确认结课',
      content: `当前课程有 ${students.filter(s => s.signed).length}/${students.length} 名学生已签到，确定要结课吗？`,
      success: async (res) => {
        if (res.confirm) {
          setIsSubmitting(true);
          try {
            await completeCourse({ lessonId: lesson!.id });
            Taro.showToast({ title: '课程已完成', icon: 'success' });
            setTimeout(() => {
              Taro.navigateBack();
            }, 1500);
          } catch (err) {
            console.error('[CourseDetailPage]', err);
            Taro.showToast({ title: '结课失败', icon: 'none' });
          } finally {
            setIsSubmitting(false);
          }
        }
      }
    });
  };

  const signedCount = students.filter(s => s.signed).length;

  if (!lesson && isLoading) {
    return (
      <View className={styles.container}>
        <Text>加载中...</Text>
      </View>
    );
  }

  if (!lesson) {
    return (
      <View className={styles.container}>
        <Text>找不到该课程</Text>
      </View>
    );
  }

  return (
    <ScrollView className={styles.detailPage} scrollY>
      <Card className={styles.infoCard}>
        <View className={styles.header}>
          <Text className={styles.courseName}>体育课</Text>
          {lesson.status === 'IN_PROGRESS' && <Tag type="warning">进行中</Tag>}
          {lesson.status === 'COMPLETED' && <Tag type="success">已完成</Tag>}
          {lesson.status === 'SCHEDULED' && <Tag type="info">未开始</Tag>}
          {lesson.status === 'CANCELLED' && <Tag type="danger">已取消</Tag>}
        </View>

        <View className={styles.timeRow}>
          <Text className={styles.label}>上课时间</Text>
          <Text className={styles.value}>{formatDate(lesson.lessonDate)} {formatTime(lesson.startTime)} - {formatTime(lesson.endTime)}</Text>
        </View>

        <View className={styles.timeRow}>
          <Text className={styles.label}>课程编号</Text>
          <Text className={styles.value}>{lesson.lessonNo || `ID:${lesson.id}`}</Text>
        </View>

        <View className={styles.timeRow}>
          <Text className={styles.label}>课程地点</Text>
          <Text className={styles.value}>{lesson.location || '未设置'}</Text>
        </View>

        <View className={styles.timeRow}>
          <Text className={styles.label}>班级</Text>
          <Text className={styles.value}>
            {lesson.classGroupId ? `班级ID:${lesson.classGroupId}` : students.length > 0 ? `${students.length}名学员` : '未分配'}
          </Text>
        </View>

        <View className={styles.timeRow}>
          <Text className={styles.label}>签到情况</Text>
          <Text className={`${styles.value} ${signedCount === students.length && students.length > 0 ? styles.allSigned : ''}`}>
            {signedCount}/{students.length} 已签到
          </Text>
        </View>

        {lesson.cancelReason && (
          <View className={styles.cancelReason}>
            <Text className={styles.reasonLabel}>取消原因</Text>
            <Text className={styles.reasonValue}>{lesson.cancelReason}</Text>
          </View>
        )}
      </Card>

      <Card className={styles.studentsCard}>
        <View className={styles.studentsHeader}>
          <Text className={styles.cardTitle}>学员列表</Text>
          {(lesson.status === 'IN_PROGRESS' || lesson.status === 'SCHEDULED') && (
            <Text className={styles.selectAllLink} onClick={selectAllUnsigned}>
              {selectedStudents.length > 0 ? '取消全选' : '全选未签到'}
            </Text>
          )}
        </View>

        <View className={styles.studentList}>
          {students.length === 0 ? (
            <View style={{ padding: 24 }}>
              <Text style={{ color: '#999' }}>该课程暂无学员数据</Text>
            </View>
          ) : (
            students.map(student => (
              <View key={student.id} className={`${styles.studentItem} ${selectedStudents.includes(student.id) ? styles.selected : ''}`}>
                {(lesson.status === 'IN_PROGRESS' || lesson.status === 'SCHEDULED') && (
                  <View
                    className={`${styles.checkbox} ${student.signed ? styles.signed : ''} ${!student.signed && selectedStudents.includes(student.id) ? styles.checked : ''}`}
                    onClick={(e: any) => {
                      e.stopPropagation && e.stopPropagation();
                      if (!student.signed) toggleStudent(student.id);
                    }}
                  />
                )}
                <View className={styles.studentAvatar}>
                  <Text className={styles.avatarText}>{student.name[0]}</Text>
                </View>
                <View className={styles.studentInfo}>
                  <Text className={styles.studentName}>{student.name}</Text>
                  <Text className={styles.studentNo}>{student.studentNo || ''}</Text>
                </View>
                {student.signed ? (
                  <Tag type="success">已签到</Tag>
                ) : (
                  <Tag type="info">未签到</Tag>
                )}
              </View>
            ))
          )}
        </View>
      </Card>

      {(lesson.status === 'IN_PROGRESS' || lesson.status === 'SCHEDULED') && (
        <View className={styles.actionBar}>
          {selectedStudents.length > 0 && (
            <CustomButton type="primary" size="lg" onClick={handleSignIn} disabled={isSubmitting}>
              {isSubmitting ? '签到中...' : `签到 ${selectedStudents.length} 人`}
            </CustomButton>
          )}
          {selectedStudents.length === 0 && lesson.status === 'IN_PROGRESS' && (
            <CustomButton type="warning" size="lg" onClick={handleSignIn} disabled={isSubmitting}>
              签到
            </CustomButton>
          )}
          {lesson.status === 'SCHEDULED' && selectedStudents.length === 0 && (
            <CustomButton type="warning" size="lg" onClick={() => Taro.showToast({ title: '请选择要签到的学生', icon: 'none' })}>
              开始课程
            </CustomButton>
          )}
          {lesson.status === 'IN_PROGRESS' && (
            <CustomButton type="secondary" size="lg" onClick={handleComplete} disabled={isSubmitting}>
              {isSubmitting ? '结课中...' : '完成课程'}
            </CustomButton>
          )}
        </View>
      )}
    </ScrollView>
  );
}
