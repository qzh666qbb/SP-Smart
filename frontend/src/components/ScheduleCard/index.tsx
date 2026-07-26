import { View, Text } from '@tarojs/components';
import Card from '../Card';
import Tag from '../Tag';
import { Lesson } from '@/types';
import { formatTime, formatWeekDay } from '@/utils/format';
import styles from './index.module.scss';

interface ScheduleCardProps {
  lesson: Lesson;
  courseName?: string;
  className?: string;
  onClick?: () => void;
}

export default function ScheduleCard({ lesson, courseName = '体育课', className, onClick }: ScheduleCardProps) {
  const getStatusTag = () => {
    switch (lesson.status) {
      case 'IN_PROGRESS':
        return <Tag type="warning">进行中</Tag>;
      case 'COMPLETED':
        return <Tag type="success">已完成</Tag>;
      case 'CANCELLED':
        return <Tag type="danger">已取消</Tag>;
      default:
        return <Tag type="info">未开始</Tag>;
    }
  };
  
  return (
    <Card className={`${styles.scheduleCard} ${className || ''}`} onClick={onClick}>
      <View className={styles.header}>
        <View className={styles.timeInfo}>
          <Text className={styles.time}>{formatTime(lesson.startTime)} - {formatTime(lesson.endTime)}</Text>
          <Text className={styles.date}>{formatWeekDay(lesson.lessonDate)} {lesson.lessonDate}</Text>
        </View>
        {getStatusTag()}
      </View>
      <View className={styles.content}>
        <Text className={styles.courseName}>{courseName}</Text>
        <Text className={styles.description}>课程编号: {lesson.lessonNo}</Text>
      </View>
      {lesson.cancelReason && (
        <View className={styles.cancelReason}>
          <Text className={styles.reasonText}>取消原因: {lesson.cancelReason}</Text>
        </View>
      )}
    </Card>
  );
}
