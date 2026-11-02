import { useState, useEffect, useRef } from 'react';
import { View, Text, Input, Image } from '@tarojs/components';
import Taro from '@tarojs/taro';
import CustomButton from '@/components/Button';
import AgreementConsent from '@/components/AgreementConsent';
import { LoginParams, UserRole } from '@/types';
import { login, wechatLogin } from '@/services/api';
import useAuthStore from '@/store/auth';
import appLogo from '@/assets/tabbar/physical-selected.png';
import styles from './index.module.scss';

type LoginMode = 'password' | 'wechat';
type WechatLoginRole = Extract<UserRole, 'TEACHER' | 'PARENT'>;

function redirectByRole(
  user: { role?: UserRole } | null | undefined,
  opts?: { profileComplete?: boolean }
): boolean {
  const role = user?.role;
  if (role === 'ADMIN') {
    Taro.showToast({ title: '管理员请使用管理端Web登录', icon: 'none' });
    return false;
  }
  if (!role) return false;
  if (role === 'PARENT') {
    if (opts?.profileComplete === false) {
      Taro.reLaunch({ url: '/pages/parent-profile-complete/index' });
      return true;
    }
    Taro.reLaunch({ url: '/pages/parent-home/index' });
    return true;
  }
  if (role === 'TEACHER') {
    Taro.reLaunch({ url: '/pages/home/index' });
    return true;
  }
  return false;
}

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<WechatLoginRole>('PARENT');
  const [loginMode, setLoginMode] = useState<LoginMode>('password');
  const [isLoading, setIsLoading] = useState(false);
  const [agreementAccepted, setAgreementAccepted] = useState(false);
  const wechatLoginInFlight = useRef(false);
  const lastProfileRequestAt = useRef(0);

  const { login: handleLogin, isLoggedIn, isValidated, user } = useAuthStore();

  useEffect(() => {
    if (isValidated && isLoggedIn && user) {
      redirectByRole(user, { profileComplete: user.profileComplete });
    }
  }, [isValidated, isLoggedIn, user]);

  const handleSubmit = async () => {
    if (!agreementAccepted) {
      Taro.showToast({ title: '请先阅读并同意用户服务协议和隐私政策', icon: 'none' });
      return;
    }
    if (!username || !password) {
      Taro.showToast({ title: '请输入账号密码', icon: 'none' });
      return;
    }

    setIsLoading(true);
    try {
      const params: LoginParams = { username, password };
      const result = await login(params);
      handleLogin(result.token, result.user, result.profile, result.refreshToken);
      Taro.showToast({ title: '登录成功', icon: 'success' });
    } catch (err: any) {
      console.error('[LoginPage]', err);
      const msg = err?.message || '登录失败';
      Taro.showToast({ title: msg, icon: 'none' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleWechatLogin = async () => {
    if (!agreementAccepted) {
      Taro.showToast({ title: '请先阅读并同意用户服务协议和隐私政策', icon: 'none' });
      return;
    }
    if (process.env.TARO_ENV !== 'weapp') {
      Taro.showToast({ title: '请在微信小程序中使用微信登录', icon: 'none' });
      return;
    }
    if (wechatLoginInFlight.current) return;
    if (Date.now() - lastProfileRequestAt.current < 3000) {
      Taro.showToast({ title: '请稍后再试', icon: 'none' });
      return;
    }
    wechatLoginInFlight.current = true;
    lastProfileRequestAt.current = Date.now();
    setIsLoading(true);
    try {
      let wechatProfile: { avatarUrl?: string; nickName?: string } | undefined;
      try {
        const profileResult = await Taro.getUserProfile({
          desc: '用于展示个人头像和昵称',
        });
        wechatProfile = {
          avatarUrl: profileResult?.userInfo?.avatarUrl,
          nickName: profileResult?.userInfo?.nickName,
        };
      } catch (_) {
        // 用户拒绝头像昵称授权时仍允许继续微信登录。
      }

      const code = await new Promise<string>((resolve, reject) => {
        Taro.login({
          success: (res) => {
            if (res && res.code) resolve(res.code);
            else reject(new Error('微信登录失败：未获取到 code'));
          },
          fail: (err) => reject(err),
        });
      });

      const result = await wechatLogin(code, role, wechatProfile);
      handleLogin(result.token, result.user, result.profile, result.refreshToken);
      Taro.showToast({ title: '登录成功', icon: 'success' });
    } catch (err: any) {
      console.error('[LoginPage.wechat]', err);
      const msg = err?.message || '微信登录失败';
      Taro.showToast({ title: msg, icon: 'none' });
    } finally {
      wechatLoginInFlight.current = false;
      setIsLoading(false);
    }
  };

  const handleGoRegister = () => {
    Taro.navigateTo({ url: '/pages/parent-register/index' });
  };

  return (
    <View className={styles.loginPage}>
      <View className={styles.logoArea}>
        <Image
          className={styles.logo}
          src={appLogo}
          mode="aspectFill"
        />
        <Text className={styles.title}>排课与体测管理</Text>
        <Text className={styles.subtitle}>专业的体育训练管理平台</Text>
      </View>

      <View className={styles.form}>
        {loginMode === 'wechat' && <View className={styles.roleSelector}>
          <View
            className={`${styles.roleOption} ${role === 'TEACHER' ? styles.active : ''}`}
            onClick={() => setRole('TEACHER')}
          >
            <Text>教师登录</Text>
          </View>
          <View
            className={`${styles.roleOption} ${role === 'PARENT' ? styles.active : ''}`}
            onClick={() => setRole('PARENT')}
          >
            <Text>家长登录</Text>
          </View>
        </View>}

        <View className={styles.loginModeTabs}>
          <View
            className={`${styles.loginModeTab} ${loginMode === 'password' ? styles.active : ''}`}
            onClick={() => setLoginMode('password')}
          >
            <Text>密码登录</Text>
          </View>
          {process.env.TARO_ENV === 'weapp' && (
            <View
              className={`${styles.loginModeTab} ${loginMode === 'wechat' ? styles.active : ''}`}
              onClick={() => setLoginMode('wechat')}
            >
              <Text>微信一键登录</Text>
            </View>
          )}
        </View>

        {loginMode === 'password' ? (
          <>
            <View className={styles.inputGroup}>
              <Text className={styles.label}>账号</Text>
              <Input
                className={styles.input}
                placeholder="请输入账号"
                value={username}
                onInput={(e) => setUsername(e.detail.value)}
              />
            </View>

            <View className={styles.inputGroup}>
              <Text className={styles.label}>密码</Text>
              <Input
                className={styles.input}
                password
                placeholder="请输入密码"
                value={password}
                onInput={(e) => setPassword(e.detail.value)}
              />
            </View>

            <CustomButton type="primary" size="lg" onClick={handleSubmit} disabled={isLoading || !agreementAccepted}>
              {isLoading ? '登录中...' : '登录'}
            </CustomButton>

            <View className={styles.registerRow}>
              <Text className={styles.registerLink} onClick={handleGoRegister}>
                还没有账号？立即注册（家长）
              </Text>
            </View>
          </>
        ) : (
          <>
            <View className={styles.wechatLoginWrap}>
              <View className={styles.tips}>
                <Text className={styles.tipText}>
                  {role === 'TEACHER'
                    ? '教师需先由管理员开通账号并绑定微信；已有账号请使用密码登录。'
                    : '首次微信登录需完善家长资料并添加孩子，课程由机构管理员安排。'}
                </Text>
              </View>
              <CustomButton
                type="primary"
                size="lg"
                block
                onClick={handleWechatLogin}
                loading={isLoading}
                disabled={!agreementAccepted}
                className={styles.wechatLoginBtn}
              >
                微信一键登录
              </CustomButton>

            </View>
          </>
        )}

        <AgreementConsent checked={agreementAccepted} onChange={setAgreementAccepted} />
      </View>
    </View>
  );
}
