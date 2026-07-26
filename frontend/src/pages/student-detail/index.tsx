import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Student, PhysicalTestRecord, Lesson } from '@/types';
import { formatDate, getLevelText } from '@/utils/format';
import styles from './index.module.scss';

const mockStudent: Student = {
  id: '1',
  name: '张三',
  age: 12,
  gender: 'MALE',
  studentNo: 'S001',
  parentName: '张父',
  parentPhone: '13800138001'
};

const mockRecords: PhysicalTestRecord[] = [
  { id: '1', recordNo: 'R001', studentId: '1', testerId: 'T001', testDate: '2024-01-15', totalScore: 85, overallLevel: 'EXCELLENT', status: 'CONFIRMED' },
  { id: '2', recordNo: 'R002', studentId: '1', testerId: 'T001', testDate: '2024-03-20', totalScore: 78, overallLevel: 'GOOD', status: 'CONFIRMED' },
  { id: '3', recordNo: 'R003', studentId: '1', testerId: 'T001', testDate: '2024-06-10', totalScore: 92, overallLevel: 'EXCELLENT', status: 'CONFIRMED' },
];

const mockCourses: Lesson[] = [
  { id: '1', lessonNo: 'L001', courseId: 'C001', classGroupId: 'G001', teacherId: 'T001', lessonDate: '2024-07-22', startTime: '08:00', endTime: '09:30', status: 'COMPLETED' },
  { id: '2', lessonNo: 'L002', courseId: 'C001', classGroupId: 'G001', teacherId: 'T001', lessonDate: '2024-07-20', startTime: '14:00', endTime: '15:30', status: 'COMPLETED' },
  { id: '3', lessonNo: 'L003', courseId: 'C001', classGroupId: 'G001', teacherId: 'T001', lessonDate: '2024-07-18', startTime: '08:00', endTime: '09:30', status: 'COMPLETED' },
];

