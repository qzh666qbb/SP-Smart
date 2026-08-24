import { useState, useEffect } from 'react';
import { View, Text, Picker, Textarea } from '@tarojs/components';
import Taro from '@tarojs/taro';
import CustomButton from '@/components/Button';
import Card from '@/components/Card';
import Input from '@/components/Input/index';
import { createChildByParent } from '@/services/api';
import { Student } from '@/types';
import styles from './index.module.scss';

type FormGender = 'MALE' | 'FEMALE' | 'UNKNOWN';

function todayStr(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export default function ChildAddEditPage() {
  const [name, setName] = useState('');
  const [gender, setGender] = useState<FormGender>('UNKNOWN');
  const [birthDate, setBirthDate] = useState('');
  const [phone, setPhone] = useState('');
  const [medicalNotes, setMedicalNotes] = useState('');
  const [remark, setRemark] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isFirstChild, setIsFirstChild] = useState(true);

  useEffect(() => {
    try {
      const pages = Taro.getCurrentPages();
      const prev = pages[pages.length - 2];
      const route = (prev as any)?.route || (prev as any)?.$taroPath || '';
      if (/parent-children/.test(String(route))) {
        const anyPrev = prev as any;
        const existingList: any =
          anyPrev?.data?.list ?? anyPrev?.list ?? anyPrev?.childrenList ?? null;
        if (Array.isArray(existingList) && existingList.length > 0) {
          setIsFirstChild(false);
        }
      }
    } catch (_) { /* noop */ }
  }, []);

  const handleField = (field: 'name' | 'phone' | 'medicalNotes' | 'remark', v: any) => {
    const s = typeof v === 'string' ? v : String(v ?? '');
    if (field === 'name') setName(s);
    else if (field === 'phone') setPhone(s);
    else if (field === 'medicalNotes') setMedicalNotes(s);
    else setRemark(s);
  };

  const validate = (): boolean => {
    if (!name.trim()) {
      Taro.showToast({ title: '请输入姓名', icon: 'none' });
      return false;
    }
    return true;
  };

  const handleSubmit = async () => {
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      const created: Student = await createChildByParent({
        name: name.trim(),
        gender,
        birthDate: birthDate || undefined,
        phone: phone.trim() || undefined,
        medicalNotes: medicalNotes.trim() || undefined,
        remark: remark.trim() || undefined,
      });

      if (isFirstChild) {
        Taro.showToast({
          title: '已成功添加孩子，现在您可以为TA充值课时',
          icon: 'none',
          duration: 2500,
        });
      } else {
        Taro.showToast({ title: '保存成功', icon: 'success' });
      }

      try {
        (Taro as any).eventCenter?.trigger?.('parent-children:refresh');
      } catch (_) { /* noop */ }

      setTimeout(() => {
        if (isFirstChild && created?.id) {
          Taro.redirectTo({
            url: `/pages/hour-transaction/index?studentId=${created.id}`,
          }).catch(() => {
            Taro.navigateBack({ delta: 1 }).catch(() => {
              Taro.reLaunch({ url: '/pages/parent-children/index' });
            });
          });
        } else {
          Taro.navigateBack({ delta: 1 }).catch(() => {
            Taro.reLaunch({ url: '/pages/parent-children/index' });
          });
        }
      }, isFirstChild ? 2000 : 1000);
    } catch (err: any) {
      console.error('[ChildAddEdit.submit]', err);
      const msg = err?.message || '保存失败';
      Taro.showToast({ title: msg, icon: 'none' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <View className={styles.addEditPage}>
      <View className={styles.container}>
        <Card className={styles.formCard}>
          <View className={styles.field}>
            <Text className={styles.fieldLabel}>
              <Text className={styles.required}>*</Text>姓名
            </Text>
            <View className={styles.inputWrap}>
              <Input
                placeholder="请输入孩子姓名"
                value={name}
                onChange={(v) => handleField('name', v)}
              />
            </View>
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>
              <Text className={styles.required}>*</Text>性别
            </Text>
            <View className={styles.genderRow}>
              <View
                className={`${styles.genderOption} ${gender === 'MALE' ? styles.active : ''}`}
                onClick={() => setGender('MALE')}
              >
                <Text>男</Text>
              </View>
              <View
                className={`${styles.genderOption} ${gender === 'FEMALE' ? styles.active : ''}`}
                onClick={() => setGender('FEMALE')}
              >
                <Text>女</Text>
              </View>
            </View>
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>出生日期</Text>
            <Picker
              mode="date"
              value={birthDate || todayStr()}
              end={todayStr()}
              onChange={(e) => setBirthDate(String(e.detail.value || ''))}
            >
              <View className={styles.datePickerWrap}>
                <Text className={`${styles.dateValue} ${birthDate ? '' : styles.placeholder}`}>
                  {birthDate || '请选择出生日期'}
                </Text>
              </View>
            </Picker>
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>孩子本人手机号（选填）</Text>
            <View className={styles.inputWrap}>
              <Input
                type="number"
                placeholder="用于接收课程提醒等"
                value={phone}
                onChange={(v) => handleField('phone', v)}
              />
            </View>
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>医疗 / 过敏备注</Text>
            <View className={styles.textareaWrap}>
              <Textarea
                className={styles.textarea}
                placeholder="例如：哮喘、花生过敏、心脏病等（用于训练/体测安全提醒）"
                value={medicalNotes}
                onInput={(e) => handleField('medicalNotes', e.detail.value)}
                maxlength={300}
              />
            </View>
          </View>

          <View className={styles.field}>
            <Text className={styles.fieldLabel}>备注</Text>
            <View className={styles.textareaWrap}>
              <Textarea
                className={styles.textarea}
                placeholder="其他需要登记的信息"
                value={remark}
                onInput={(e) => handleField('remark', e.detail.value)}
                maxlength={300}
              />
            </View>
          </View>
        </Card>
      </View>

      <View className={styles.submitWrap}>
        <CustomButton
          type="primary"
          size="lg"
          block
          loading={isSubmitting}
          onClick={handleSubmit}
        >
          保存孩子资料
        </CustomButton>
      </View>
    </View>
  );
}
