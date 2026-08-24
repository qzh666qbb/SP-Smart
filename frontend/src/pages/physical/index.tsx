import { useState, useEffect } from 'react';
import { View, Text, ScrollView, Image } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Student, PhysicalTestRecord } from '@/types';
import { formatDate } from '@/utils/format';
import {
  getTeacherStudents,
  getTeacherPhysicalTests,
} from '@/services/api';
import { notifyTabBarActive } from '@/utils/tabbar';
import styles from './index.module.scss';

export default function PhysicalPage() {
  Taro.useDidShow(() => notifyTabBarActive('/pages/physical/index'));
  const [activeTab, setActiveTab] = useState<'import' | 'export'>('import');
  const [selectedStudents, setSelectedStudents] = useState<string[]>([]);
  const [showExportModal, setShowExportModal] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<PhysicalTestRecord | null>(null);
  const [students, setStudents] = useState<Student[]>([]);
  const [records, setRecords] = useState<PhysicalTestRecord[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    loadInitial();
  }, []);

  const loadInitial = async () => {
    setIsLoading(true);
    try {
      const [sRes, rRes] = await Promise.all([
        getTeacherStudents({ pageNum: 1, pageSize: 200 }),
        getTeacherPhysicalTests({ pageNum: 1, pageSize: 50 }),
      ]);
      setStudents(sRes.students || []);
      setRecords(rRes.records || []);
    } catch (err) {
      console.error('[PhysicalPage.loadInitial]', err);
      Taro.showToast({ title: '数据加载失败', icon: 'none' });
    } finally {
      setIsLoading(false);
    }
  };

  const getStudentNameById = (id: string) => students.find(s => s.id === id)?.name || '未知学生';

  const toggleStudent = (studentId: string) => {
    if (selectedStudents.includes(studentId)) {
      setSelectedStudents(selectedStudents.filter(id => id !== studentId));
    } else {
      setSelectedStudents([...selectedStudents, studentId]);
    }
  };

  const selectAllStudents = () => {
    if (selectedStudents.length === students.length) {
      setSelectedStudents([]);
    } else {
      setSelectedStudents(students.map(s => s.id));
    }
  };

  const handleImportClick = () => {
    Taro.navigateTo({ url: '/pages/physical-add/index' });
  };

  const handleBatchImport = () => {
    Taro.showToast({ title: '请选择表格文件', icon: 'none' });
  };

  const handleExportPDF = () => {
    Taro.showToast({ title: '生成PDF中...', icon: 'loading' });
    setTimeout(() => {
      Taro.showToast({ title: 'PDF导出成功', icon: 'success' });
    }, 1500);
  };

  const handleBatchExport = () => {
    if (selectedStudents.length === 0) {
      Taro.showToast({ title: '请选择学生', icon: 'none' });
      return;
    }
    Taro.showToast({ title: `正在导出 ${selectedStudents.length} 份报告...`, icon: 'loading' });
    setTimeout(() => {
      Taro.showToast({ title: '批量导出成功', icon: 'success' });
      setShowExportModal(false);
      setSelectedStudents([]);
    }, 2000);
  };

  const handleViewReport = (record: PhysicalTestRecord) => {
    setSelectedRecord(record);
    setShowReportModal(true);
  };

  const handleDownloadReport = () => {
    Taro.showToast({ title: '报告下载中', icon: 'loading' });
    setTimeout(() => {
      Taro.showToast({ title: '下载成功', icon: 'success' });
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

  const renderModalStudents = () => {
    if (students.length === 0) {
      return (
        <View className={styles.studentItem}>
          <Text className={styles.studentName}>暂无学员</Text>
        </View>
      );
    }
    return students.map(student => (
      <View key={student.id} className={styles.studentItem}>
        <View
          className={`${styles.checkbox} ${selectedStudents.includes(student.id) ? styles.checked : ''}`}
          onClick={() => toggleStudent(student.id)}
        />
        <View className={styles.studentInfo}>
          <Text className={styles.studentName}>{student.name}</Text>
          <Text className={styles.studentNo}>{student.studentNo || ('ID: ' + student.id)}</Text>
        </View>
        {student.gender === 'MALE' && <Tag type="primary">男</Tag>}
        {student.gender === 'FEMALE' && <Tag type="success">女</Tag>}
      </View>
    ));
  };

  return (
    <ScrollView className={styles.physicalPage} scrollY>
      <View className={styles.tabHeader}>
        <View
          className={`${styles.tabItem} ${activeTab === 'import' ? styles.active : ''}`}
          onClick={() => setActiveTab('import')}
        >
          <Text className={styles.tabText}>体测导入</Text>
        </View>
        <View
          className={`${styles.tabItem} ${activeTab === 'export' ? styles.active : ''}`}
          onClick={() => setActiveTab('export')}
        >
          <Text className={styles.tabText}>报告导出</Text>
        </View>
      </View>

      {activeTab === 'import' && (
        <View className={styles.container}>
          <Card className={styles.importCard}>
            <View className={styles.importHeader}>
              <Image className={styles.importIcon} src="https://picsum.photos/id/2/100/100" mode="aspectFill" />
              <View className={styles.importInfo}>
                <Text className={styles.importTitle}>体测数据导入</Text>
                <Text className={styles.importDesc}>支持手动录入和表格导入</Text>
              </View>
            </View>

            <View className={styles.importActions}>
              <CustomButton type="primary" size="lg" onClick={handleImportClick}>
                手动录入
              </CustomButton>
              <CustomButton type="secondary" size="lg" onClick={handleBatchImport}>
                导入表格
              </CustomButton>
            </View>
          </Card>

          <View className={styles.section}>
            <Text className={styles.sectionTitle}>最近录入记录 {isLoading ? '(加载中...)' : `(共${records.length}条)`}</Text>

            {!isLoading && records.length === 0 ? (
              <Card className={styles.emptyCard}>
                <Text className={styles.emptyText}>暂无录入记录</Text>
              </Card>
            ) : (
              records.map(record => {
                const level = (record as any).overallLevel || (record as any).scoreLevel;
                return (
                  <Card key={record.id} className={styles.recordCard} onClick={() => handleViewReport(record)}>
                    <View className={styles.recordHeader}>
                      <View className={styles.recordInfo}>
                        <Text className={styles.recordStudent}>{getStudentNameById(record.studentId)}</Text>
                        <Text className={styles.recordDate}>{formatDate(record.testDate)}</Text>
                      </View>
                      <View className={styles.recordScore}>
                        <Text className={styles.score}>{record.totalScore ?? '-'}</Text>
                        {level ? getLevelTag(level) : null}
                      </View>
                    </View>
                    <View className={styles.recordArrow}>
                      <Text className={styles.arrowText}>查看报告</Text>
                      <Text className={styles.arrow}>›</Text>
                    </View>
                  </Card>
                );
              })
            )}
          </View>
        </View>
      )}

      {activeTab === 'export' && (
        <View className={styles.container}>
          <Card className={styles.exportCard}>
            <Text className={styles.exportTitle}>学生体测报告</Text>
            <Text className={styles.exportDesc}>选择学生导出PDF报告，支持批量导出</Text>
            <CustomButton type="primary" size="lg" onClick={() => setShowExportModal(true)}>
              批量导出
            </CustomButton>
          </Card>

          <View className={styles.section}>
            <Text className={styles.sectionTitle}>学生列表（共{students.length}）</Text>

            {students.length === 0 ? (
              <Card className={styles.emptyCard}>
                <Text className={styles.emptyText}>暂无学员</Text>
              </Card>
            ) : (
              students.map(student => {
                const latestRecord = records.slice().sort((a,b) => (b.testDate||'').localeCompare(a.testDate||'')).find(r => r.studentId === student.id)
                  || records.find(r => r.studentId === student.id);
                const level = latestRecord ? ((latestRecord as any).overallLevel || (latestRecord as any).scoreLevel) : null;
                return (
                  <Card key={student.id} className={styles.studentCard}>
                    <View className={styles.studentHeader}>
                      <View className={styles.studentInfo}>
                        <Text className={styles.studentName}>{student.name}</Text>
                        <Text className={styles.studentNo}>{student.studentNo || ('ID: ' + student.id)}</Text>
                      </View>
                      {student.gender === 'MALE' && <Tag type="primary">男</Tag>}
                      {student.gender === 'FEMALE' && <Tag type="success">女</Tag>}
                    </View>

                    {latestRecord && (
                      <View className={styles.studentRecord}>
                        <Text className={styles.recordLabel}>最近成绩</Text>
                        <View style={{ display: 'flex', alignItems: 'center' }}>
                          <Text className={styles.recordValue}>{latestRecord.totalScore ?? '-'}分</Text>
                          {level ? <View style={{ marginLeft: 8 }}>{getLevelTag(level)}</View> : null}
                        </View>
                      </View>
                    )}

                    <CustomButton type="secondary" size="sm" onClick={handleExportPDF}>
                      导出PDF
                    </CustomButton>
                  </Card>
                );
              })
            )}
          </View>
        </View>
      )}

      {showExportModal && (
        <View className={styles.modalOverlay} onClick={() => setShowExportModal(false)}>
          <View className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <View className={styles.modalHeader}>
              <Text className={styles.modalTitle}>选择导出学生</Text>
              <Text className={styles.modalClose} onClick={() => setShowExportModal(false)}>×</Text>
            </View>
            <View className={styles.modalBody}>
              <View className={styles.selectAll} onClick={selectAllStudents}>
                <View className={`${styles.checkbox} ${selectedStudents.length === students.length && students.length > 0 ? styles.checked : ''}`} />
                <Text className={styles.selectAllText}>全选</Text>
              </View>
              <ScrollView className={styles.studentList} scrollY>
                {renderModalStudents()}
              </ScrollView>
            </View>
            <View className={styles.modalFooter}>
              <CustomButton type="secondary" onClick={() => setShowExportModal(false)}>取消</CustomButton>
              <CustomButton type="primary" onClick={handleBatchExport}>
                导出 {selectedStudents.length} 份
              </CustomButton>
            </View>
          </View>
        </View>
      )}

      {showReportModal && selectedRecord && (
        <View className={styles.reportOverlay} onClick={() => setShowReportModal(false)}>
          <View className={styles.reportContent} onClick={(e) => e.stopPropagation()}>
            <View className={styles.reportHeader}>
              <View className={styles.reportTitleWrap}>
                <Text className={styles.reportTitle}>体测报告</Text>
                <Text className={styles.reportSubtitle}>{formatDate(selectedRecord.testDate)}</Text>
              </View>
              <Text className={styles.reportClose} onClick={() => setShowReportModal(false)}>×</Text>
            </View>

            <ScrollView className={styles.reportBody} scrollY>
              <View className={styles.reportSummary}>
                <View className={styles.totalScoreCard}>
                  <Text className={styles.totalScore}>{selectedRecord.totalScore ?? '-'}</Text>
                  <Text className={styles.totalLabel}>综合得分</Text>
                </View>
                <View className={styles.studentInfoRow}>
                  <Text className={styles.studentInfoLabel}>测试学员</Text>
                  <Text className={styles.studentInfoValue}>{getStudentNameById(selectedRecord.studentId)}</Text>
                </View>
                <View className={styles.studentInfoRow}>
                  <Text className={styles.studentInfoLabel}>记录状态</Text>
                  <Text className={styles.studentInfoValue}>{selectedRecord.status || 'DRAFT'}</Text>
                </View>
              </View>

              <View className={styles.reportSection}>
                <Text className={styles.reportSectionTitle}>各项指标</Text>
                {((selectedRecord as any).scores || (selectedRecord as any).details || []).map((score: any, index: number) => (
                  <View key={index} className={styles.scoreItem}>
                    <Text className={styles.scoreName}>{score.itemName || score.name || ('项目' + (index + 1))}</Text>
                    <Text className={styles.scoreValue}>{score.rawValue ?? score.value ?? '-'}</Text>
                    {score.unit && <Text className={styles.scoreUnit}>{score.unit}</Text>}
                    {score.level || score.convertedLevel ? getLevelTag(score.level || score.convertedLevel) : null}
                  </View>
                ))}
                {(((selectedRecord as any).scores || (selectedRecord as any).details || []).length === 0) && (
                  <Text className={styles.studentInfoValue}>暂无分项详情</Text>
                )}
              </View>

              <View className={styles.reportSection}>
                <Text className={styles.reportSectionTitle}>评估结果</Text>
                <View className={styles.evaluation}>
                  <Text className={styles.evaluationText}>
                    {(selectedRecord as any).evaluation || (selectedRecord as any).remark || '暂无评估结果'}
                  </Text>
                </View>
              </View>
            </ScrollView>

            <View className={styles.reportFooter}>
              <Text className={styles.downloadBtn} onClick={handleDownloadReport}>下载报告</Text>
            </View>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
