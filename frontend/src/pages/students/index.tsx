import { useState, useEffect } from 'react';
import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Student } from '@/types';
import styles from './index.module.scss';

const mockStudents: Student[] = [
  { id: '1', name: '张三', age: 12, gender: 'MALE', studentNo: 'S001', parentName: '张父', parentPhone: '13800138001' },
  { id: '2', name: '李四', age: 12, gender: 'FEMALE', studentNo: 'S002', parentName: '李母', parentPhone: '13800138002' },
  { id: '3', name: '王五', age: 13, gender: 'MALE', studentNo: 'S003', parentName: '王父', parentPhone: '13800138003' },
  { id: '4', name: '赵六', age: 12, gender: 'FEMALE', studentNo: 'S004', parentName: '赵母', parentPhone: '13800138004' },
  { id: '5', name: '钱七', age: 13, gender: 'MALE', studentNo: 'S005', parentName: '钱父', parentPhone: '13800138005' },
  { id: '6', name: '孙八', age: 12, gender: 'FEMALE', studentNo: 'S006', parentName: '孙母', parentPhone: '13800138006' },
];

export default function StudentsPage() {
  const [students, setStudents] = useState<Student[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    setIsLoading(true);
    try {
      setStudents(mockStudents);
    } catch (err) {
      console.error('[StudentsPage]', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleStudentClick = (student: Student) => {
    Taro.navigateTo({ url: `/pages/student-detail/index?id=${student.id}` });
  };

  return (
    <ScrollView className={styles.studentsPage} scrollY>
      <View className={styles.container}>
        <View className={styles.header}>
          <Text className={styles.pageTitle}>学员列表</Text>
          <Text className={styles.pageCount}>{students.length}名学员</Text>
        </View>

        {isLoading ? (
          <View className={styles.loading}>
            <Text>加载中...</Text>
          </View>
        ) : students.length === 0 ? (
          <Card className={styles.emptyCard}>
            <Text className={styles.emptyText}>暂无学员</Text>
          </Card>
        ) : (
          students.map(student => (
            <Card key={student.id} className={styles.studentCard} onClick={() => handleStudentClick(student)}>
              <View className={styles.studentHeader}>
                <View className={styles.studentAvatar}>
                  <Text className={styles.avatarText}>{student.name[0]}</Text>
                </View>
                <View className={styles.studentInfo}>
                  <View className={styles.studentNameRow}>
                    <Text className={styles.studentName}>{student.name}</Text>
                    {student.gender === 'MALE' && <Tag type="primary">男</Tag>}
                    {student.gender === 'FEMALE' && <Tag type="success">女</Tag>}
                  </View>
                  <Text className={styles.studentNo}>学号: {student.studentNo}</Text>
                  {student.age && <Text className={styles.studentAge}>{student.age}岁</Text>}
                </View>
              </View>
              <View className={styles.parentInfo}>
                <Text className={styles.parentLabel}>家长:</Text>
                <Text className={styles.parentName}>{student.parentName || '未填写'}</Text>
                <Text className={styles.parentPhone}>{student.parentPhone || '未填写'}</Text>
              </View>
              <Text className={styles.arrow}>›</Text>
            </Card>
          ))
        )}
      </View>
    </ScrollView>
  );
}
