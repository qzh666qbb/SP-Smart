import { useEffect, useState } from 'react';
import { View, Text } from '@tarojs/components';
import Taro from '@tarojs/taro';
import useAuthStore from '@/store/auth';
import { TABBAR_ACTIVE_EVENT } from '@/utils/tabbar';

interface TabItem {
  key: string;
  pagePath: string;
  text: string;
}

const TEACHER_TABS: TabItem[] = [
  { key: 'home', pagePath: '/pages/home/index', text: '首页' },
  { key: 'physical', pagePath: '/pages/physical/index', text: '体测' },
  { key: 'mine', pagePath: '/pages/mine/index', text: '我的' },
];

const PARENT_TABS: TabItem[] = [
  { key: 'parent-home', pagePath: '/pages/parent-home/index', text: '首页' },
  { key: 'parent-children', pagePath: '/pages/parent-children/index', text: '孩子' },
  { key: 'parent-physical', pagePath: '/pages/parent-physical/index', text: '体测' },
  { key: 'mine', pagePath: '/pages/mine/index', text: '我的' },
];
const ROLE_ROOT_PATHS = [...TEACHER_TABS, ...PARENT_TABS].map((tab) => tab.pagePath);

export default function BottomNav() {
  const { user } = useAuthStore();
  const tabs = user?.role === 'PARENT' ? PARENT_TABS : TEACHER_TABS;
  const [selected, setSelected] = useState(tabs[0].key);

  useEffect(() => {
    const pages = Taro.getCurrentPages();
    const current = pages[pages.length - 1];
    const path = current?.route ? `/${current.route}` : '';
    if (!user) {
      Taro.reLaunch({ url: '/pages/login/index' });
      return;
    }
    if (user.role === 'PARENT' && user.profileComplete === false) {
      Taro.reLaunch({ url: '/pages/parent-profile-complete/index' });
      return;
    }
    const hit = tabs.find((tab) => tab.pagePath === path);
    if (hit) {
      setSelected(hit.key);
    } else if (ROLE_ROOT_PATHS.includes(path)) {
      Taro.reLaunch({ url: tabs[0].pagePath });
    }
  }, [tabs, user]);

  useEffect(() => {
    const handler = (path: string) => {
      const hit = tabs.find((tab) => tab.pagePath === path);
      if (hit) setSelected(hit.key);
    };
    Taro.eventCenter.on(TABBAR_ACTIVE_EVENT, handler);
    return () => {
      Taro.eventCenter.off(TABBAR_ACTIVE_EVENT, handler);
    };
  }, [tabs]);

  return (
    <View
      style={{
        position: 'fixed',
        left: 0,
        right: 0,
        bottom: 0,
        width: '100%',
        height: 'calc(100rpx + env(safe-area-inset-bottom))',
        paddingBottom: 'env(safe-area-inset-bottom)',
        boxSizing: 'border-box',
        display: 'flex',
        flexDirection: 'row',
        backgroundColor: '#ffffff',
        borderTop: '1rpx solid #f0f0f0',
        zIndex: 999,
      }}
    >
      {tabs.map((tab) => {
        const active = selected === tab.key;
        return (
          <View
            key={tab.key}
            onClick={() => {
              if (!active) Taro.reLaunch({ url: tab.pagePath });
            }}
            style={{
              flex: 1,
              minWidth: 0,
              height: '100rpx',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              textAlign: 'center',
            }}
          >
            <Text
              style={{
                fontSize: '28rpx',
                lineHeight: '40rpx',
                whiteSpace: 'nowrap',
                color: active ? '#3b82f6' : '#9ca3af',
                fontWeight: active ? 600 : 400,
              }}
            >
              {tab.text}
            </Text>
          </View>
        );
      })}
    </View>
  );
}
