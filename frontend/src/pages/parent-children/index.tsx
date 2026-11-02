import { useState, useEffect, useCallback } from 'react';
import { View, Text } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import CustomButton from '@/components/Button';
import { listParentChildren } from '@/services/api';
import { Student } from '@/types';
import useAuthStore from '@/store/auth';
import { notifyTabBarActive } from '@/utils/tabbar';
import styles from './index.module.scss';
import BottomNav from '@/components/BottomNav';

function genderLabel(g?: 'MALE' | 'FEMALE' | 'UNKNOWN'): string {
  if (g === 'MALE') return '男';
  if (g === 'FEMALE') return '女';
  if (g === 'UNKNOWN') return '未知';
  return '--';
}

function genderClass(g?: 'MALE' | 'FEMALE' | 'UNKNOWN'): string {
  if (g === 'MALE') return 'male';
  if (g === 'FEMALE') return 'female';
  return '';
}

export default function ParentChildrenPage() {
  Taro.useDidShow(() => notifyTabBarActive('/pages/parent-children/index'));
  const { token } = useAuthStore();
  const [list, setList] = useState<Student[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState('');

  const fetchChildren = useCallback(async () => {
    setIsLoading(true);
    setLoadError('');
    try {
      const data = await listParentChildren();
      setList(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('[ParentChildren.fetch]', err);
      setLoadError('孩子资料加载失败，请检查登录状态后重试');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchChildren();
  }, [fetchChildren]);

  useEffect(() => {
    const refresh = () => fetchChildren();
    try {
      (Taro as any).eventCenter?.on?.('parent-children:refresh', refresh);
    } catch (_) { /* noop */ }
    return () => {
      try {
        (Taro as any).eventCenter?.off?.('parent-children:refresh', refresh);
      } catch (_) { /* noop */ }
    };
  }, [fetchChildren]);

  const goAddChild = () => {
    Taro.navigateTo({ url: `/pages/child-add-edit/index?isFirst=${list.length === 0 ? '1' : '0'}` });
  };

  const goDetail = (studentId: string) => {
    if (!studentId) return;
    Taro.navigateTo({ url: `/pages/student-detail/index?id=${studentId}` });
  };

  const goHours = (e: any, studentId: string) => {
    e?.stopPropagation?.();
    if (!studentId) return;
    Taro.navigateTo({ url: `/pages/hour-transaction/index?studentId=${studentId}` });
  };

  const goGlobalHoursEntry = () => {
    if (list.length === 0) {
      Taro.showToast({ title: '请先添加孩子', icon: 'none' });
      return;
    }
    const firstId = list[0]?.id;
    if (firstId) {
      Taro.navigateTo({ url: `/pages/hour-transaction/index?studentId=${firstId}` });
    }
  };

  const renderEmpty = () => (
    <View className={styles.emptyState}>
      <Text className={styles.emptyIcon}>🧒</Text>
      <Text className={styles.emptyTitle}>您还没有添加孩子资料</Text>
      <Text className={styles.emptyDesc}>
        添加孩子后即可查看课时、课程和体测档案；充值请微信联系管理员
      </Text>
      <CustomButton type="primary" size="lg" block onClick={goAddChild}>
        立即添加孩子
      </CustomButton>
    </View>
  );

  return (
    <View className={styles.childrenPage}>
      <View className={styles.pageHeader}>
        <Text className={styles.pageTitle}>我的孩子</Text>
        <View className={styles.headerActions}>
          <View className={styles.addChildButton} onClick={goAddChild}>
            <Text className={styles.addChildIcon}>＋</Text>
            <Text>添加孩子</Text>
          </View>
          <Text className={styles.quickEntryBtn} onClick={goGlobalHoursEntry}>
            课时账户 / 流水
          </Text>
        </View>
      </View>

      <View className={styles.scrollArea}>
        <View className={styles.container}>
          {isLoading && list.length === 0 ? (
            <View className={styles.emptyState}>
              <Text className={styles.emptyTitle}>正在加载孩子资料...</Text>
            </View>
          ) : loadError && list.length === 0 ? (
            <View className={styles.emptyState}>
              <Text className={styles.emptyTitle}>{loadError}</Text>
              <CustomButton type="secondary" size="md" onClick={fetchChildren}>重新加载</CustomButton>
            </View>
          ) : list.length === 0 ? (
            renderEmpty()
          ) : (
            <>
              {loadError && (
                <View className={styles.emptyState}>
                  <Text className={styles.emptyDesc}>{loadError}</Text>
                  <CustomButton type="secondary" size="sm" onClick={fetchChildren}>重新加载</CustomButton>
                </View>
              )}
              {list.map((s) => (
              <Card
                key={s.id || String(s.name || Math.random())}
                className={styles.childCard}
                onClick={() => goDetail(s.id)}
              >
                <View className={styles.childAvatar}>
                  <Text className={styles.avatarText}>
                    {(s.name || ' ')[0]}
                  </Text>
                </View>
                <View className={styles.childInfo}>
                  <View className={styles.childNameRow}>
                    <Text className={styles.childName}>{s.name || '--'}</Text>
                    <Text className={`${styles.genderTag} ${genderClass(s.gender)}`}>
                      {genderLabel(s.gender)}
                    </Text>
                  </View>
                  <View className={styles.childMeta}>
                    <Text className={styles.label}>学号</Text>
                    <Text>{s.studentNo || '--'}</Text>
                  </View>
                  <View className={styles.childMeta}>
                    <Text className={styles.label}>出生</Text>
                    <Text>{s.birthDate || '--'}</Text>
                  </View>
                  {s.medicalNotes && (
                    <View className={styles.childMetaLast}>
                      <Text className={styles.label}>备注</Text>
                      <Text>{s.medicalNotes}</Text>
                    </View>
                  )}
                </View>
                <View className={styles.childCardActions}>
                  <Text
                    className={styles.hoursShortcut}
                    onClick={(e) => goHours(e, s.id)}
                  >
                    课时
                  </Text>
                  <Text className={styles.arrowRight}>›</Text>
                </View>
              </Card>
              ))}
            </>
          )}
        </View>
      </View>

      <BottomNav />

    </View>
  );
}
