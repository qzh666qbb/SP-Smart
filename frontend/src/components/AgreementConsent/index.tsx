import { View, Text } from '@tarojs/components';
import Taro from '@tarojs/taro';
import styles from './index.module.scss';

interface AgreementConsentProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
}

export default function AgreementConsent({ checked, onChange }: AgreementConsentProps) {
  const openServiceAgreement = (event: any) => {
    event?.stopPropagation?.();
    Taro.navigateTo({ url: '/pages/agreement/index?type=service' });
  };

  const openPrivacyPolicy = (event: any) => {
    event?.stopPropagation?.();
    const wechat = (globalThis as any).wx;
    if (process.env.TARO_ENV === 'weapp' && typeof wechat?.openPrivacyContract === 'function') {
      wechat.openPrivacyContract({
        fail: () => Taro.navigateTo({ url: '/pages/agreement/index?type=privacy' }),
      });
      return;
    }
    Taro.navigateTo({ url: '/pages/agreement/index?type=privacy' });
  };

  return (
    <View
      className={styles.consentRow}
      onClick={() => onChange(!checked)}
      ariaRole="checkbox"
      ariaLabel={`${checked ? '已勾选' : '未勾选'}，阅读并同意用户服务协议及隐私政策`}
    >
      <View className={`${styles.checkbox} ${checked ? styles.checked : ''}`}>
        {checked && <Text className={styles.checkmark}>✓</Text>}
      </View>
      <View className={styles.consentContent}>
        <View className={styles.consentText}>
          <Text>我已阅读并同意</Text>
          <Text className={styles.link} onClick={openServiceAgreement}>《用户服务协议》</Text>
          <Text>和</Text>
          <Text className={styles.link} onClick={openPrivacyPolicy}>《隐私政策》</Text>
        </View>
        <Text className={styles.consentHint}>请自主选择；未勾选时不会提交登录或注册信息</Text>
      </View>
    </View>
  );
}
