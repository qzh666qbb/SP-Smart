import { useState, useEffect } from 'react';
import { View, Text, Image } from '@tarojs/components';
import Taro from '@tarojs/taro';
import useAuthStore from '@/store/auth';
import { TABBAR_ACTIVE_EVENT } from '@/utils/tabbar';
import homeIcon from '@/assets/tabbar/home.svg';
import homeSelectedIcon from '@/assets/tabbar/home-selected.svg';
import scheduleIcon from '@/assets/tabbar/schedule.svg';
import scheduleSelectedIcon from '@/assets/tabbar/schedule-selected.svg';
import physicalIcon from '@/assets/tabbar/physical.svg';
import physicalSelectedIcon from '@/assets/tabbar/physical-selected.svg';
import mineIcon from '@/assets/tabbar/mine.svg';
import mineSelectedIcon from '@/assets/tabbar/mine-selected.svg';
import styles from './index.module.scss';

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
  { key: 'home', pagePath: '/pages/home/index', text: '首页', iconPath: homeIcon, selectedIconPath: homeSelectedIcon, isSwitchTab: true },
  { key: 'schedule', pagePath: '/pages/schedule/index', text: '课程', iconPath: scheduleIcon, selectedIconPath: scheduleSelectedIcon, isSwitchTab: true },
  { key: 'physical', pagePath: '/pages/physical/index', text: '体测', iconPath: physicalIcon, selectedIconPath: physicalSelectedIcon, isSwitchTab: true },
  { key: 'mine', pagePath: '/pages/mine/index', text: '我的', iconPath: mineIcon, selectedIconPath: mineSelectedIcon, isSwitchTab: true },
];

const PARENT_TABS: TabItem[] = [
  { key: 'parent-home', pagePath: '/pages/parent-home/index', text: '首页', emoji: '📅' },
  { key: 'parent-children', pagePath: '/pages/parent-children/index', text: '孩子', emoji: '👶' },
  { key: 'parent-physical', pagePath: '/pages/parent-physical/index', text: '体测', iconPath: physicalIcon, selectedIconPath: physicalSelectedIcon },
  { key: 'mine', pagePath: '/pages/mine/index', text: '我的', iconPath: mineIcon, selectedIconPath: mineSelectedIcon, isSwitchTab: true },
];

export default function CustomTabBar() {
  const { user, isLoggedIn } = useAuthStore();
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
    if (tab.isSwitchTab) {
      Taro.switchTab({ url: tab.pagePath }).catch(() => {
        Taro.reLaunch({ url: tab.pagePath });
      });
    } else {
      Taro.reLaunch({ url: tab.pagePath });
    }
  };

  if (!isLoggedIn) return null;

  return (
    <View className={styles.tabBar}>
      {tabs.map((tab) => {
        const isActive = selected === tab.key;
        return (
          <View
            key={tab.key}
            className={`${styles.tabItem} ${isActive ? styles.active : ''}`}
            onClick={() => handleSwitch(tab)}
          >
            {tab.emoji ? (
              <Text className={styles.tabIcon}>{tab.emoji}</Text>
            ) : (
              <Image
                className={styles.tabIconImage}
                src={isActive ? tab.selectedIconPath : tab.iconPath}
                mode="aspectFit"
              />
            )}
            <Text className={styles.tabText}>{tab.text}</Text>
          </View>
        );
      })}
    </View>
  );
}
