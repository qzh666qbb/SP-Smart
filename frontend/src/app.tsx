import { useEffect, useRef } from 'react';
import { useDidShow, useDidHide } from '@tarojs/taro';
import { getCurrentUser } from '@/services/api';
import useAuthStore from '@/store/auth';
import { mergeProfile } from '@/utils/profile';
// 全局样式
import './app.scss';

function App(props) {
  const revision = useRef(0);
  const refreshSession = () => {
    const requestId = ++revision.current;
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
        if (!active || requestId !== revision.current || latest.sessionVersion !== sessionVersion || !latest.token || !latest.user || !latest.isLoggedIn) return;
        const mergedProfile = mergeProfile(latest.profile, profile);
        latest.syncIdentity({ ...latest.user!, ...user }, mergedProfile);
      })
      .catch(() => {
        // 认证失败由请求层统一刷新 token 或清理登录态。
        const latest = useAuthStore.getState();
        if (active && latest.sessionVersion === sessionVersion) latest.setValidated(true);
      });
    return () => { active = false; };
  };
  useEffect(refreshSession, []);
  useDidShow(() => { refreshSession(); });

  useDidHide(() => {});

  return props.children;
}

export default App;
