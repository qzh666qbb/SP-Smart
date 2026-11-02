import { useEffect, useRef, useState } from 'react';
import { View, Text, Input } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import { Student } from '@/types';
import { getAllTeacherStudents } from '@/services/api';
import styles from './index.module.scss';

export default function TeacherStudentsPage() {
  const [students, setStudents] = useState<Student[]>([]);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const requestId = useRef(0);

  const load = async (value = '') => {
    const id = ++requestId.current;
    const isCurrent = () => requestId.current === id;
    setLoading(true);
    setLoadError('');
    setStudents([]);
    try {
      const result = await getAllTeacherStudents(value.trim() || undefined, isCurrent);
      if (isCurrent()) setStudents(result);
    } catch (error) {
      if (!isCurrent()) return;
      console.error('[TeacherStudentsPage.load]', error);
      setLoadError('学员加载失败，请点击搜索重试');
      Taro.showToast({ title: '学员加载失败', icon: 'none' });
    } finally {
      if (isCurrent()) setLoading(false);
    }
  };

  useEffect(() => {
    load();
    return () => { requestId.current++; };
  }, []);

  return (
    <View className={styles.page}>
      <View className={styles.searchRow}>
        <Input
          className={styles.searchInput}
          value={keyword}
          placeholder="输入姓名或学号"
          confirmType="search"
          onInput={(event) => setKeyword(event.detail.value)}
          onConfirm={() => load(keyword)}
        />
        <Text className={styles.searchButton} onClick={() => load(keyword)}>搜索</Text>
      </View>

      <Text className={styles.count}>{loading ? '加载中…' : loadError || `共 ${students.length} 名学员`}</Text>
      {!loading && students.length === 0 ? (
        <Card className={styles.empty}>{loadError || '暂无匹配学员'}</Card>
      ) : students.map((student) => (
        <Card
          key={student.id}
          className={styles.studentCard}
          onClick={() => Taro.navigateTo({ url: `/pages/student-detail/index?studentId=${encodeURIComponent(student.id)}` })}
        >
          <View className={styles.studentMain}>
            <Text className={styles.name}>{student.name}</Text>
            <Text className={styles.no}>学号：{student.studentNo || '-'}</Text>
          </View>
          <View className={styles.studentMeta}>
            {student.gender === 'MALE' && <Tag type="primary">男</Tag>}
            {student.gender === 'FEMALE' && <Tag type="success">女</Tag>}
            <Text className={styles.arrow}>›</Text>
          </View>
        </Card>
      ))}
    </View>
  );
}
