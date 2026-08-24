import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Student, PhysicalTestRecord, Lesson } from '@/types';
import { formatDate, getLevelText } from '@/utils/format';
import {
  getStudentById,
  getPhysicalTests,
  getLessons,
} from '@/services/api';
import styles from './index.module.scss';

export default function StudentDetailPage() {
  const [student, setStudent] = useState<Student | null>(null);
  const [records, setRecords] = useState<PhysicalTestRecord[]>([]);
  const [courses, setCourses] = useState<Lesson[]>([]);
  const [activeTab, setActiveTab] = useState<'physical' | 'courses'>('physical');
  const [selectedRecord, setSelectedRecord] = useState<PhysicalTestRecord | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [studentId, setStudentId] = useState<string | null>(null);

  useEffect(() => {
    const opts = Taro.getCurrentInstance()?.router?.params;
    const sid = String(opts?.studentId || opts?.id || '');
    if (!sid) {
      Taro.showToast({ title: '缺少学员ID', icon: 'none' });
      return;
    }
    setStudentId(sid);
    fetchData(sid);
  }, []);

  const fetchData = async (sid: string) => {
    setIsLoading(true);
    try {
      const [stu, recs, lsns] = await Promise.all([
        getStudentById(sid).catch(() => null as any),
        getPhysicalTests({ studentId: sid }).catch(() => [] as any),
        getLessons({ studentId: sid }).catch(() => [] as any),
      ]);
      if (stu) setStudent(stu);
      setRecords(recs || []);
      setCourses(lsns || []);
    } catch (err) {
      console.error('[StudentDetailPage.fetchData]', err);
      Taro.showToast({ title: '数据加载失败', icon: 'none' });
    } finally {
      setIsLoading(false);
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

  if (!student && isLoading) {
    return (
      <View className={styles.container}>
        <Text>加载中...</Text>
      </View>
    );
  }

  if (!student) {
    return (
      <View className={styles.container}>
        <Text>找不到该学员</Text>
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
          <Text className={styles.infoValue}>{student.studentNo || ('ID: ' + student.id)}</Text>
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
          <Text className={styles.tabText}>体测记录（共{records.length}）</Text>
        </View>
        <View
          className={`${styles.tabItem} ${activeTab === 'courses' ? styles.active : ''}`}
          onClick={() => setActiveTab('courses')}
        >
          <Text className={styles.tabText}>课程记录（共{courses.length}）</Text>
        </View>
      </View>

      {activeTab === 'physical' && (
        <View className={styles.content}>
          {records.length === 0 ? (
            <Card className={styles.emptyCard}>
              <Text className={styles.emptyText}>暂无体测记录</Text>
            </Card>
          ) : (
            records.map(record => {
              const level = (record as any).overallLevel || (record as any).scoreLevel;
              return (
                <Card key={record.id} className={styles.recordCard} onClick={() => handleRecordClick(record)}>
                  <View className={styles.recordHeader}>
                    <View className={styles.recordInfo}>
                      <Text className={styles.recordNo}>{record.recordNo || `R-${record.id}`}</Text>
                      <Text className={styles.recordDate}>{formatDate(record.testDate)}</Text>
                    </View>
                    <View className={styles.recordScore}>
                      <Text className={styles.score}>{record.totalScore ?? '-'}</Text>
                      {level ? getLevelTag(level) : null}
                    </View>
                  </View>
                </Card>
              );
            })
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
                  <Text className={styles.courseNo}>课程编号: {course.lessonNo || `ID:${course.id}`}</Text>
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
                  <Text className={styles.reportValue}>{selectedRecord.recordNo || `R-${selectedRecord.id}`}</Text>
                </View>
                <View className={styles.reportRow}>
                  <Text className={styles.reportLabel}>测试日期</Text>
                  <Text className={styles.reportValue}>{formatDate(selectedRecord.testDate)}</Text>
                </View>
                <View className={styles.reportRow}>
                  <Text className={styles.reportLabel}>总分</Text>
                  <Text className={styles.reportValue}>{selectedRecord.totalScore ?? '-'}分</Text>
                </View>
                <View className={styles.reportRow}>
                  <Text className={styles.reportLabel}>评级</Text>
                  <Text className={styles.reportValue}>
                    {getLevelText((selectedRecord as any).overallLevel || (selectedRecord as any).scoreLevel)}
                  </Text>
                </View>
                <View className={styles.scoreSection}>
                  <Text className={styles.sectionTitle}>测试项目</Text>
                  {((selectedRecord as any).scores || (selectedRecord as any).details || []).map((s: any, idx: number) => (
                    <View key={idx} className={styles.scoreItem}>
                      <Text className={styles.scoreName}>{s.itemName || s.name || '项目' + (idx + 1)}</Text>
                      <Text className={styles.scoreValue}>{s.rawValue ?? s.value ?? '-'}{s.unit || ''}</Text>
                      {(s.level || s.convertedLevel) && <Tag type="info">{s.level || s.convertedLevel}</Tag>}
                    </View>
                  ))}
                  {(((selectedRecord as any).scores || (selectedRecord as any).details || []).length === 0) && (
                    <Text className={styles.infoValue}>暂无分项详情</Text>
                  )}
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
