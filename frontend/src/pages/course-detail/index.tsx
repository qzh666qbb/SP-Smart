import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Lesson, Student } from '@/types';
import { getSchedules, signIn, completeCourse } from '@/services/api';
import { formatDate, formatTime } from '@/utils/format';
import styles from './index.module.scss';

interface StudentWithSignIn extends Student {
  signed: boolean;
}

const mockStudents: StudentWithSignIn[] = [
  { id: '1', name: '张三', age: 12, gender: 'MALE', studentNo: 'S001', signed: false, parentName: '张爸爸', parentPhone: '13800138001' },
  { id: '2', name: '李四', age: 12, gender: 'FEMALE', studentNo: 'S002', signed: true, parentName: '李妈妈', parentPhone: '13800138002' },
  { id: '3', name: '王五', age: 13, gender: 'MALE', studentNo: 'S003', signed: false, parentName: '王爸爸', parentPhone: '13800138003' },
  { id: '4', name: '赵六', age: 12, gender: 'FEMALE', studentNo: 'S004', signed: true, parentName: '赵妈妈', parentPhone: '13800138004' },
  { id: '5', name: '钱七', age: 13, gender: 'MALE', studentNo: 'S005', signed: false, parentName: '钱爸爸', parentPhone: '13800138005' },
  { id: '6', name: '孙八', age: 12, gender: 'FEMALE', studentNo: 'S006', signed: false, parentName: '孙妈妈', parentPhone: '13800138006' },
];

export default function CourseDetailPage() {
  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [students, setStudents] = useState<StudentWithSignIn[]>(mockStudents);
  const [selectedStudents, setSelectedStudents] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const options = Taro.getCurrentInstance()?.router?.params;
    if (options?.id) {
      fetchLesson(options.id);
    }
  }, []);

  const fetchLesson = async (id: string) => {
    try {
      const schedules = await getSchedules();
      const found = schedules.find(s => s.id === id);
      if (found) {
        setLesson(found);
      }
    } catch (err) {
      console.error('[CourseDetailPage]', err);
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

  if (!lesson) {
    return (
      <View className={styles.container}>
        <Text>加载中...</Text>
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
          <Text className={styles.value}>{lesson.lessonNo}</Text>
        </View>

        <View className={styles.timeRow}>
          <Text className={styles.label}>授课老师</Text>
          <Text className={styles.value}>张老师</Text>
        </View>

        <View className={styles.timeRow}>
          <Text className={styles.label}>班级</Text>
          <Text className={styles.value}>初一(1)班</Text>
        </View>

        <View className={styles.timeRow}>
          <Text className={styles.label}>签到情况</Text>
          <Text className={`${styles.value} ${signedCount === students.length ? styles.allSigned : ''}`}>
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
          {students.map(student => (
            <View key={student.id} className={`${styles.studentItem} ${selectedStudents.includes(student.id) ? styles.selected : ''}`}>
              {(lesson.status === 'IN_PROGRESS' || lesson.status === 'SCHEDULED') && (
                <View 
                  className={`${styles.checkbox} ${student.signed ? styles.signed : ''} ${!student.signed && selectedStudents.includes(student.id) ? styles.checked : ''}`}
                  onClick={(e: any) => { 
                    e.stopPropagation && e.stopPropagation(); 
                    if (!student.signed) {
                      toggleStudent(student.id);
                    }
                  }}
                />
              )}
              <View className={styles.studentAvatar}>
                <Text className={styles.avatarText}>{student.name[0]}</Text>
              </View>
              <View className={styles.studentInfo}>
                <Text className={styles.studentName}>{student.name}</Text>
                <Text className={styles.studentNo}>{student.studentNo}</Text>
                {student.parentName && (
                  <Text className={styles.parentInfo}>{student.parentName} {student.parentPhone}</Text>
                )}
              </View>
              {student.signed ? (
                <Tag type="success">已签到</Tag>
              ) : (
                <Tag type="info">未签到</Tag>
              )}
            </View>
          ))}
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
