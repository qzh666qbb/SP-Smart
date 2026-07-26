import { useState } from 'react';
import { View, Text, Input, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import CustomButton from '@/components/Button';
import Tag from '@/components/Tag';
import { addPhysicalTest } from '@/services/api';
import { Student } from '@/types';
import styles from './index.module.scss';

interface ScoreItem {
  itemId: string;
  itemName: string;
  unit: string;
  rawValue: string;
  convertedScore: string;
  level: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL';
}

const mockStudents: Student[] = [
  { id: '1', name: '张三', age: 12, gender: 'MALE', studentNo: 'S001', parentName: '张爸爸', parentPhone: '13800138001' },
  { id: '2', name: '李四', age: 12, gender: 'FEMALE', studentNo: 'S002', parentName: '李妈妈', parentPhone: '13800138002' },
  { id: '3', name: '王五', age: 13, gender: 'MALE', studentNo: 'S003', parentName: '王爸爸', parentPhone: '13800138003' },
  { id: '4', name: '赵六', age: 12, gender: 'FEMALE', studentNo: 'S004', parentName: '赵妈妈', parentPhone: '13800138004' },
  { id: '5', name: '钱七', age: 13, gender: 'MALE', studentNo: 'S005', parentName: '钱爸爸', parentPhone: '13800138005' },
  { id: '6', name: '孙八', age: 12, gender: 'FEMALE', studentNo: 'S006', parentName: '孙妈妈', parentPhone: '13800138006' },
];

export default function PhysicalAddPage() {
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const [scores, setScores] = useState<ScoreItem[]>([
    { itemId: '1', itemName: '身高', unit: 'cm', rawValue: '', convertedScore: '', level: 'PASS' },
    { itemId: '2', itemName: '体重', unit: 'kg', rawValue: '', convertedScore: '', level: 'PASS' },
    { itemId: '3', itemName: '50米跑', unit: '秒', rawValue: '', convertedScore: '', level: 'PASS' },
    { itemId: '4', itemName: '立定跳远', unit: 'cm', rawValue: '', convertedScore: '', level: 'PASS' },
    { itemId: '5', itemName: '坐位体前屈', unit: 'cm', rawValue: '', convertedScore: '', level: 'PASS' },
  ]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showStudentSelector, setShowStudentSelector] = useState(false);
  
  const handleInputChange = (index: number, field: 'rawValue' | 'convertedScore', value: string) => {
    const newScores = [...scores];
    newScores[index][field] = value;
    setScores(newScores);
  };
  
  const handleLevelChange = (index: number, level: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL') => {
    const newScores = [...scores];
    newScores[index].level = level;
    setScores(newScores);
  };
  
  const getLevelTag = (level: string) => {
    switch (level) {
      case 'EXCELLENT': return <Tag type="success">优秀</Tag>;
      case 'GOOD': return <Tag type="primary">良好</Tag>;
      case 'PASS': return <Tag type="info">及格</Tag>;
      default: return <Tag type="danger">不及格</Tag>;
    }
  };
  
  const handleSubmit = async () => {
    if (!selectedStudent) {
      Taro.showToast({ title: '请选择学员', icon: 'none' });
      return;
    }
    
    for (const score of scores) {
      if (!score.rawValue || !score.convertedScore) {
        Taro.showToast({ title: '请填写完整数据', icon: 'none' });
        return;
      }
    }
    
    setIsSubmitting(true);
    try {
      const params = {
        studentId: selectedStudent.id,
        scores: scores.map(s => ({
          itemId: s.itemId,
          rawValue: parseFloat(s.rawValue),
          convertedScore: parseFloat(s.convertedScore),
          level: s.level
        }))
      };
      
      await addPhysicalTest(params);
      Taro.showToast({ title: '录入成功', icon: 'success' });
      setTimeout(() => {
        Taro.navigateBack();
      }, 1500);
    } catch (err) {
      console.error('[PhysicalAddPage]', err);
      Taro.showToast({ title: '录入失败', icon: 'none' });
    } finally {
      setIsSubmitting(false);
    }
  };
  
  return (
    <ScrollView className={styles.addPage} scrollY>
      <View className={styles.container}>
        <Card className={styles.studentCard} onClick={() => setShowStudentSelector(true)}>
          <Text className={styles.studentLabel}>测试学员</Text>
          {selectedStudent ? (
            <View className={styles.studentInfo}>
              <Text className={styles.studentName}>{selectedStudent.name}</Text>
              <Text className={styles.studentNo}>{selectedStudent.studentNo}</Text>
              {selectedStudent.parentName && (
                <Text className={styles.parentInfo}>{selectedStudent.parentName} {selectedStudent.parentPhone}</Text>
              )}
            </View>
          ) : (
            <Text className={styles.studentPlaceholder}>请选择学员</Text>
          )}
          <Text className={styles.arrow}>›</Text>
        </Card>
        
        {scores.map((score, index) => (
          <Card key={score.itemId} className={styles.scoreCard}>
            <View className={styles.scoreHeader}>
              <Text className={styles.scoreName}>{score.itemName}</Text>
              <Text className={styles.scoreUnit}>{score.unit}</Text>
            </View>
            
            <View className={styles.inputRow}>
              <View className={styles.inputGroup}>
                <Text className={styles.inputLabel}>原始成绩</Text>
                <Input
                  className={styles.input}
                  placeholder={`请输入${score.itemName}`}
                  type="digit"
                  value={score.rawValue}
                  onInput={(e) => handleInputChange(index, 'rawValue', e.detail.value)}
                />
              </View>
              <View className={styles.inputGroup}>
                <Text className={styles.inputLabel}>转换分数</Text>
                <Input
                  className={styles.input}
                  placeholder="请输入分数"
                  type="digit"
                  value={score.convertedScore}
                  onInput={(e) => handleInputChange(index, 'convertedScore', e.detail.value)}
                />
              </View>
            </View>
            
            <View className={styles.levelSelector}>
              <Text className={styles.levelLabel}>评级</Text>
              <View className={styles.levelOptions}>
                {(['EXCELLENT', 'GOOD', 'PASS', 'FAIL'] as const).map(l => (
                  <View
                    key={l}
                    className={`${styles.levelOption} ${score.level === l ? styles.active : ''}`}
                    onClick={() => handleLevelChange(index, l)}
                  >
                    {getLevelTag(l)}
                  </View>
                ))}
              </View>
            </View>
          </Card>
        ))}
        
        <CustomButton type="primary" size="lg" onClick={handleSubmit} disabled={isSubmitting}>
          {isSubmitting ? '提交中...' : '确认提交'}
        </CustomButton>
      </View>

      {showStudentSelector && (
        <View className={styles.modalOverlay} onClick={() => setShowStudentSelector(false)}>
          <View className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <View className={styles.modalHeader}>
              <Text className={styles.modalTitle}>选择学员</Text>
              <Text className={styles.modalClose} onClick={() => setShowStudentSelector(false)}>×</Text>
            </View>
            <ScrollView className={styles.studentList} scrollY>
              {mockStudents.map(student => (
                <View 
                  key={student.id} 
                  className={`${styles.studentItem} ${selectedStudent?.id === student.id ? styles.selected : ''}`}
                  onClick={() => {
                    setSelectedStudent(student);
                    setShowStudentSelector(false);
                  }}
                >
                  <View className={styles.studentAvatar}>
                    <Text className={styles.avatarText}>{student.name[0]}</Text>
                  </View>
                  <View className={styles.studentDetail}>
                    <Text className={styles.studentName}>{student.name}</Text>
                    <Text className={styles.studentNo}>{student.studentNo}</Text>
                    {student.parentName && (
                      <Text className={styles.parentInfo}>{student.parentName} {student.parentPhone}</Text>
                    )}
                  </View>
                  {selectedStudent?.id === student.id && (
                    <Text className={styles.checkIcon}>✓</Text>
                  )}
                </View>
              ))}
            </ScrollView>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
