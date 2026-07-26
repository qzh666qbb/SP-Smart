import { useState } from 'react';
import { View, Text, ScrollView, Image } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Student, PhysicalTestRecord } from '@/types';
import { formatDate } from '@/utils/format';
import styles from './index.module.scss';

const mockStudents: Student[] = [
  { id: '1', name: '张三', age: 12, gender: 'MALE', studentNo: 'S001', parentName: '张爸爸', parentPhone: '13800138001' },
  { id: '2', name: '李四', age: 12, gender: 'FEMALE', studentNo: 'S002', parentName: '李妈妈', parentPhone: '13800138002' },
  { id: '3', name: '王五', age: 13, gender: 'MALE', studentNo: 'S003', parentName: '王爸爸', parentPhone: '13800138003' },
  { id: '4', name: '赵六', age: 12, gender: 'FEMALE', studentNo: 'S004', parentName: '赵妈妈', parentPhone: '13800138004' },
  { id: '5', name: '钱七', age: 13, gender: 'MALE', studentNo: 'S005', parentName: '钱爸爸', parentPhone: '13800138005' },
  { id: '6', name: '孙八', age: 12, gender: 'FEMALE', studentNo: 'S006', parentName: '孙妈妈', parentPhone: '13800138006' },
];

const mockRecords: PhysicalTestRecord[] = [
  { 
    id: '1', 
    recordNo: 'R001', 
    studentId: '1', 
    testerId: 'T001', 
    teacherName: '张老师',
    testDate: '2024-01-15', 
    totalScore: 85, 
    overallLevel: 'EXCELLENT', 
    status: 'CONFIRMED',
    scores: [
      { itemId: '1', itemName: '身高', unit: 'cm', rawValue: 158, convertedScore: 90, level: 'EXCELLENT' },
      { itemId: '2', itemName: '体重', unit: 'kg', rawValue: 45, convertedScore: 85, level: 'GOOD' },
      { itemId: '3', itemName: '50米跑', unit: '秒', rawValue: 8.2, convertedScore: 80, level: 'GOOD' },
      { itemId: '4', itemName: '立定跳远', unit: 'cm', rawValue: 185, convertedScore: 85, level: 'GOOD' },
      { itemId: '5', itemName: '坐位体前屈', unit: 'cm', rawValue: 15, convertedScore: 90, level: 'EXCELLENT' },
    ],
    evaluation: '身体状况优秀，继续保持锻炼！'
  },
  { 
    id: '2', 
    recordNo: 'R002', 
    studentId: '2', 
    testerId: 'T001', 
    teacherName: '张老师',
    testDate: '2024-01-15', 
    totalScore: 72, 
    overallLevel: 'GOOD', 
    status: 'CONFIRMED',
    scores: [
      { itemId: '1', itemName: '身高', unit: 'cm', rawValue: 155, convertedScore: 85, level: 'GOOD' },
      { itemId: '2', itemName: '体重', unit: 'kg', rawValue: 42, convertedScore: 80, level: 'GOOD' },
      { itemId: '3', itemName: '50米跑', unit: '秒', rawValue: 8.8, convertedScore: 70, level: 'PASS' },
      { itemId: '4', itemName: '立定跳远', unit: 'cm', rawValue: 170, convertedScore: 75, level: 'GOOD' },
      { itemId: '5', itemName: '坐位体前屈', unit: 'cm', rawValue: 10, convertedScore: 70, level: 'PASS' },
    ],
    evaluation: '身体状况良好，建议加强下肢力量训练。'
  },
  { 
    id: '3', 
    recordNo: 'R003', 
    studentId: '3', 
    testerId: 'T001', 
    teacherName: '张老师',
    testDate: '2024-01-15', 
    totalScore: 65, 
    overallLevel: 'PASS', 
    status: 'CONFIRMED',
    scores: [
      { itemId: '1', itemName: '身高', unit: 'cm', rawValue: 152, convertedScore: 80, level: 'GOOD' },
      { itemId: '2', itemName: '体重', unit: 'kg', rawValue: 48, convertedScore: 70, level: 'PASS' },
      { itemId: '3', itemName: '50米跑', unit: '秒', rawValue: 9.2, convertedScore: 65, level: 'PASS' },
      { itemId: '4', itemName: '立定跳远', unit: 'cm', rawValue: 160, convertedScore: 68, level: 'PASS' },
      { itemId: '5', itemName: '坐位体前屈', unit: 'cm', rawValue: 8, convertedScore: 62, level: 'PASS' },
    ],
    evaluation: '身体状况及格，需要加强锻炼，尤其是耐力训练。'
  },
];

