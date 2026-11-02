import { useState, useEffect, useRef } from 'react';
import { View, Text, ScrollView, Picker } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import { PhysicalTestRecord, ParentProfile, Student } from '@/types';
import { getPhysicalTests, getParentStudents } from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import { notifyTabBarActive } from '@/utils/tabbar';
import styles from './index.module.scss';
import BottomNav from '@/components/BottomNav';

export default function ParentPhysicalPage() {
  Taro.useDidShow(() => notifyTabBarActive('/pages/parent-physical/index'));
  const [records, setRecords] = useState<PhysicalTestRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showReportModal, setShowReportModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<PhysicalTestRecord | null>(null);
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const recordsRequestId = useRef(0);
  const { profile } = useAuthStore();
  const parentProfile = profile as ParentProfile;

  useEffect(() => {
    fetchStudents();
  }, []);

  useEffect(() => {
    if (selectedStudent) {
      fetchRecords(selectedStudent.id);
    }
  }, [selectedStudent]);

  const fetchStudents = async () => {
    try {
      const nextStudents = await getParentStudents();
      setStudents(nextStudents || []);
      if (nextStudents && nextStudents.length > 0) {
        setSelectedStudent(nextStudents[0]);
      } else {
        recordsRequestId.current += 1;
        setSelectedStudent(null);
        setRecords([]);
        setIsLoading(false);
      }
    } catch (err) {
      console.error('[ParentPhysicalPage]', err);
      if (parentProfile?.students && parentProfile.students.length > 0) {
        setStudents(parentProfile.students);
        setSelectedStudent(parentProfile.students[0]);
      } else {
        setIsLoading(false);
      }
    }
  };

  const fetchRecords = async (studentId: string) => {
    const requestId = ++recordsRequestId.current;
    setIsLoading(true);
    try {
      const data = await getPhysicalTests({ studentId });
      if (requestId === recordsRequestId.current) setRecords(data);
    } catch (err) {
      console.error('[ParentPhysicalPage]', err);
    } finally {
      if (requestId === recordsRequestId.current) setIsLoading(false);
    }
  };

  const handleRecordClick = (record: PhysicalTestRecord) => {
    setSelectedRecord(record);
    setShowReportModal(true);
  };

  const handleStudentChange = (e: any) => {
    const student = students[Number(e.detail.value)];
    if (student) {
      setRecords([]);
      setSelectedStudent(student);
    }
  };

  return (
    <View className={styles.physicalPage}>
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
          <Text className={styles.summaryLabel}>体测成绩</Text>
        </View>
        <View className={styles.summaryStats}>
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{records.length}</Text>
            <Text className={styles.statLabel}>测试次数</Text>
          </View>
          <View className={styles.statDivider} />
          <View className={styles.statItem}>
            <Text className={styles.statValue}>{records.length > 0 ? records[0].totalScore : '--'}</Text>
            <Text className={styles.statLabel}>最近得分</Text>
          </View>
        </View>
      </Card>

      <View className={styles.section}>
        <Text className={styles.sectionTitle}>测试记录</Text>

        {isLoading ? (
          <View className={styles.loading}>
            <Text>加载中...</Text>
          </View>
        ) : records.length === 0 ? (
          <Card className={styles.emptyCard}>
            <Text className={styles.emptyText}>暂无体测记录</Text>
          </Card>
        ) : (
          records.map(record => (
            <Card 
              key={record.id} 
              className={styles.recordCard}
              onClick={() => handleRecordClick(record)}
            >
              <View className={styles.recordHeader}>
                <View className={styles.recordInfo}>
                  <Text className={styles.recordDate}>{formatDate(record.testDate)}</Text>
                  <Text className={styles.recordTeacher}>测试老师: {record.teacherName}</Text>
                </View>
                <View className={styles.recordScore}>
                  <Text className={styles.score}>{record.totalScore}</Text>
                  <Text className={styles.scoreLabel}>总分</Text>
                </View>
              </View>
              <View className={styles.recordArrow}>
                <Text className={styles.arrowText}>查看报告</Text>
                <Text className={styles.arrow}>›</Text>
              </View>
            </Card>
          ))
        )}
      </View>

      {showReportModal && selectedRecord && (
        <View className={styles.modalOverlay} onClick={() => setShowReportModal(false)}>
          <View className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <View className={styles.modalHeader}>
              <View className={styles.modalTitleWrap}>
                <Text className={styles.modalTitle}>体测报告</Text>
                <Text className={styles.modalSubtitle}>{formatDate(selectedRecord.testDate)}</Text>
              </View>
              <Text className={styles.modalClose} onClick={() => setShowReportModal(false)}>×</Text>
            </View>

            <ScrollView className={styles.modalBody} scrollY>
              <View className={styles.modalBodyInner}>
              <View className={styles.reportSummary}>
                <View className={styles.totalScoreCard}>
                  <Text className={styles.totalScore}>{selectedRecord.totalScore}</Text>
                  <Text className={styles.totalLabel}>综合得分</Text>
                </View>
              </View>

              <View className={styles.reportSection}>
                <Text className={styles.reportSectionTitle}>各项指标</Text>
                {(selectedRecord.scores || []).map((score, index) => (
                  <View key={index} className={styles.scoreItem}>
                    <Text className={styles.scoreName}>{score.itemName}</Text>
                    <Text className={styles.scoreValue}>{score.score ?? '暂无数据'}</Text>
                    <Text className={styles.scoreUnit}>{score.unit}</Text>
                  </View>
                ))}
              </View>

              <View className={styles.reportSection}>
                <Text className={styles.reportSectionTitle}>评估结果</Text>
                <View className={styles.evaluation}>
                  <Text className={styles.evaluationText}>
                    {selectedRecord.evaluation || selectedRecord.remark || '暂无评估建议'}
                  </Text>
                </View>
              </View>
              </View>
            </ScrollView>

            <View className={styles.modalFooter}>
              <Text className={styles.downloadBtn} onClick={() => setShowReportModal(false)}>
                关闭
              </Text>
            </View>
          </View>
        </View>
      )}
      <BottomNav />
    </View>
  );
}
