import { useRef } from 'react';
import { View, Text, Image } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import useAuthStore from '@/store/auth';
import { UserRole, TeacherProfile, ParentProfile } from '@/types';
import { notifyTabBarActive } from '@/utils/tabbar';
import BottomNav from '@/components/BottomNav';
import { bindWechat, logoutSession } from '@/services/api';
import { mergeProfile } from '@/utils/profile';
import mineIcon from '@/assets/tabbar/mine-selected.png';
import scheduleIcon from '@/assets/tabbar/schedule-selected.png';
import physicalIcon from '@/assets/tabbar/physical-selected.png';
import styles from './index.module.scss';

interface MenuItem {
  icon: string;
  title: string;
  subtitle?: string;
  url?: string;
  onClick?: () => void;
}

export default function MinePage() {
  Taro.useDidShow(() => notifyTabBarActive('/pages/mine/index'));
  const { user, profile, login: updateAuth, logout, isLoggedIn } = useAuthStore();
  const profileUpdateInFlight = useRef(false);
  const lastProfileRequestAt = useRef(0);

  const handleUpdateWechatProfile = async () => {
    if (process.env.TARO_ENV !== 'weapp') {
      Taro.showToast({ title: '请在微信小程序中操作', icon: 'none' });
      return;
    }
    if (profileUpdateInFlight.current) return;
    if (Date.now() - lastProfileRequestAt.current < 3000) {
      Taro.showToast({ title: '请稍后再试', icon: 'none' });
      return;
    }
    profileUpdateInFlight.current = true;
    lastProfileRequestAt.current = Date.now();
    try {
      let wechatProfile: { avatarUrl?: string; nickName?: string } | undefined;
      try {
        const result = await Taro.getUserProfile({ desc: '用于更新个人头像和昵称' });
        wechatProfile = {
          avatarUrl: result.userInfo?.avatarUrl,
          nickName: result.userInfo?.nickName,
        };
      } catch (_) {
        // 拒绝头像昵称授权不影响绑定微信身份。
      }
      const loginResult = await new Promise<Taro.login.SuccessCallbackResult>((resolve, reject) => {
        Taro.login({ success: resolve, fail: reject });
      });
      if (!loginResult.code) throw new Error('未获取到微信登录凭证');
      const bound = await bindWechat(loginResult.code, wechatProfile);
      const mergedProfile = mergeProfile(profile, bound.profile);
      updateAuth(bound.token, bound.user, mergedProfile, bound.refreshToken);
      Taro.showToast({ title: user?.wechatBound ? '头像昵称已更新' : '微信绑定成功', icon: 'success' });
    } catch (error: any) {
      const message = error?.errMsg?.includes('cancel') || error?.errMsg?.includes('deny')
        ? '已取消授权'
        : (error?.message || '更新失败');
      Taro.showToast({ title: message, icon: 'none' });
    } finally {
      profileUpdateInFlight.current = false;
    }
  };
  
  const getRoleText = (role: UserRole) => {
    const roleMap: Record<UserRole, string> = {
      ADMIN: '管理员',
      TEACHER: '教师',
      PARENT: '家长'
    };
    return roleMap[role];
  };
  
  const getRoleTag = (role: UserRole) => {
    const typeMap: Record<UserRole, 'primary' | 'success' | 'warning' | 'danger'> = {
      ADMIN: 'danger',
      TEACHER: 'primary',
      PARENT: 'success'
    };
    return <Tag type={typeMap[role]}>{getRoleText(role)}</Tag>;
  };
  
  const wechatMenuItems: MenuItem[] = process.env.TARO_ENV === 'weapp'
    ? [{ icon: mineIcon, title: user?.wechatBound ? '更新微信头像昵称' : '绑定微信', subtitle: '使用当前微信资料', onClick: handleUpdateWechatProfile }]
    : [];

  const teacherMenuItems: MenuItem[] = [
    ...wechatMenuItems,
    { icon: scheduleIcon, title: '课程流水', subtitle: '查看全部上课记录', url: '/pages/hour-transaction/index' },
    { icon: physicalIcon, title: '学员管理', subtitle: '选择学员查看详情', url: '/pages/teacher-students/index' },
    { icon: mineIcon, title: '设置', subtitle: '个人信息', url: '/pages/settings/index' },
  ];
  
  const parentMenuItems: MenuItem[] = [
    ...wechatMenuItems,
    { icon: scheduleIcon, title: '课程记录', subtitle: '查看孩子上课记录', url: '/pages/parent-course/index' },
    { icon: physicalIcon, title: '体测成绩', subtitle: '查看孩子体测成绩', url: '/pages/parent-physical/index' },
    { icon: mineIcon, title: '设置', subtitle: '个人信息', url: '/pages/settings/index' },
  ];
  
  const menuItems = user?.role === 'PARENT' ? parentMenuItems : teacherMenuItems;
  
  const handleMenuClick = (item: MenuItem) => {
    if (item.url) {
      Taro.navigateTo({ url: item.url });
    } else if (item.onClick) {
      item.onClick();
    }
  };
  
  const handleLogout = () => {
    Taro.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await logoutSession();
          } catch (error) {
            console.warn('[MinePage.logout]', error);
          }
          logout();
          Taro.redirectTo({ url: '/pages/login/index' });
        }
      }
    });
  };
  
  if (!isLoggedIn) {
    return (
      <View className={styles.minePage}>
        <Card className={styles.loginCard}>
          <Text className={styles.loginText}>请先登录</Text>
          <CustomButton type="primary" onClick={() => Taro.navigateTo({ url: '/pages/login/index' })}>
            去登录
          </CustomButton>
        </Card>
      </View>
    );
  }
  
  return (
    <View className={styles.minePage}>
      <Card className={styles.userCard}>
        <View className={styles.userInfo}>
          <Image 
            className={styles.avatar} 
            src={profile?.avatar || user?.avatar || mineIcon}
            mode="aspectFill"
          />
          <View className={styles.userDetail}>
            <Text className={styles.userName}>{profile?.name || user?.username}</Text>
            <View className={styles.userMeta}>
              <Text className={styles.userRole}>{getRoleTag(user!.role)}</Text>
              {user?.role === 'TEACHER' && (profile as TeacherProfile | null)?.school && (
                <Text className={styles.userSchool}>{(profile as TeacherProfile | null)?.school}</Text>
              )}
              {user?.role === 'PARENT' && (profile as ParentProfile | null)?.students?.[0] && (
                <Text className={styles.userSchool}>绑定孩子: {(profile as ParentProfile | null)?.students![0].name}</Text>
              )}
            </View>
          </View>
        </View>
      </Card>
      
      <Card className={styles.menuCard}>
        {menuItems.map((item, index) => (
          <View 
            key={index} 
            className={`${styles.menuItem} ${index !== menuItems.length - 1 ? styles.hasBorder : ''}`}
            onClick={() => handleMenuClick(item)}
          >
            <Image className={styles.menuIcon} src={item.icon} mode="aspectFill" />
            <View className={styles.menuContent}>
              <Text className={styles.menuTitle}>{item.title}</Text>
              {item.subtitle && (
                <Text className={styles.menuSubtitle}>{item.subtitle}</Text>
              )}
            </View>
            <Text className={styles.menuArrow}>›</Text>
          </View>
        ))}
      </Card>
      
      <Card className={styles.logoutCard}>
        <CustomButton type="secondary" size="lg" onClick={handleLogout}>
          退出登录
        </CustomButton>
      </Card>
      
      <View className={styles.version}>
        <Text className={styles.versionText}>版本 1.0.0</Text>
      </View>
      <BottomNav />
    </View>
  );
}