export default function PhysicalPage() {
  const [activeTab, setActiveTab] = useState<'import' | 'export'>('import');
  const [selectedStudents, setSelectedStudents] = useState<string[]>([]);
  const [showExportModal, setShowExportModal] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<PhysicalTestRecord | null>(null);

  const toggleStudent = (studentId: string) => {
    if (selectedStudents.includes(studentId)) {
      setSelectedStudents(selectedStudents.filter(id => id !== studentId));
    } else {
      setSelectedStudents([...selectedStudents, studentId]);
    }
  };

  const selectAllStudents = () => {
    if (selectedStudents.length === mockStudents.length) {
      setSelectedStudents([]);
    } else {
      setSelectedStudents(mockStudents.map(s => s.id));
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
            <Text className={styles.sectionTitle}>最近录入记录</Text>
            
            {mockRecords.length === 0 ? (
              <Card className={styles.emptyCard}>
                <Text className={styles.emptyText}>暂无录入记录</Text>
              </Card>
            ) : (
              mockRecords.map(record => {
                const student = mockStudents.find(s => s.id === record.studentId);
                return (
                  <Card key={record.id} className={styles.recordCard} onClick={() => handleViewReport(record)}>
                    <View className={styles.recordHeader}>
                      <View className={styles.recordInfo}>
                        <Text className={styles.recordStudent}>{student?.name || '未知学生'}</Text>
                        <Text className={styles.recordDate}>{formatDate(record.testDate)}</Text>
                      </View>
                      <View className={styles.recordScore}>
                        <Text className={styles.score}>{record.totalScore}</Text>
                        {getLevelTag(record.overallLevel)}
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
            <Text className={styles.sectionTitle}>学生列表</Text>
            
            {mockStudents.map(student => {
              const latestRecord = mockRecords.find(r => r.studentId === student.id);
              return (
                <Card key={student.id} className={styles.studentCard}>
                  <View className={styles.studentHeader}>
                    <View className={styles.studentInfo}>
                      <Text className={styles.studentName}>{student.name}</Text>
                      <Text className={styles.studentNo}>{student.studentNo}</Text>
                    </View>
                    {student.gender === 'MALE' && <Tag type="primary">男</Tag>}
                    {student.gender === 'FEMALE' && <Tag type="success">女</Tag>}
                  </View>
                  
                  {latestRecord && (
                    <View className={styles.studentRecord}>
                      <Text className={styles.recordLabel}>最近成绩</Text>
                      <Text className={styles.recordValue}>{latestRecord.totalScore}分 {getLevelTag(latestRecord.overallLevel)}</Text>
                    </View>
                  )}
                  
                  <CustomButton type="secondary" size="sm" onClick={handleExportPDF}>
                    导出PDF
                  </CustomButton>
                </Card>
              );
            })}
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
                <View className={`${styles.checkbox} ${selectedStudents.length === mockStudents.length ? styles.checked : ''}`} />
                <Text className={styles.selectAllText}>全选</Text>
              </View>
              <ScrollView className={styles.studentList} scrollY>
                {mockStudents.map(student => (
                  <View key={student.id} className={styles.studentItem}>
                    <View 
                      className={`${styles.checkbox} ${selectedStudents.includes(student.id) ? styles.checked : ''}`}
                      onClick={() => toggleStudent(student.id)}
                    />
                    <View className={styles.studentInfo}>
                      <Text className={styles.studentName}>{student.name}</Text>
                      <Text className={styles.studentNo}>{student.studentNo}</Text>
                    </View>
                    {student.gender === 'MALE' && <Tag type="primary">男</Tag>}
                    {student.gender === 'FEMALE' && <Tag type="success">女</Tag>}
                  </View>
                ))}
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
                  <Text className={styles.totalScore}>{selectedRecord.totalScore}</Text>
                  <Text className={styles.totalLabel}>综合得分</Text>
                </View>
                <View className={styles.studentInfoRow}>
                  <Text className={styles.studentInfoLabel}>测试学员</Text>
                  <Text className={styles.studentInfoValue}>
                    {mockStudents.find(s => s.id === selectedRecord.studentId)?.name || '未知学生'}
                  </Text>
                </View>
                <View className={styles.studentInfoRow}>
                  <Text className={styles.studentInfoLabel}>测试老师</Text>
                  <Text className={styles.studentInfoValue}>{selectedRecord.teacherName || '未知'}</Text>
                </View>
              </View>

              <View className={styles.reportSection}>
                <Text className={styles.reportSectionTitle}>各项指标</Text>
                {selectedRecord.scores?.map((score, index) => (
                  <View key={index} className={styles.scoreItem}>
                    <Text className={styles.scoreName}>{score.itemName}</Text>
                    <Text className={styles.scoreValue}>{score.rawValue}</Text>
                    <Text className={styles.scoreUnit}>{score.unit}</Text>
                    {getLevelTag(score.level)}
                  </View>
                ))}
              </View>

              <View className={styles.reportSection}>
                <Text className={styles.reportSectionTitle}>评估结果</Text>
                <View className={styles.evaluation}>
                  <Text className={styles.evaluationText}>
                    {selectedRecord.evaluation || '暂无评估结果'}
                  </Text>
                </View>
              </View>
            </ScrollView>

            <View className={styles.reportFooter}>
              <Text className={styles.downloadBtn} onClick={handleDownloadReport}>
                下载报告
              </Text>
            </View>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
