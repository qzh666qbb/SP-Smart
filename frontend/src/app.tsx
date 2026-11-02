import { useEffect } from 'react';
import { useDidShow, useDidHide } from '@tarojs/taro';
import { getCurrentUser } from '@/services/api';
import useAuthStore from '@/store/auth';
import { mergeProfile } from '@/utils/profile';
// 全局样式
import './app.scss';

function App(props) {
  useEffect(() => {
    const state = useAuthStore.getState();
    if (!state.token || !state.user) {
      state.setValidated(true);
      return;
    }
    const sessionVersion = state.sessionVersion;
    let active = true;
    getCurrentUser()
      .then(({ user, profile }) => {
        const latest = useAuthStore.getState();
        if (!active || latest.sessionVersion !== sessionVersion || !latest.token || !latest.user || !latest.isLoggedIn) return;
        const mergedProfile = mergeProfile(latest.profile, profile);
        latest.login(latest.token!, { ...latest.user!, ...user }, mergedProfile, latest.refreshToken);
      })
      .catch(() => {
        // 认证失败由请求层统一刷新 token 或清理登录态。
        const latest = useAuthStore.getState();
        if (active && latest.sessionVersion === sessionVersion) latest.setValidated(true);
      });
    return () => { active = false; };
  }, []);

  useDidShow(() => {});

  useDidHide(() => {});

  return props.children;
}

export default App;
