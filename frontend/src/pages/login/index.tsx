import { useState, useEffect } from 'react';
import { View, Text, Input, Image } from '@tarojs/components';
import Taro from '@tarojs/taro';
import CustomButton from '@/components/Button';
import { LoginParams, UserRole } from '@/types';
import { login } from '@/services/api';
import useAuthStore from '@/store/auth';
import styles from './index.module.scss';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('TEACHER');
  const [isLoading, setIsLoading] = useState(false);
  
  const { login: handleLogin, isLoggedIn, user } = useAuthStore();
  
  useEffect(() => {
    if (isLoggedIn && user) {
      const homeUrl = user.role === 'PARENT' ? '/pages/parent-home/index' : '/pages/home/index';
      Taro.switchTab({ url: homeUrl });
    }
  }, [isLoggedIn, user]);
  
  const handleSubmit = async () => {
    if (!username || !password) {
      Taro.showToast({ title: '请输入账号密码', icon: 'none' });
      return;
    }
    
    setIsLoading(true);
    try {
      const params: LoginParams = { username, password, role };
      const result = await login(params);
      handleLogin(result.token, result.user, result.profile);
      Taro.showToast({ title: '登录成功', icon: 'success' });
      const homeUrl = role === 'PARENT' ? '/pages/parent-home/index' : '/pages/home/index';
      Taro.switchTab({ url: homeUrl });
    } catch (err) {
      console.error('[LoginPage]', err);
      Taro.showToast({ title: '登录失败', icon: 'none' });
    } finally {
      setIsLoading(false);
    }
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
      </View>
    </View>
  );
}
