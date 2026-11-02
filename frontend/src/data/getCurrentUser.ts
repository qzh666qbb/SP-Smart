import { User, TeacherProfile } from '@/types';

export default function mockGetCurrentUser(): { user: User; profile: TeacherProfile | null } {
  console.log('[Mock] getCurrentUser');
  
  const user: User = {
    id: '1',
    username: 'teacher001',
    role: 'TEACHER',
    phone: '13800138000',
    avatar: undefined
  };
  
  const profile: TeacherProfile = {
    id: '1',
    userId: '1',
    name: '张老师',
    avatar: undefined,
    school: '阳光体育学校',
    title: '高级教练'
  };
  
  return { user, profile };
}
