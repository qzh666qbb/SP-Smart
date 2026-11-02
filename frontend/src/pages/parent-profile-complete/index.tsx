import { useState } from 'react';
import { View, Text, Input } from '@tarojs/components';
import Taro from '@tarojs/taro';
import CustomButton from '@/components/Button';
import { completeParentProfile } from '@/services/api';
import useAuthStore from '@/store/auth';
import styles from '../parent-register/index.module.scss';

type Relation = 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER';
const RELATIONS: { key: Relation; label: string }[] = [
  { key: 'FATHER', label: '爸爸' },
  { key: 'MOTHER', label: '妈妈' },
  { key: 'GUARDIAN', label: '监护人' },
  { key: 'OTHER', label: '其他' },
];
const PHONE_REGEX = /^1[3-9]\d{9}$/;

export default function ParentProfileCompletePage() {
  const { user, login } = useAuthStore();
  const [name, setName] = useState(user?.nickname || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [relation, setRelation] = useState<Relation>('MOTHER');
  const [submitting, setSubmitting] = useState(false);

  const submit = async () => {
    if (!name.trim()) return void Taro.showToast({ title: '请输入真实姓名', icon: 'none' });
    if (!PHONE_REGEX.test(phone.trim())) return void Taro.showToast({ title: '手机号格式不正确', icon: 'none' });
    setSubmitting(true);
    try {
      const result = await completeParentProfile({ name: name.trim(), phone: phone.trim(), relation });
      login(result.token, result.user, result.profile, result.refreshToken);
      Taro.showToast({ title: '资料已完善', icon: 'success' });
      setTimeout(() => Taro.reLaunch({ url: '/pages/parent-children/index' }), 500);
    } catch (error: any) {
      Taro.showToast({ title: error?.message || '保存失败', icon: 'none' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <View className={styles.registerPage}>
      <View className={styles.logoArea}>
        <Text className={styles.title}>完善家长资料</Text>
        <Text className={styles.subtitle}>完善后即可添加和管理孩子</Text>
      </View>
      <View className={styles.form}>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>真实姓名</Text>
          <View className={styles.inputWrap}>
            <Input value={name} placeholder="请输入真实姓名" onInput={(e: any) => setName(e.detail.value)} />
          </View>
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>手机号</Text>
          <View className={styles.inputWrap}>
            <Input type="number" value={phone} placeholder="请输入手机号" onInput={(e: any) => setPhone(e.detail.value)} />
          </View>
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>与孩子关系</Text>
          <View className={styles.relationRow}>
            {RELATIONS.map((item) => (
              <View key={item.key}
                className={`${styles.relationChip} ${relation === item.key ? styles.active : ''}`}
                onClick={() => setRelation(item.key)}>
                <Text>{item.label}</Text>
              </View>
            ))}
          </View>
        </View>
        <CustomButton type="primary" size="lg" block loading={submitting} onClick={submit}>
          {submitting ? '保存中...' : '保存并继续'}
        </CustomButton>
      </View>
    </View>
  );
}
