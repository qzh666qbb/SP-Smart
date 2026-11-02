import { useState, useEffect } from 'react';
import Taro from '@tarojs/taro';
import { View, Text, ScrollView } from '@tarojs/components';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { HourTransaction, Lesson, Student, StudentHourAccount } from '@/types';
import {
  getSchedules,
  getLessonAttendance,
  getAllTeacherStudents,
  getHourAccount,
  getHourTransactions,
} from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import styles from './index.module.scss';

interface AttendanceStudent extends Student {
  attendanceStatus: 'PRESENT' | 'ABSENT' | 'LATE' | 'LEAVE';
  signInAt?: string;
}

export default function CourseTransactionPage() {
  const { user } = useAuthStore();
  const routeStudentId = String(Taro.getCurrentInstance()?.router?.params?.studentId || '');
  const isParentHours = user?.role === 'PARENT' && Boolean(routeStudentId);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showAttendanceModal, setShowAttendanceModal] = useState(false);
  const [selectedLesson, setSelectedLesson] = useState<Lesson | null>(null);
  const [attendanceList, setAttendanceList] = useState<AttendanceStudent[]>([]);
  const [hourAccount, setHourAccount] = useState<StudentHourAccount | null>(null);
  const [hourTransactions, setHourTransactions] = useState<HourTransaction[]>([]);

  useEffect(() => {
    if (isParentHours) fetchParentHours(routeStudentId);
    else fetchData();
  }, [isParentHours, routeStudentId]);

  const fetchParentHours = async (studentId: string) => {
    setIsLoading(true);
    try {
      const [account, transactions] = await Promise.all([
        getHourAccount({ studentId }),
        getHourTransactions({ studentId }),
      ]);
      setHourAccount(account);
      setHourTransactions(transactions || []);
    } catch (error) {
      console.error('[CourseTransactionPage.fetchParentHours]', error);
      Taro.showToast({ title: '课时数据加载失败', icon: 'none' });
    } finally {
      setIsLoading(false);
    }
  };

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const now = new Date();
      const data = await getSchedules({
        startDate: `${now.getFullYear() - 1}-01-01`,
        endDate: `${now.getFullYear()}-12-31`,
      });
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
        const students = await getAllTeacherStudents().catch(() => [] as Student[]);
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
      case 'LEAVE': return <Tag type="info">请假</Tag>;
      default: return <Tag type="info">未知</Tag>;
    }
  };

  const presentCount = attendanceList.filter(s => s.attendanceStatus === 'PRESENT').length;
  const absentCount = attendanceList.filter(s => s.attendanceStatus === 'ABSENT').length;
  const lateCount = attendanceList.filter(s => s.attendanceStatus === 'LATE').length;
  const excusedCount = attendanceList.filter(s => s.attendanceStatus === 'LEAVE').length;
  const totalCount = attendanceList.length;
  const monthCount = lessons.filter(l => {
    const d = l.lessonDate || '';
    const now = new Date();
    const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    return d.startsWith(ym);
  }).length;

  const showRechargeHelp = () => {
    Taro.showModal({
      title: '课时充值',
      content: '如需充值课时，请通过微信联系管理员。管理员添加课时后，本页面会显示最新余额和充值流水。',
      showCancel: false,
      confirmText: '我知道了',
    });
  };

  if (isParentHours) {
    return (
      <View className={styles.transactionPage}>
        <Card className={styles.summaryCard}>
          <View className={styles.summaryHeader}><Text className={styles.summaryLabel}>课时账户</Text></View>
          <View className={styles.summaryStats}>
            <View className={styles.statItem}><Text className={styles.statValue}>{hourAccount?.remainingHours ?? 0}</Text><Text className={styles.statLabel}>剩余课时</Text></View>
            <View className={styles.statDivider} />
            <View className={styles.statItem}><Text className={styles.statValue}>{hourAccount?.totalHours ?? 0}</Text><Text className={styles.statLabel}>累计课时</Text></View>
            <View className={styles.statDivider} />
            <View className={styles.statItem}><Text className={styles.statValue}>{hourAccount?.usedHours ?? 0}</Text><Text className={styles.statLabel}>已用课时</Text></View>
          </View>
        </Card>
        <Card className={styles.rechargeNotice} onClick={showRechargeHelp}>
          <View className={styles.rechargeNoticeContent}>
            <Text className={styles.rechargeNoticeTitle}>需要充值课时？</Text>
            <Text className={styles.rechargeNoticeText}>请通过微信联系管理员，由管理员在管理端为学生添加课时</Text>
          </View>
          <Text className={styles.rechargeNoticeAction}>查看说明 ›</Text>
        </Card>
        <View className={styles.section}>
          <Text className={styles.sectionTitle}>课时流水</Text>
          {isLoading ? (
            <View className={styles.loading}><Text>加载中...</Text></View>
          ) : hourTransactions.length === 0 ? (
            <Card className={styles.emptyCard}><Text className={styles.emptyText}>暂无课时流水</Text></Card>
          ) : hourTransactions.map((transaction) => (
            <Card key={transaction.id} className={styles.lessonCard}>
              <View className={styles.lessonHeader}>
                <View className={styles.lessonInfo}>
                  <Text className={styles.lessonName}>{transaction.reason || '课时变动'}</Text>
                  <Text className={styles.lessonDate}>{transaction.createTime ? formatDate(transaction.createTime) : '-'}</Text>
                </View>
                <Tag type={transaction.changeAmount >= 0 ? 'success' : 'warning'}>{transaction.changeAmount >= 0 ? '+' : ''}{transaction.changeAmount}</Tag>
              </View>
              <View className={styles.lessonFooter}>
                <Text className={styles.lessonNo}>变动前：{transaction.beforeBalance}</Text>
                <Text className={styles.lessonTeacher}>变动后：{transaction.afterBalance}</Text>
              </View>
            </Card>
          ))}
        </View>
      </View>
    );
  }

  return (
    <View className={styles.transactionPage}>
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
                  <Text className={styles.lessonName}>{lesson.courseName || `课程 #${lesson.courseId}`}</Text>
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
    </View>
  );
}
