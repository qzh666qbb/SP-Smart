import { View, Text } from '@tarojs/components';
import Card from '../Card';
import Tag from '../Tag';
import { PhysicalTestRecord } from '@/types';
import { formatDate, formatScore } from '@/utils/format';
import styles from './index.module.scss';

interface PhysicalCardProps {
  record: PhysicalTestRecord;
  onClick?: () => void;
}

export default function PhysicalCard({ record, onClick }: PhysicalCardProps) {
  const getLevelTag = () => {
    switch (record.overallLevel) {
      case 'EXCELLENT':
        return <Tag type="success">优秀</Tag>;
      case 'GOOD':
        return <Tag type="primary">良好</Tag>;
      case 'PASS':
        return <Tag type="info">及格</Tag>;
      default:
        return <Tag type="danger">不及格</Tag>;
    }
  };
  
  return (
    <Card className={styles.physicalCard} onClick={onClick}>
      <View className={styles.header}>
        <Text className={styles.score}>{formatScore(record.totalScore)}</Text>
        {getLevelTag()}
      </View>
      <View className={styles.info}>
        <Text className={styles.label}>测试日期</Text>
        <Text className={styles.value}>{formatDate(record.testDate)}</Text>
      </View>
      <View className={styles.info}>
        <Text className={styles.label}>测试编号</Text>
        <Text className={styles.value}>{record.recordNo}</Text>
      </View>
      {record.remark && (
        <View className={styles.info}>
          <Text className={styles.label}>备注</Text>
          <Text className={styles.value}>{record.remark}</Text>
        </View>
      )}
    </Card>
  );
}
