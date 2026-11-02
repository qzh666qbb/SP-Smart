import { LoginParams, LoginResult, User, TeacherProfile, ParentProfile, Student } from '@/types';

export default function mockLogin(params: LoginParams): LoginResult {
  console.log('[Mock] login:', params);
  
  const mockUser: User = {
    id: params.role === 'PARENT' ? 'P001' : '1',
    username: params.username,
    role: params.role,
    phone: '13800138000',
    avatar: undefined
  };
  
  if (params.role === 'PARENT') {
    const mockStudent: Student = {
      id: 'S001',
      name: '张三',
      age: 12,
      gender: 'MALE',
      parentId: 'P001',
      parentName: params.username,
      parentPhone: '13800138000',
      studentNo: '2024001',
      teacherId: 'T001'
    };
    
    const mockProfile: ParentProfile = {
      id: 'P001',
      userId: 'P001',
      name: '张家长',
      avatar: undefined,
      phone: '13800138000',
      students: [mockStudent]
    };
    
    return {
      token: 'mock-token-' + Date.now(),
      user: mockUser,
      profile: mockProfile
    };
  }
  
  const mockProfile: TeacherProfile = {
    id: '1',
    userId: '1',
    name: '张老师',
    avatar: undefined,
    school: '阳光体育学校',
    title: '高级教练'
  };
  
  return {
    token: 'mock-token-' + Date.now(),
    user: mockUser,
    profile: mockProfile
  };
}
