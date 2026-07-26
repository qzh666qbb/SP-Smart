import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Lesson, Student } from '@/types';
import { formatDate } from '@/utils/format';
import styles from './index.module.scss';

interface AttendanceStudent extends Student {
  attendanceStatus: 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';
  signInAt?: string;
}

const mockLessons: Lesson[] = [
  { id: '1', lessonNo: 'L001', courseId: 'C001', classGroupId: 'G001', teacherId: 'T001', lessonDate: '2024-07-25', startTime: '08:00', endTime: '09:30', status: 'COMPLETED' },
  { id: '2', lessonNo: 'L002', courseId: 'C001', classGroupId: 'G001', teacherId: 'T001', lessonDate: '2024-07-23', startTime: '14:00', endTime: '15:30', status: 'COMPLETED' },
  { id: '3', lessonNo: 'L003', courseId: 'C001', classGroupId: 'G001', teacherId: 'T001', lessonDate: '2024-07-21', startTime: '08:00', endTime: '09:30', status: 'COMPLETED' },
  { id: '4', lessonNo: 'L004', courseId: 'C001', classGroupId: 'G001', teacherId: 'T001', lessonDate: '2024-07-18', startTime: '17:20', endTime: '18:50', status: 'COMPLETED' },
  { id: '5', lessonNo: 'L005', courseId: 'C001', classGroupId: 'G001', teacherId: 'T001', lessonDate: '2024-07-16', startTime: '09:40', endTime: '11:10', status: 'COMPLETED' },
];

const mockAttendance: AttendanceStudent[] = [
  { id: '1', name: '张三', age: 12, gender: 'MALE', studentNo: 'S001', attendanceStatus: 'PRESENT', signInAt: '08:00' },
  { id: '2', name: '李四', age: 12, gender: 'FEMALE', studentNo: 'S002', attendanceStatus: 'PRESENT', signInAt: '08:02' },
  { id: '3', name: '王五', age: 13, gender: 'MALE', studentNo: 'S003', attendanceStatus: 'LATE', signInAt: '08:15' },
  { id: '4', name: '赵六', age: 12, gender: 'FEMALE', studentNo: 'S004', attendanceStatus: 'PRESENT', signInAt: '07:58' },
  { id: '5', name: '钱七', age: 13, gender: 'MALE', studentNo: 'S005', attendanceStatus: 'ABSENT' },
  { id: '6', name: '孙八', age: 12, gender: 'FEMALE', studentNo: 'S006', attendanceStatus: 'EXCUSED' },
];

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
      setLessons(mockLessons);
    } catch (err) {
      console.error('[CourseTransactionPage]', err);
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleLessonClick = (lesson: Lesson) => {
    setSelectedLesson(lesson);
    setAttendanceList(mockAttendance);
    setShowAttendanceModal(true);
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
  
  const getAttendanceTag = (status: string) => {
    switch (status) {
      case 'PRESENT':
        return <Tag type="success">出勤</Tag>;
      case 'ABSENT':
        return <Tag type="danger">缺勤</Tag>;
      case 'LATE':
        return <Tag type="warning">迟到</Tag>;
      case 'EXCUSED':
        return <Tag type="info">请假</Tag>;
      default:
        return <Tag type="info">未知</Tag>;
    }
  };
  
  const presentCount = attendanceList.filter(s => s.attendanceStatus === 'PRESENT').length;
  const absentCount = attendanceList.filter(s => s.attendanceStatus === 'ABSENT').length;
  const lateCount = attendanceList.filter(s => s.attendanceStatus === 'LATE').length;
  const excusedCount = attendanceList.filter(s => s.attendanceStatus === 'EXCUSED').length;
  
  return (
    <ScrollView className={styles.transactionPage} scrollY>
      <Card className={styles.summaryCard}>
        <View className={styles.summaryHeader}>
          <Text className={styles.summaryLabel}>课程统计</Text>
        </View>
        <View className={styles.summaryStats}>
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{lessons.length}</Text>
            <Text className={styles.statLabel}>已完成课程</Text>
          </View>
          <View className={styles.statDivider} />
          <View className={styles.statItem}>
            <Text className={styles.statValue}>12</Text>
            <Text className={styles.statLabel}>本月课程</Text>
          </View>
          <View className={styles.statDivider} />
          <View className={styles.statItem}>
            <Text className={styles.statValue}>48</Text>
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
                <Text className={styles.lessonNo}>课程编号: {lesson.lessonNo}</Text>
                <Text className={styles.lessonTeacher}>授课老师: 张老师</Text>
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
                <Text className={styles.summaryNum}>{attendanceList.length}</Text>
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
                {attendanceList.map(student => (
                  <View key={student.id} className={styles.studentItem}>
                    <View className={styles.studentAvatar}>
                      <Text className={styles.avatarText}>{student.name[0]}</Text>
                    </View>
                    <View className={styles.studentInfo}>
                      <Text className={styles.studentName}>{student.name}</Text>
                      <Text className={styles.studentNo}>
                        {student.studentNo}
                        {student.signInAt && ` · 签到时间: ${student.signInAt}`}
                      </Text>
                    </View>
                    {getAttendanceTag(student.attendanceStatus)}
                  </View>
                ))}
              </ScrollView>
            </View>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
