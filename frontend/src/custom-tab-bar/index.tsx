import { useState, useEffect } from 'react';
import { View, Text } from '@tarojs/components';
import Taro from '@tarojs/taro';
import useAuthStore from '@/store/auth';
import { TABBAR_ACTIVE_EVENT } from '@/utils/tabbar';

type Role = 'TEACHER' | 'PARENT';

interface TabItem {
  key: string;
  pagePath: string;
  text: string;
  iconPath?: string;
  selectedIconPath?: string;
  emoji?: string;
  isSwitchTab?: boolean;
}

const TEACHER_TABS: TabItem[] = [
  { key: 'home', pagePath: '/pages/home/index', text: '首页' },
  { key: 'physical', pagePath: '/pages/physical/index', text: '体测' },
  { key: 'mine', pagePath: '/pages/mine/index', text: '我的' },
];

const PARENT_TABS: TabItem[] = [
  { key: 'parent-home', pagePath: '/pages/parent-home/index', text: '首页', emoji: '📅' },
  { key: 'parent-children', pagePath: '/pages/parent-children/index', text: '孩子', emoji: '👶' },
  { key: 'parent-physical', pagePath: '/pages/parent-physical/index', text: '体测' },
  { key: 'mine', pagePath: '/pages/mine/index', text: '我的' },
];

export default function CustomTabBar() {
  const { user } = useAuthStore();
  const role: Role = user?.role === 'PARENT' ? 'PARENT' : 'TEACHER';
  const tabs = role === 'PARENT' ? PARENT_TABS : TEACHER_TABS;
  const [selected, setSelected] = useState<string>(tabs[0].key);

  useEffect(() => {
    const pages = Taro.getCurrentPages();
    if (pages && pages.length > 0) {
      const current = pages[pages.length - 1];
      const route = current?.route ? `/${current.route}` : '';
      const hit = tabs.find(t => t.pagePath === route);
      if (hit) setSelected(hit.key);
    }
  }, []);

  useEffect(() => {
    const handler = (path: string) => {
      const hit = tabs.find(t => t.pagePath === path);
      if (hit) setSelected(hit.key);
    };
    Taro.eventCenter.on(TABBAR_ACTIVE_EVENT, handler);
    return () => {
      Taro.eventCenter.off(TABBAR_ACTIVE_EVENT, handler);
    };
  }, [tabs]);

  const handleSwitch = (tab: TabItem) => {
    if (selected === tab.key) return;
    Taro.reLaunch({ url: tab.pagePath });
  };

  return (
    <View
      className="custom-tab-bar"
      style={{
        position: 'fixed',
        left: 0,
        right: 0,
        bottom: 0,
        width: '100%',
        height: '100rpx',
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'stretch',
        backgroundColor: '#ffffff',
        borderTop: '1rpx solid #f0f0f0',
        zIndex: 999,
        boxSizing: 'border-box',
      }}
    >
      {tabs.map((tab) => {
        const isActive = selected === tab.key;
        return (
          <View
            key={tab.key}
            className={`custom-tab-item ${isActive ? 'custom-tab-item-active' : ''}`}
            style={{
              flex: 1,
              minWidth: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              textAlign: 'center',
            }}
            onClick={() => handleSwitch(tab)}
          >
            <Text
              className="custom-tab-text"
              style={{
                display: 'block',
                fontSize: '28rpx',
                lineHeight: 1,
                whiteSpace: 'nowrap',
                color: isActive ? '#3b82f6' : '#9ca3af',
                fontWeight: isActive ? 600 : 400,
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
