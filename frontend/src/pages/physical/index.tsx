import { useEffect, useRef, useState } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Student, PhysicalTestRecord } from '@/types';
import { formatDate } from '@/utils/format';
import { getAllTeacherStudents, getAllTeacherPhysicalTests } from '@/services/api';
import { notifyTabBarActive } from '@/utils/tabbar';
import BottomNav from '@/components/BottomNav';
import styles from './index.module.scss';

export default function PhysicalPage() {
  const [activeTab, setActiveTab] = useState<'records' | 'students'>('records');
  const [showReportModal, setShowReportModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<PhysicalTestRecord | null>(null);
  const [students, setStudents] = useState<Student[]>([]);
  const [records, setRecords] = useState<PhysicalTestRecord[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState('');
  const requestId = useRef(0);

  const loadInitial = async () => {
    const id = ++requestId.current;
    const isCurrent = () => requestId.current === id;
    setIsLoading(true);
    setLoadError('');
    try {
      const [studentResult, recordResult] = await Promise.all([
        getAllTeacherStudents(undefined, isCurrent),
        getAllTeacherPhysicalTests(isCurrent),
      ]);
      if (!isCurrent()) return;
      setStudents(studentResult);
      setRecords(recordResult);
    } catch (error) {
      if (!isCurrent()) return;
      console.error('[PhysicalPage.loadInitial]', error);
      setLoadError('数据加载失败，请重试');
      Taro.showToast({ title: '数据加载失败', icon: 'none' });
    } finally {
      if (isCurrent()) setIsLoading(false);
    }
  };

  Taro.useDidShow(() => {
    notifyTabBarActive('/pages/physical/index');
    loadInitial();
  });
  useEffect(() => () => { requestId.current++; }, []);

  const getStudentNameById = (id: string) => students.find((student) => student.id === id)?.name || '未知学生';
  const getLevelTag = (level?: string) => {
    switch (level) {
      case 'EXCELLENT': return <Tag type="success">优秀</Tag>;
      case 'GOOD': return <Tag type="primary">良好</Tag>;
      case 'PASS': return <Tag type="info">及格</Tag>;
      case 'FAIL': return <Tag type="danger">不及格</Tag>;
      default: return null;
    }
  };
  const openReport = (record: PhysicalTestRecord) => {
    setSelectedRecord(record);
    setShowReportModal(true);
  };
  const latestRecordFor = (studentId: string) => records
    .filter((record) => record.studentId === studentId)
    .sort((a, b) => (b.testDate || '').localeCompare(a.testDate || ''))[0];

  return (
    <View className={styles.physicalPage}>
      <View className={styles.tabHeader}>
        <View className={`${styles.tabItem} ${activeTab === 'records' ? styles.active : ''}`} onClick={() => setActiveTab('records')}>
          <Text className={styles.tabText}>体测记录</Text>
        </View>
        <View className={`${styles.tabItem} ${activeTab === 'students' ? styles.active : ''}`} onClick={() => setActiveTab('students')}>
          <Text className={styles.tabText}>学员报告</Text>
        </View>
      </View>

      <View className={styles.container}>
        {loadError && (
          <Card className={styles.emptyCard}>
            <Text className={styles.emptyText}>{loadError}</Text>
            <CustomButton type="secondary" onClick={loadInitial} disabled={isLoading}>重新加载</CustomButton>
          </Card>
        )}
        {activeTab === 'records' && (
          <>
            <Card className={styles.importCard}>
              <View className={styles.importInfo}>
                <Text className={styles.importTitle}>录入体测数据</Text>
                <Text className={styles.importDesc}>选择学员并填写本次各项体测成绩</Text>
              </View>
              <CustomButton type="primary" size="lg" onClick={() => Taro.navigateTo({ url: '/pages/physical-add/index' })}>新增体测记录</CustomButton>
            </Card>
            <View className={styles.section}>
              <Text className={styles.sectionTitle}>最近记录 {isLoading ? '（加载中…）' : `（共 ${records.length} 条）`}</Text>
              {!isLoading && records.length === 0 ? (
                <Card className={styles.emptyCard}><Text className={styles.emptyText}>暂无体测记录</Text></Card>
              ) : records.map((record) => {
                const level = (record as any).overallLevel || record.scoreLevel;
                return (
                  <Card key={record.id} className={styles.recordCard} onClick={() => openReport(record)}>
                    <View className={styles.recordHeader}>
                      <View className={styles.recordInfo}>
                        <Text className={styles.recordStudent}>{getStudentNameById(record.studentId)}</Text>
                        <Text className={styles.recordDate}>{formatDate(record.testDate)}</Text>
                      </View>
                      <View className={styles.recordScore}><Text className={styles.score}>{record.totalScore ?? '-'}</Text>{getLevelTag(level)}</View>
                    </View>
                    <View className={styles.recordArrow}><Text className={styles.arrowText}>查看报告</Text><Text className={styles.arrow}>›</Text></View>
                  </Card>
                );
              })}
            </View>
          </>
        )}

        {activeTab === 'students' && (
          <View className={styles.section}>
            <Text className={styles.sectionTitle}>学员列表（共 {students.length} 名）</Text>
            {students.length === 0 ? (
              <Card className={styles.emptyCard}><Text className={styles.emptyText}>暂无学员</Text></Card>
            ) : students.map((student) => {
              const latest = latestRecordFor(student.id);
              const level = latest ? ((latest as any).overallLevel || latest.scoreLevel) : undefined;
              return (
                <Card key={student.id} className={styles.studentCard} onClick={() => latest && openReport(latest)}>
                  <View className={styles.studentHeader}>
                    <View className={styles.studentInfo}><Text className={styles.studentName}>{student.name}</Text><Text className={styles.studentNo}>{student.studentNo || `ID: ${student.id}`}</Text></View>
                    {latest ? getLevelTag(level) : <Tag type="info">暂无体测</Tag>}
                  </View>
                  {latest && <View className={styles.studentRecord}><Text className={styles.recordLabel}>最近成绩 · {formatDate(latest.testDate)}</Text><Text className={styles.recordValue}>{latest.totalScore ?? '-'} 分 ›</Text></View>}
                </Card>
              );
            })}
          </View>
        )}
      </View>

      {showReportModal && selectedRecord && (
        <View className={styles.reportOverlay} onClick={() => setShowReportModal(false)}>
          <View className={styles.reportContent} onClick={(event) => event.stopPropagation()}>
            <View className={styles.reportHeader}>
              <View className={styles.reportTitleWrap}><Text className={styles.reportTitle}>体测报告</Text><Text className={styles.reportSubtitle}>{formatDate(selectedRecord.testDate)}</Text></View>
              <Text className={styles.reportClose} onClick={() => setShowReportModal(false)}>×</Text>
            </View>
            <ScrollView className={styles.reportBody} scrollY>
              <View className={styles.reportBodyInner}>
                <View className={styles.reportSummary}>
                  <View className={styles.totalScoreCard}><Text className={styles.totalScore}>{selectedRecord.totalScore ?? '-'}</Text><Text className={styles.totalLabel}>综合得分</Text></View>
                  <View className={styles.studentInfoRow}><Text className={styles.studentInfoLabel}>测试学员</Text><Text className={styles.studentInfoValue}>{getStudentNameById(selectedRecord.studentId)}</Text></View>
                  <View className={styles.studentInfoRow}><Text className={styles.studentInfoLabel}>记录状态</Text><Text className={styles.studentInfoValue}>{selectedRecord.status || 'DRAFT'}</Text></View>
                </View>
                <View className={styles.reportSection}>
                  <Text className={styles.reportSectionTitle}>各项指标</Text>
                  {((selectedRecord as any).scores || selectedRecord.details || []).map((score: any, index: number) => (
                    <View key={index} className={styles.scoreItem}>
                      <Text className={styles.scoreName}>{score.itemName || score.name || `项目${index + 1}`}</Text>
                      <Text className={styles.scoreValue}>{score.rawValue ?? score.value ?? '-'}</Text>
                      {score.unit && <Text className={styles.scoreUnit}>{score.unit}</Text>}
                      {getLevelTag(score.level || score.convertedLevel)}
                    </View>
                  ))}
                  {(((selectedRecord as any).scores || selectedRecord.details || []).length === 0) && <Text className={styles.studentInfoValue}>暂无分项详情</Text>}
                </View>
                <View className={styles.reportSection}><Text className={styles.reportSectionTitle}>评估结果</Text><View className={styles.evaluation}><Text className={styles.evaluationText}>{(selectedRecord as any).evaluation || (selectedRecord as any).remark || '暂无评估结果'}</Text></View></View>
              </View>
            </ScrollView>
            <View className={styles.reportFooter}><Text className={styles.downloadBtn} onClick={() => setShowReportModal(false)}>关闭</Text></View>
          </View>
        </View>
      )}
      <BottomNav />
    </View>
  );
}
