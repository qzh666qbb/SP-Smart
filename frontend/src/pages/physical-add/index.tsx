import { useState, useEffect } from 'react';
import { View, Text, Input, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { addPhysicalTest, getTeacherStudents, getPhysicalTestItems } from '@/services/api';
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

const defaultItems: ScoreItem[] = [
  { itemId: '1', itemName: '身高', unit: 'cm', rawValue: '', convertedScore: '', level: 'PASS' },
  { itemId: '2', itemName: '体重', unit: 'kg', rawValue: '', convertedScore: '', level: 'PASS' },
  { itemId: '3', itemName: '50米跑', unit: '秒', rawValue: '', convertedScore: '', level: 'PASS' },
  { itemId: '4', itemName: '立定跳远', unit: 'cm', rawValue: '', convertedScore: '', level: 'PASS' },
  { itemId: '5', itemName: '坐位体前屈', unit: 'cm', rawValue: '', convertedScore: '', level: 'PASS' },
];

export default function PhysicalAddPage() {
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const [students, setStudents] = useState<Student[]>([]);
  const [isLoadingStudents, setIsLoadingStudents] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showStudentSelector, setShowStudentSelector] = useState(false);
  const [studentSearchKeyword, setStudentSearchKeyword] = useState('');
  const [scores, setScores] = useState<ScoreItem[]>(defaultItems);

  useEffect(() => {
    fetchStudentsAndItems();
  }, []);

  const fetchStudentsAndItems = async () => {
    setIsLoadingStudents(true);
    try {
      const [sRes, items] = await Promise.all([
        getTeacherStudents({ pageNum: 1, pageSize: 200 }),
        getPhysicalTestItems().catch(() => []),
      ]);
      setStudents(sRes.students || []);
      if (Array.isArray(items) && items.length > 0) {
        setScores(items.map((it: any, idx) => ({
          itemId: String(it.id ?? idx + 1),
          itemName: it.name || it.itemName || defaultItems[idx]?.itemName || ('项目' + (idx + 1)),
          unit: it.unit || defaultItems[idx]?.unit || '',
          rawValue: '',
          convertedScore: '',
          level: 'PASS' as const,
        })));
      }
    } catch (err) {
      console.error('[PhysicalAddPage.fetch]', err);
      Taro.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      setIsLoadingStudents(false);
    }
  };

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
          itemName: s.itemName,
          unit: s.unit,
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

  const renderStudents = () => {
    if (isLoadingStudents) return <View className={styles.studentItem}><Text className={styles.studentName}>学员列表加载中...</Text></View>;

    const kw = studentSearchKeyword.trim().toLowerCase();
    const displayStudents = !kw
      ? students
      : students.filter(s =>
          (s.name || '').toLowerCase().includes(kw) ||
          (s.studentNo || '').toLowerCase().includes(kw) ||
          (s.parentName || '').toLowerCase().includes(kw) ||
          (s.parentPhone || '').toLowerCase().includes(kw) ||
          (String(s.id) === kw)
        );

    if (displayStudents.length === 0) {
      return (
        <View className={styles.studentItem}>
          <Text className={styles.studentName}>
            {students.length === 0 ? '暂无学员' : '未找到匹配的学员，试试姓名/学号/家长手机号'}
          </Text>
        </View>
      );
    }

    return displayStudents.map(student => (
      <View
        key={student.id}
        className={`${styles.studentItem} ${selectedStudent?.id === student.id ? styles.selected : ''}`}
        onClick={() => {
          setSelectedStudent(student);
          setShowStudentSelector(false);
          setStudentSearchKeyword('');
        }}
      >
        <View className={styles.studentAvatar}>
          <Text className={styles.avatarText}>{student.name[0]}</Text>
        </View>
        <View className={styles.studentDetail}>
          <Text className={styles.studentName}>{student.name}</Text>
          <Text className={styles.studentNo}>{student.studentNo || ('ID: ' + student.id)}</Text>
          {student.parentName && (
            <Text className={styles.parentInfo}>{student.parentName} {student.parentPhone}</Text>
          )}
        </View>
        {selectedStudent?.id === student.id && (
          <Text className={styles.checkIcon}>✓</Text>
        )}
      </View>
    ));
  };

  return (
    <ScrollView className={styles.addPage} scrollY>
      <View className={styles.container}>
        <Card className={styles.studentCard} onClick={() => setShowStudentSelector(true)}>
          <Text className={styles.studentLabel}>测试学员</Text>
          {selectedStudent ? (
            <View className={styles.studentInfo}>
              <Text className={styles.studentName}>{selectedStudent.name}</Text>
              <Text className={styles.studentNo}>{selectedStudent.studentNo || ('ID: ' + selectedStudent.id)}</Text>
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
              {score.unit && <Text className={styles.scoreUnit}>{score.unit}</Text>}
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
        <View className={styles.modalOverlay} onClick={() => {
          setShowStudentSelector(false);
          setStudentSearchKeyword('');
        }}>
          <View className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <View className={styles.modalHeader}>
              <Text className={styles.modalTitle}>选择学员（共 {students.length} 人）</Text>
              <Text className={styles.modalClose} onClick={() => {
                setShowStudentSelector(false);
                setStudentSearchKeyword('');
              }}>×</Text>
            </View>
            <View className={styles.searchBox}>
              <Input
                className={styles.searchInput}
                placeholder="输入姓名 / 学号 / 家长手机号 快速搜索"
                value={studentSearchKeyword}
                confirmType="search"
                onInput={(e) => setStudentSearchKeyword(e.detail.value)}
              />
            </View>
            <ScrollView className={styles.studentList} scrollY>
              {renderStudents()}
            </ScrollView>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
