import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import { PhysicalTestRecord, ParentProfile, Student } from '@/types';
import { getPhysicalTests, getParentStudents } from '@/services/api';
import { formatDate } from '@/utils/format';
import useAuthStore from '@/store/auth';
import styles from './index.module.scss';

export default function ParentPhysicalPage() {
  const [records, setRecords] = useState<PhysicalTestRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showReportModal, setShowReportModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<PhysicalTestRecord | null>(null);
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
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
      const students = await getParentStudents();
      setStudents(students || []);
      if (students && students.length > 0) {
        setSelectedStudent(students[0]);
      }
    } catch (err) {
      console.error('[ParentPhysicalPage]', err);
      if (parentProfile?.students && parentProfile.students.length > 0) {
        setStudents(parentProfile.students);
        setSelectedStudent(parentProfile.students[0]);
      }
    }
  };

  const fetchRecords = async (studentId: string) => {
    setIsLoading(true);
    try {
      const data = await getPhysicalTests({ studentId });
      setRecords(data);
    } catch (err) {
      console.error('[ParentPhysicalPage]', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRecordClick = (record: PhysicalTestRecord) => {
    setSelectedRecord(record);
    setShowReportModal(true);
  };

  const handleDownload = () => {
    Taro.showToast({ title: '报告下载中', icon: 'loading' });
    setTimeout(() => {
      Taro.showToast({ title: '下载成功', icon: 'success' });
    }, 1500);
  };

  return (
    <ScrollView className={styles.physicalPage} scrollY>
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
                    <Text className={styles.scoreValue}>{score.score}</Text>
                    <Text className={styles.scoreUnit}>{score.unit}</Text>
                  </View>
                ))}
              </View>

              <View className={styles.reportSection}>
                <Text className={styles.reportSectionTitle}>评估结果</Text>
                <View className={styles.evaluation}>
                  <Text className={styles.evaluationText}>
                    {selectedRecord.evaluation || '身体状况良好，继续保持锻炼！'}
                  </Text>
                </View>
              </View>
            </ScrollView>

            <View className={styles.modalFooter}>
              <Text className={styles.downloadBtn} onClick={handleDownload}>
                下载报告
              </Text>
            </View>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
