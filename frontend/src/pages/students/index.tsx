import { useState, useMemo } from 'react';
import { View, Text, ScrollView, Picker, Textarea } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import CustomButton from '@/components/Button';
import Input from '@/components/Input/index';
import { createChildByParent } from '@/services/api';
import styles from './index.module.scss';

type Gender = 'MALE' | 'FEMALE' | 'UNKNOWN';

const GENDER_LABELS: Record<Gender, string> = {
  MALE: '男',
  FEMALE: '女',
  UNKNOWN: '其他',
};

export default function StudentsPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [name, setName] = useState('');
  const [gender, setGender] = useState<Gender>('MALE');
  const [birthDate, setBirthDate] = useState('');
  const [phone, setPhone] = useState('');
  const [medicalNotes, setMedicalNotes] = useState('');
  const [remark, setRemark] = useState('');

  const todayStr = useMemo(() => new Date().toISOString().slice(0, 10), []);

  const handleSave = async () => {
    if (!name.trim()) {
      return Taro.showToast({ title: '请输入孩子姓名', icon: 'none' });
    }
    if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
      return Taro.showToast({ title: '孩子手机号格式不正确', icon: 'none' });
    }
    setIsLoading(true);
    try {
      const res = await createChildByParent({
        name: name.trim(),
        gender,
        birthDate: birthDate || undefined,
        phone: phone || undefined,
        medicalNotes: medicalNotes.trim() || undefined,
        remark: remark.trim() || undefined,
      });
      Taro.showToast({
        title: '保存成功',
        icon: 'success',
        duration: 1500,
      });
      setTimeout(() => {
        Taro.reLaunch({
          url: '/pages/parent-children/index',
        }).catch(() => {
          Taro.reLaunch({ url: '/pages/parent-home/index' });
        });
      }, 1200);
      // 保存返回的学号等信息，便于调试/观察
      console.log('[录入孩子] saved student:', res);
    } catch (err: any) {
      Taro.showToast({
        title: err?.message || '保存失败，请稍后重试',
        icon: 'none',
        duration: 2500,
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <ScrollView className={styles.studentsPage} scrollY>
      <View className={styles.container}>
        <View className={styles.header}>
          <Text className={styles.pageTitle}>录入自己孩子信息</Text>
          <Text className={styles.pageHint}>
            学号将由系统自动生成，保存后可在"我的孩子"中查看
          </Text>
        </View>

        <Card className={styles.formCard}>
          <Text className={styles.hint}>
            请如实填写孩子的基本资料，保存成功后会自动与您的家长账号绑定。
          </Text>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>孩子姓名 *</Text>
            <Input
              placeholder="请输入孩子真实姓名"
              value={name}
              onChange={(val) => setName(typeof val === 'string' ? val : String(val ?? ''))}
            />
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>性别 *</Text>
            <View className={styles.relationRow}>
              {(['MALE', 'FEMALE', 'UNKNOWN'] as Gender[]).map((k) => (
                <View
                  key={k}
                  className={`${styles.relationChip} ${gender === k ? styles.active : ''}`}
                  onClick={() => setGender(k)}
                >
                  <Text>{GENDER_LABELS[k]}</Text>
                </View>
              ))}
            </View>
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>出生日期</Text>
            <Picker
              mode="date"
              value={birthDate}
              end={todayStr}
              onChange={(e: any) => setBirthDate(e?.detail?.value || '')}
            >
              <View className={styles.pickerBox}>
                <Text className={birthDate ? '' : styles.placeholderText}>
                  {birthDate || '请选择出生日期'}
                </Text>
              </View>
            </Picker>
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>孩子本人手机号（选填）</Text>
            <Input
              type="number"
              placeholder="孩子如有常用手机号可填写"
              value={phone}
              onChange={(val) => setPhone(typeof val === 'string' ? val : String(val ?? ''))}
            />
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>医疗 / 过敏备注</Text>
            <View className={styles.textareaWrap}>
              <Textarea
                className={styles.textarea}
                placeholder="如：花生过敏、哮喘、心脏病、药物禁忌等"
                value={medicalNotes}
                onInput={(e: any) => setMedicalNotes(e?.detail?.value ?? '')}
                maxlength={200}
              />
            </View>
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>备注</Text>
            <View className={styles.textareaWrap}>
              <Textarea
                className={styles.textarea}
                placeholder="其他需要老师了解的信息（选填）"
                value={remark}
                onInput={(e: any) => setRemark(e?.detail?.value ?? '')}
                maxlength={200}
              />
            </View>
          </View>

          <CustomButton
            type="primary"
            block
            loading={isLoading}
            onClick={handleSave}
            className={styles.submitBtn}
          >
            保存孩子资料
          </CustomButton>
        </Card>
      </View>
    </ScrollView>
  );
}
