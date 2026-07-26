import { useState } from 'react';
import { View, Text, ScrollView, Input, Picker } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import { Student } from '@/types';
import { createCourse } from '@/services/api';
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

export default function SchedulePage() {
  const [selectedDate, setSelectedDate] = useState(formatDate(new Date()));
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [courseName, setCourseName] = useState('体育课');
  const [selectedStudents, setSelectedStudents] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showStudentSelector, setShowStudentSelector] = useState(false);

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

  const handleDateChange = (e: any) => {
    setSelectedDate(e.detail.value);
  };

  const handleStartTimeChange = (e: any) => {
    setStartTime(e.detail.value);
  };

  const handleEndTimeChange = (e: any) => {
    setEndTime(e.detail.value);
  };

  const handleSubmit = async () => {
    if (!startTime) {
      Taro.showToast({ title: '请选择开始时间', icon: 'none' });
      return;
    }
    if (!endTime) {
      Taro.showToast({ title: '请选择结束时间', icon: 'none' });
      return;
    }
    if (startTime >= endTime) {
      Taro.showToast({ title: '结束时间必须晚于开始时间', icon: 'none' });
      return;
    }
    if (selectedStudents.length === 0) {
      Taro.showToast({ title: '请选择学生', icon: 'none' });
      return;
    }

    setIsSubmitting(true);
    try {
      const params = {
        lessonDate: selectedDate,
        startTime,
        endTime,
        courseName,
        studentIds: selectedStudents,
      };

      await createCourse(params);
      Taro.showToast({ title: '排课成功', icon: 'success' });
      
      setStartTime('');
      setEndTime('');
      setSelectedStudents([]);
    } catch (err) {
      console.error('[SchedulePage]', err);
      Taro.showToast({ title: '排课失败', icon: 'none' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <ScrollView className={styles.schedulePage} scrollY>
      <View className={styles.container}>
        <Card className={styles.formCard}>
          <Text className={styles.formTitle}>创建课程</Text>

          <View className={styles.formItem}>
            <Text className={styles.formLabel}>上课日期</Text>
            <Picker mode="date" value={selectedDate} onChange={handleDateChange}>
              <View className={styles.formValue}>
                <Text className={styles.valueText}>{selectedDate}</Text>
                <Text className={styles.arrow}>›</Text>
              </View>
            </Picker>
          </View>

          <View className={styles.formItem}>
            <Text className={styles.formLabel}>课程名称</Text>
            <Input
              className={styles.formInput}
              value={courseName}
              onInput={(e) => setCourseName(e.detail.value)}
              placeholder="请输入课程名称"
            />
          </View>

          <View className={styles.formItem}>
            <Text className={styles.formLabel}>开始时间</Text>
            <Picker mode="time" value={startTime} onChange={handleStartTimeChange}>
              <View className={styles.formValue}>
                <Text className={styles.valueText}>{startTime || '请选择开始时间'}</Text>
                <Text className={styles.arrow}>›</Text>
              </View>
            </Picker>
          </View>

          <View className={styles.formItem}>
            <Text className={styles.formLabel}>结束时间</Text>
            <Picker mode="time" value={endTime} onChange={handleEndTimeChange}>
              <View className={styles.formValue}>
                <Text className={styles.valueText}>{endTime || '请选择结束时间'}</Text>
                <Text className={styles.arrow}>›</Text>
              </View>
            </Picker>
          </View>

          <View className={styles.formItem}>
            <Text className={styles.formLabel}>
              选择学生
              <Text className={styles.selectCount}>{selectedStudents.length}/{mockStudents.length}</Text>
            </Text>
            <View className={styles.studentPreview} onClick={() => setShowStudentSelector(true)}>
              {selectedStudents.length === 0 ? (
                <Text className={styles.previewPlaceholder}>点击选择学生</Text>
              ) : (
                <Text className={styles.previewText}>
                  已选择 {selectedStudents.length} 名学生
                </Text>
              )}
              <Text className={styles.arrow}>›</Text>
            </View>
          </View>

          <CustomButton type="primary" size="lg" onClick={handleSubmit} disabled={isSubmitting}>
            {isSubmitting ? '提交中...' : '确认排课'}
          </CustomButton>
        </Card>
      </View>

      {showStudentSelector && (
        <View className={styles.modalOverlay} onClick={() => setShowStudentSelector(false)}>
          <View className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <View className={styles.modalHeader}>
              <Text className={styles.modalTitle}>选择学生</Text>
              <Text className={styles.modalClose} onClick={() => setShowStudentSelector(false)}>×</Text>
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
                      onClick={(e: any) => { e.stopPropagation && e.stopPropagation(); toggleStudent(student.id); }}
                    />
                    <View className={styles.studentInfo}>
                      <Text className={styles.studentName}>{student.name}</Text>
                      <Text className={styles.studentNo}>{student.studentNo}</Text>
                      {student.parentName && (
                        <Text className={styles.parentInfo}>{student.parentName} {student.parentPhone}</Text>
                      )}
                    </View>
                    {student.gender === 'MALE' && <Tag type="primary">男</Tag>}
                    {student.gender === 'FEMALE' && <Tag type="success">女</Tag>}
                  </View>
                ))}
              </ScrollView>
            </View>
            <View className={styles.modalFooter}>
              <CustomButton type="secondary" onClick={() => setShowStudentSelector(false)}>取消</CustomButton>
              <CustomButton type="primary" onClick={() => setShowStudentSelector(false)}>确定</CustomButton>
            </View>
          </View>
        </View>
      )}
    </ScrollView>
  );
}