export default function StudentDetailPage() {
  const [student, setStudent] = useState<Student | null>(null);
  const [records, setRecords] = useState<PhysicalTestRecord[]>([]);
  const [courses, setCourses] = useState<Lesson[]>([]);
  const [activeTab, setActiveTab] = useState<'physical' | 'courses'>('physical');
  const [selectedRecord, setSelectedRecord] = useState<PhysicalTestRecord | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setStudent(mockStudent);
      setRecords(mockRecords);
      setCourses(mockCourses);
    } catch (err) {
      console.error('[StudentDetailPage]', err);
    }
  };

  const handleRecordClick = (record: PhysicalTestRecord) => {
    setSelectedRecord(record);
  };

  const handleDownloadReport = () => {
    Taro.showToast({ title: '正在下载报告...', icon: 'loading' });
    setTimeout(() => {
      Taro.showToast({ title: '报告下载成功', icon: 'success' });
    }, 1500);
  };

  const getLevelTag = (level: string) => {
    switch (level) {
      case 'EXCELLENT': return <Tag type="success">优秀</Tag>;
      case 'GOOD': return <Tag type="primary">良好</Tag>;
      case 'PASS': return <Tag type="info">及格</Tag>;
      default: return <Tag type="danger">不及格</Tag>;
    }
  };

  const getStatusTag = (status: string) => {
    switch (status) {
      case 'COMPLETED': return <Tag type="success">已完成</Tag>;
      case 'IN_PROGRESS': return <Tag type="warning">进行中</Tag>;
      case 'SCHEDULED': return <Tag type="info">未开始</Tag>;
      default: return <Tag type="danger">已取消</Tag>;
    }
  };

  if (!student) {
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
          <View className={styles.avatar}>
            <Text className={styles.avatarText}>{student.name[0]}</Text>
          </View>
          <View className={styles.userInfo}>
            <Text className={styles.userName}>{student.name}</Text>
            <View className={styles.userMeta}>
              {student.gender === 'MALE' && <Tag type="primary">男</Tag>}
              {student.gender === 'FEMALE' && <Tag type="success">女</Tag>}
              {student.age && <Text className={styles.age}>{student.age}岁</Text>}
            </View>
          </View>
        </View>
        <View className={styles.infoRow}>
          <Text className={styles.infoLabel}>学号</Text>
          <Text className={styles.infoValue}>{student.studentNo}</Text>
        </View>
        <View className={styles.infoRow}>
          <Text className={styles.infoLabel}>家长</Text>
          <Text className={styles.infoValue}>{student.parentName || '未填写'}</Text>
        </View>
        <View className={styles.infoRow}>
          <Text className={styles.infoLabel}>联系电话</Text>
          <Text className={styles.infoValue}>{student.parentPhone || '未填写'}</Text>
        </View>
      </Card>

      <View className={styles.tabHeader}>
        <View 
          className={`${styles.tabItem} ${activeTab === 'physical' ? styles.active : ''}`}
          onClick={() => setActiveTab('physical')}
        >
          <Text className={styles.tabText}>体测记录</Text>
        </View>
        <View 
          className={`${styles.tabItem} ${activeTab === 'courses' ? styles.active : ''}`}
          onClick={() => setActiveTab('courses')}
        >
          <Text className={styles.tabText}>课程记录</Text>
        </View>
      </View>

      {activeTab === 'physical' && (
        <View className={styles.content}>
          {records.length === 0 ? (
            <Card className={styles.emptyCard}>
              <Text className={styles.emptyText}>暂无体测记录</Text>
            </Card>
          ) : (
            records.map(record => (
              <Card key={record.id} className={styles.recordCard} onClick={() => handleRecordClick(record)}>
                <View className={styles.recordHeader}>
                  <View className={styles.recordInfo}>
                    <Text className={styles.recordNo}>{record.recordNo}</Text>
                    <Text className={styles.recordDate}>{formatDate(record.testDate)}</Text>
                  </View>
                  <View className={styles.recordScore}>
                    <Text className={styles.score}>{record.totalScore}</Text>
                    {getLevelTag(record.overallLevel)}
                  </View>
                </View>
              </Card>
            ))
          )}
        </View>
      )}

      {activeTab === 'courses' && (
        <View className={styles.content}>
          {courses.length === 0 ? (
            <Card className={styles.emptyCard}>
              <Text className={styles.emptyText}>暂无课程记录</Text>
            </Card>
          ) : (
            courses.map(course => (
              <Card key={course.id} className={styles.courseCard}>
                <View className={styles.courseHeader}>
                  <View className={styles.courseInfo}>
                    <Text className={styles.courseName}>体育课</Text>
                    <Text className={styles.courseDate}>{formatDate(course.lessonDate)} {course.startTime} - {course.endTime}</Text>
                  </View>
                  {getStatusTag(course.status)}
                </View>
                <View className={styles.courseFooter}>
                  <Text className={styles.courseNo}>课程编号: {course.lessonNo}</Text>
                </View>
              </Card>
            ))
          )}
        </View>
      )}

      {selectedRecord && (
        <View className={styles.modalOverlay} onClick={() => setSelectedRecord(null)}>
          <View className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <View className={styles.modalHeader}>
              <Text className={styles.modalTitle}>体测报告详情</Text>
              <Text className={styles.modalClose} onClick={() => setSelectedRecord(null)}>×</Text>
            </View>
            <View className={styles.modalBody}>
              <View className={styles.reportInfo}>
                <View className={styles.reportRow}>
                  <Text className={styles.reportLabel}>报告编号</Text>
                  <Text className={styles.reportValue}>{selectedRecord.recordNo}</Text>
                </View>
                <View className={styles.reportRow}>
                  <Text className={styles.reportLabel}>测试日期</Text>
                  <Text className={styles.reportValue}>{formatDate(selectedRecord.testDate)}</Text>
                </View>
                <View className={styles.reportRow}>
                  <Text className={styles.reportLabel}>总分</Text>
                  <Text className={styles.reportValue}>{selectedRecord.totalScore}分</Text>
                </View>
                <View className={styles.reportRow}>
                  <Text className={styles.reportLabel}>评级</Text>
                  <Text className={styles.reportValue}>{getLevelText(selectedRecord.overallLevel)}</Text>
                </View>
                <View className={styles.scoreSection}>
                  <Text className={styles.sectionTitle}>测试项目</Text>
                  <View className={styles.scoreItems}>
                    <View className={styles.scoreItem}>
                      <Text className={styles.scoreName}>身高</Text>
                      <Text className={styles.scoreValue}>156cm</Text>
                      <Text className={styles.scoreLevel}>优秀</Text>
                    </View>
                    <View className={styles.scoreItem}>
                      <Text className={styles.scoreName}>体重</Text>
                      <Text className={styles.scoreValue}>45kg</Text>
                      <Text className={styles.scoreLevel}>良好</Text>
                    </View>
                    <View className={styles.scoreItem}>
                      <Text className={styles.scoreName}>50米跑</Text>
                      <Text className={styles.scoreValue}>8.2秒</Text>
                      <Text className={styles.scoreLevel}>优秀</Text>
                    </View>
                    <View className={styles.scoreItem}>
                      <Text className={styles.scoreName}>立定跳远</Text>
                      <Text className={styles.scoreValue}>185cm</Text>
                      <Text className={styles.scoreLevel}>良好</Text>
                    </View>
                    <View className={styles.scoreItem}>
                      <Text className={styles.scoreName}>肺活量</Text>
                      <Text className={styles.scoreValue}>3200ml</Text>
                      <Text className={styles.scoreLevel}>及格</Text>
                    </View>
                  </View>
                </View>
              </View>
            </View>
            <View className={styles.modalFooter}>
              <CustomButton type="primary" onClick={handleDownloadReport}>下载报告</CustomButton>
            </View>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
