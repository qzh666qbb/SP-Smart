import { useState } from 'react';
import { View, Text, Input, Image } from '@tarojs/components';
import Taro from '@tarojs/taro';
import CustomButton from '@/components/Button';
import AgreementConsent from '@/components/AgreementConsent';
import { registerParent } from '@/services/api';
import useAuthStore from '@/store/auth';
import appLogo from '@/assets/tabbar/physical-selected.png';
import styles from './index.module.scss';

type Relation = 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER';

const RELATION_OPTIONS: { key: Relation; label: string }[] = [
  { key: 'FATHER', label: '爸爸' },
  { key: 'MOTHER', label: '妈妈' },
  { key: 'GUARDIAN', label: '监护人' },
  { key: 'OTHER', label: '其他' },
];

const PHONE_REGEX = /^1[3-9]\d{9}$/;

export default function ParentRegisterPage() {
  const [phone, setPhone] = useState('');
  const [realName, setRealName] = useState('');
  const [defaultRelation, setDefaultRelation] = useState<Relation>('MOTHER');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [agreementAccepted, setAgreementAccepted] = useState(false);

  const { login: handleLogin } = useAuthStore();

  const handleGoLogin = () => {
    Taro.navigateBack({ delta: 1 }).catch(() => {
      Taro.reLaunch({ url: '/pages/login/index' });
    });
  };

  const validate = (): boolean => {
    if (!agreementAccepted) {
      Taro.showToast({ title: '请先阅读并同意用户服务协议和隐私政策', icon: 'none' });
      return false;
    }
    if (!phone.trim()) {
      Taro.showToast({ title: '请输入手机号', icon: 'none' });
      return false;
    }
    if (!PHONE_REGEX.test(phone.trim())) {
      Taro.showToast({ title: '手机号格式不正确', icon: 'none' });
      return false;
    }
    if (!realName.trim()) {
      Taro.showToast({ title: '请输入真实姓名', icon: 'none' });
      return false;
    }
    if (!password || password.length < 6) {
      Taro.showToast({ title: '密码至少 6 位', icon: 'none' });
      return false;
    }
    if (password !== confirmPassword) {
      Taro.showToast({ title: '两次输入的密码不一致，已清空请重新输入', icon: 'none' });
      setPassword('');
      setConfirmPassword('');
      return false;
    }
    return true;
  };

  const handleSubmit = async () => {
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      const result = await registerParent({
        phone: phone.trim(),
        password,
        realName: realName.trim(),
        defaultRelation,
      });
      handleLogin(result.token, result.user, result.profile, result.refreshToken);
      Taro.showToast({ title: '注册并登录成功', icon: 'success' });
      setTimeout(() => {
        Taro.reLaunch({ url: '/pages/parent-children/index' });
      }, 1200);
    } catch (err: any) {
      console.error('[ParentRegister.submit]', err);
      const msg = err?.message || '注册失败';
      Taro.showToast({ title: msg, icon: 'none' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <View className={styles.registerPage}>
      <View className={styles.logoArea}>
        <Image
          className={styles.logo}
          src={appLogo}
          mode="aspectFill"
        />
        <Text className={styles.title}>家长注册</Text>
        <Text className={styles.subtitle}>完善信息后注册并登录</Text>
      </View>

      <View className={styles.form}>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>手机号</Text>
          <View className={styles.inputWrap}>
            <Input
              type="number"
              placeholder="请输入手机号"
              value={phone}
              onInput={(e: any) => setPhone(e?.detail?.value ?? '')}
            />
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.fieldLabel}>真实姓名</Text>
          <View className={styles.inputWrap}>
            <Input
              placeholder="请输入真实姓名"
              value={realName}
              onInput={(e: any) => setRealName(e?.detail?.value ?? '')}
            />
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.fieldLabel}>默认与孩子关系</Text>
          <View className={styles.relationRow}>
            {RELATION_OPTIONS.map((opt) => (
              <View
                key={opt.key}
                className={`${styles.relationChip} ${defaultRelation === opt.key ? styles.active : ''}`}
                onClick={() => setDefaultRelation(opt.key)}
              >
                <Text>{opt.label}</Text>
              </View>
            ))}
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.fieldLabel}>密码</Text>
          <View className={styles.inputWrap}>
            <Input
              password
              placeholder="请输入密码（至少 6 位）"
              value={password}
              onInput={(e: any) => setPassword(e?.detail?.value ?? '')}
            />
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.fieldLabel}>确认密码</Text>
          <View className={styles.inputWrap}>
            <Input
              password
              placeholder="请再次输入密码"
              value={confirmPassword}
              onInput={(e: any) => setConfirmPassword(e?.detail?.value ?? '')}
            />
          </View>
        </View>

        <CustomButton
          type="primary"
          size="lg"
          block
          loading={isSubmitting}
          disabled={!agreementAccepted}
          onClick={handleSubmit}
          className={styles.submitBtn}
        >
          立即注册并登录
        </CustomButton>

        <AgreementConsent checked={agreementAccepted} onChange={setAgreementAccepted} />

        <View className={styles.haveAccountRow}>
          <Text className={styles.link} onClick={handleGoLogin}>
            已有账号？返回登录
          </Text>
        </View>
      </View>
    </View>
  );
}
