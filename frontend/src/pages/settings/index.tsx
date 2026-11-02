import { useState, useEffect } from 'react';
import { View, Text, Input, Textarea } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import CustomButton from '@/components/Button';
import useAuthStore from '@/store/auth';
import { TeacherProfile } from '@/types';
import styles from './index.module.scss';

export default function SettingsPage() {
  const { profile } = useAuthStore();
  const [formData, setFormData] = useState({
    name: '',
    avatar: '',
    school: '',
    title: '',
    phone: '',
    email: '',
    description: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (profile) {
      const teacherProfile = profile as TeacherProfile;
      setFormData({
        name: teacherProfile.name || '',
        avatar: teacherProfile.avatar || '',
        school: teacherProfile.school || '',
        title: teacherProfile.title || '',
        phone: '',
        email: '',
        description: ''
      });
    }
  }, []);

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSubmit = async () => {
    if (!formData.name) {
      Taro.showToast({ title: '请输入姓名', icon: 'none' });
      return;
    }

    setIsSubmitting(true);
    try {
      Taro.showToast({ title: '保存成功', icon: 'success' });
    } catch (err) {
      console.error('[SettingsPage]', err);
      Taro.showToast({ title: '保存失败', icon: 'none' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <View className={styles.settingsPage}>
      <Card className={styles.formCard}>
        <Text className={styles.formTitle}>个人信息</Text>

        <View className={styles.formItem}>
          <Text className={styles.formLabel}>姓名</Text>
          <Input
            className={styles.formInput}
            value={formData.name}
            onInput={(e) => handleInputChange('name', e.detail.value)}
            placeholder="请输入姓名"
          />
        </View>

        <View className={styles.formItem}>
          <Text className={styles.formLabel}>学校</Text>
          <Input
            className={styles.formInput}
            value={formData.school}
            onInput={(e) => handleInputChange('school', e.detail.value)}
            placeholder="请输入学校名称"
          />
        </View>

        <View className={styles.formItem}>
          <Text className={styles.formLabel}>职称</Text>
          <Input
            className={styles.formInput}
            value={formData.title}
            onInput={(e) => handleInputChange('title', e.detail.value)}
            placeholder="请输入职称（如：高级教师）"
          />
        </View>

        <View className={styles.formItem}>
          <Text className={styles.formLabel}>联系电话</Text>
          <Input
            className={styles.formInput}
            value={formData.phone}
            onInput={(e) => handleInputChange('phone', e.detail.value)}
            placeholder="请输入联系电话"
            type="number"
          />
        </View>

        <View className={styles.formItem}>
          <Text className={styles.formLabel}>电子邮箱</Text>
          <Input
            className={styles.formInput}
            value={formData.email}
            onInput={(e) => handleInputChange('email', e.detail.value)}
            placeholder="请输入电子邮箱"
            type="text"
          />
        </View>

        <View className={styles.formItem}>
          <Text className={styles.formLabel}>个人简介</Text>
          <Textarea
            className={styles.formInput}
            value={formData.description}
            onInput={(e) => handleInputChange('description', e.detail.value)}
            placeholder="请输入个人简介"
            maxlength={500}
          />
        </View>

        <CustomButton type="primary" size="lg" onClick={handleSubmit} disabled={isSubmitting}>
          {isSubmitting ? '保存中...' : '保存信息'}
        </CustomButton>
      </Card>
    </View>
  );
}
