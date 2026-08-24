import { View, Text, Image, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import Card from '@/components/Card';
import Tag from '@/components/Tag';
import CustomButton from '@/components/Button';
import useAuthStore from '@/store/auth';
import { UserRole, TeacherProfile, ParentProfile } from '@/types';
import { notifyTabBarActive } from '@/utils/tabbar';
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
  const { user, profile, logout, isLoggedIn } = useAuthStore();
  
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
  
  const teacherMenuItems: MenuItem[] = [
    { icon: 'https://picsum.photos/id/3/64/64', title: '课程流水', subtitle: '查看上课记录', url: '/pages/hour-transaction/index?studentId=1' },
    { icon: 'https://picsum.photos/id/4/64/64', title: '学员详情', subtitle: '查看学员信息', url: '/pages/students/index' },
    { icon: 'https://picsum.photos/id/6/64/64', title: '设置', subtitle: '个人信息', url: '/pages/settings/index' },
  ];
  
  const parentMenuItems: MenuItem[] = [
    { icon: 'https://picsum.photos/id/3/64/64', title: '课程记录', subtitle: '查看孩子上课记录', url: '/pages/parent-course/index' },
    { icon: 'https://picsum.photos/id/5/64/64', title: '体测成绩', subtitle: '查看孩子体测成绩', url: '/pages/parent-physical/index' },
    { icon: 'https://picsum.photos/id/6/64/64', title: '设置', subtitle: '个人信息', url: '/pages/settings/index' },
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
      success: (res) => {
        if (res.confirm) {
          logout();
          Taro.redirectTo({ url: '/pages/login/index' });
        }
      }
    });
  };
  
  if (!isLoggedIn) {
    return (
      <ScrollView className={styles.minePage} scrollY>
        <Card className={styles.loginCard}>
          <Text className={styles.loginText}>请先登录</Text>
          <CustomButton type="primary" onClick={() => Taro.navigateTo({ url: '/pages/login/index' })}>
            去登录
          </CustomButton>
        </Card>
      </ScrollView>
    );
  }
  
  return (
    <ScrollView className={styles.minePage} scrollY>
      <Card className={styles.userCard}>
        <View className={styles.userInfo}>
          <Image 
            className={styles.avatar} 
            src={profile?.avatar || user?.avatar || 'https://picsum.photos/id/64/200/200'} 
            mode="aspectFill"
          />
          <View className={styles.userDetail}>
            <Text className={styles.userName}>{profile?.name || user?.username}</Text>
            <View className={styles.userMeta}>
              <Text className={styles.userRole}>{getRoleTag(user!.role)}</Text>
              {user?.role === 'TEACHER' && (profile as TeacherProfile).school && (
                <Text className={styles.userSchool}>{(profile as TeacherProfile).school}</Text>
              )}
              {user?.role === 'PARENT' && (profile as ParentProfile).students?.[0] && (
                <Text className={styles.userSchool}>绑定孩子: {(profile as ParentProfile).students![0].name}</Text>
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
    </ScrollView>
  );
}
