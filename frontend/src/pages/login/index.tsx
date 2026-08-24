import { useState, useEffect } from 'react';
import { View, Text, Input, Image } from '@tarojs/components';
import Taro from '@tarojs/taro';
import CustomButton from '@/components/Button';
import { LoginParams, UserRole } from '@/types';
import { login, wechatExchangeCode, wechatLogin } from '@/services/api';
import useAuthStore from '@/store/auth';
import styles from './index.module.scss';

type LoginMode = 'password' | 'wechat';

function randomHex8(): string {
  const arr = new Uint8Array(4);
  if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
    crypto.getRandomValues(arr);
  } else {
    for (let i = 0; i < arr.length; i++) arr[i] = Math.floor(Math.random() * 256);
  }
  return Array.from(arr).map((b) => b.toString(16).padStart(2, '0')).join('');
}

function redirectByRole(
  user: { role?: UserRole } | null | undefined,
  opts?: { needReview?: boolean }
): boolean {
  const role = user?.role;
  if (role === 'ADMIN') {
    Taro.showToast({ title: '管理员请使用管理端Web登录', icon: 'none' });
    return false;
  }
  if (!role) {
    if (opts?.needReview) {
      Taro.showToast({ title: '教师申请已提交，等待管理员审核', icon: 'none' });
    }
    return false;
  }
  if (role === 'PARENT') {
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
  const [role, setRole] = useState<UserRole>('TEACHER');
  const [loginMode, setLoginMode] = useState<LoginMode>('password');
  const [isLoading, setIsLoading] = useState(false);

  const { login: handleLogin, isLoggedIn, user } = useAuthStore();

  useEffect(() => {
    if (isLoggedIn && user) {
      redirectByRole(user);
    }
  }, [isLoggedIn, user]);

  const handleSubmit = async () => {
    if (!username || !password) {
      Taro.showToast({ title: '请输入账号密码', icon: 'none' });
      return;
    }

    setIsLoading(true);
    try {
      const params: LoginParams = { username, password };
      const result = await login(params);
      handleLogin(result.token, result.user, result.profile);
      Taro.showToast({ title: '登录成功', icon: 'success' });
      redirectByRole(result.user);
    } catch (err: any) {
      console.error('[LoginPage]', err);
      const msg = err?.message || '登录失败';
      Taro.showToast({ title: msg, icon: 'none' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleWechatLogin = async () => {
    setIsLoading(true);
    try {
      let code: string;
      if (process.env.TARO_ENV === 'h5') {
        code = 'mock_' + randomHex8();
      } else {
        code = await new Promise<string>((resolve, reject) => {
          Taro.login({
            success: (res) => {
              if (res && res.code) resolve(res.code);
              else reject(new Error('微信登录失败：未获取到 code'));
            },
            fail: (err) => reject(err),
          });
        });
      }

      const exchangeRes = await wechatExchangeCode(code);

      if (!exchangeRes?.openid) {
        Taro.showToast({ title: '微信登录失败：未获取到 openid', icon: 'none' });
        return;
      }

      if (exchangeRes.needReview && !exchangeRes.openid) {
        Taro.showToast({ title: '教师申请已提交，等待管理员审核', icon: 'none' });
        return;
      }

      const result = await wechatLogin(exchangeRes.openid, exchangeRes.unionid ?? null);
      handleLogin(result.token, result.user, result.profile);
      Taro.showToast({ title: '登录成功', icon: 'success' });
      redirectByRole(result.user, { needReview: exchangeRes.needReview });
    } catch (err: any) {
      console.error('[LoginPage.wechat]', err);
      const msg = err?.message || '微信登录失败';
      Taro.showToast({ title: msg, icon: 'none' });
    } finally {
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
          src="https://picsum.photos/id/119/200/200"
          mode="aspectFill"
        />
        <Text className={styles.title}>排课与体测管理</Text>
        <Text className={styles.subtitle}>专业的体育训练管理平台</Text>
      </View>

      <View className={styles.form}>
        <View className={styles.roleSelector}>
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
        </View>

        <View className={styles.loginModeTabs}>
          <View
            className={`${styles.loginModeTab} ${loginMode === 'password' ? styles.active : ''}`}
            onClick={() => setLoginMode('password')}
          >
            <Text>密码登录</Text>
          </View>
          <View
            className={`${styles.loginModeTab} ${loginMode === 'wechat' ? styles.active : ''}`}
            onClick={() => setLoginMode('wechat')}
          >
            <Text>微信一键登录</Text>
          </View>
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

            <CustomButton type="primary" size="lg" onClick={handleSubmit} disabled={isLoading}>
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
              <CustomButton
                type="primary"
                size="lg"
                block
                onClick={handleWechatLogin}
                loading={isLoading}
                className={styles.wechatLoginBtn}
              >
                微信一键登录（默认）
              </CustomButton>

              <Text className={styles.agreementText}>
                登录/注册即代表同意《用户协议》《隐私政策》
              </Text>
            </View>
          </>
        )}
      </View>
    </View>
  );
}
