export type UserRole = 'ADMIN' | 'TEACHER' | 'PARENT';

export interface User {
  id: string;
  username: string;
  role: UserRole;
  phone?: string;
  avatar?: string;
  createTime?: string;
}

export interface TeacherProfile {
  id: string;
  userId: string;
  name: string;
  avatar?: string;
  school?: string;
  title?: string;
  createTime?: string;
}

export interface ParentProfile {
  id: string;
  userId: string;
  name: string;
  avatar?: string;
  phone?: string;
  students?: Student[];
  createTime?: string;
}

export interface Student {
  id: string;
  name: string;
  age?: number;
  gender?: 'MALE' | 'FEMALE' | 'UNKNOWN';
  parentId?: string;
  parentName?: string;
  parentPhone?: string;
  teacherId?: string;
  studentNo?: string;
  createTime?: string;
  birthDate?: string;
  phone?: string;
  medicalNotes?: string;
  remark?: string;
}

export interface LoginParams {
  username: string;
  password: string;
  role?: UserRole;
}

export interface LoginResult {
  token: string;
  user: User;
  profile: TeacherProfile | ParentProfile | null;
}
