import React from 'react';
import { View, Text } from '@tarojs/components';
import styles from './index.module.scss';

interface TagProps {
  children: React.ReactNode;
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
}

export default function Tag({ children, type = 'primary' }: TagProps) {
  const typeMap = {
    primary: styles.typePrimary,
    success: styles.typeSuccess,
    warning: styles.typeWarning,
    danger: styles.typeDanger,
    info: styles.typeInfo
  };
  
  return (
    <View className={`${styles.tag} ${typeMap[type]}`}>
      <Text className={styles.text}>{children}</Text>
    </View>
  );
}
