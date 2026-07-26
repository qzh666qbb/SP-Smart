import { get, post } from '@/utils/request';
import { callWithFallback } from './cloud';
import mockLogin from '../data/login';
import mockGetSchedules from '../data/getSchedules';
import mockGetLessons from '../data/getLessons';
import mockGetPhysicalTests from '../data/getPhysicalTests';
import mockAddPhysicalTest from '../data/addPhysicalTest';
import mockGetHourAccount from '../data/getHourAccount';
import mockGetHourTransactions from '../data/getHourTransactions';
import mockCreateCourse from '../data/createCourse';
import mockSignIn from '../data/signIn';
import mockCompleteCourse from '../data/completeCourse';
import { LoginParams, LoginResult, User, TeacherProfile, ParentProfile } from '@/types';
import { Lesson, PhysicalTestRecord, StudentHourAccount, HourTransaction } from '@/types';

export const login = async (params: LoginParams): Promise<LoginResult> => {
  return callWithFallback(
    () => post<LoginResult>('/auth/login', params),
    () => mockLogin(params)
  );
};

export const getCurrentUser = async (): Promise<{ user: User; profile: TeacherProfile | ParentProfile | null }> => {
  return callWithFallback(
    () => get('/auth/me'),
    () => ({
      user: { id: '1', username: 'teacher', role: 'TEACHER', phone: '13800138000', avatar: '' },
      profile: { id: '1', userId: '1', name: '张老师', avatar: '', school: '阳光体育学校', title: '高级教练' } as TeacherProfile
    })
  );
};

export const getSchedules = async (params?: { date?: string }): Promise<Lesson[]> => {
  return callWithFallback(
    () => get('/business/lessons', params),
    () => mockGetSchedules(params).schedules
  );
};

export const getLessons = async (params: { studentId: string }): Promise<Lesson[]> => {
  const today = new Date();
  const startDate = today.toISOString().split('T')[0];
  const endDate = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  return callWithFallback(
    () => get('/parent/schedules', { ...params, startDate, endDate }),
    () => mockGetLessons(params).lessons
  );
};

export const getPhysicalTests = async (params: { studentId: string }): Promise<PhysicalTestRecord[]> => {
  return callWithFallback(
    () => get('/business/physical-tests', params),
    () => mockGetPhysicalTests(params).records
  );
};

export const addPhysicalTest = async (params: { studentId: string; scores: any[] }): Promise<PhysicalTestRecord> => {
  return callWithFallback(
    () => post('/business/physical-tests', params),
    () => mockAddPhysicalTest(params).record
  );
};

export const getHourAccount = async (params: { studentId: string }): Promise<StudentHourAccount> => {
  return callWithFallback(
    () => get(`/business/hour-accounts/${params.studentId}`),
    () => mockGetHourAccount(params).account
  );
};

export const getHourTransactions = async (params: { studentId: string }): Promise<HourTransaction[]> => {
  return callWithFallback(
    () => get(`/business/hour-accounts/${params.studentId}/transactions`),
    () => mockGetHourTransactions(params).transactions
  );
};

export const createCourse = async (params: { lessonDate: string; startTime: string; endTime: string; courseName: string; studentIds: string[] }): Promise<any> => {
  return callWithFallback(
    () => post('/business/schedules', params),
    () => mockCreateCourse(params).lesson
  );
};

export const signIn = async (params: { lessonId: string; studentIds: string[] }): Promise<{ success: boolean }> => {
  return callWithFallback(
    () => post('/business/attendance', { lessonId: params.lessonId, studentIds: params.studentIds }),
    () => mockSignIn(params)
  );
};

export const completeCourse = async (params: { lessonId: string }): Promise<{ success: boolean }> => {
  return callWithFallback(
    () => post(`/business/lessons/${params.lessonId}/complete`),
    () => mockCompleteCourse(params)
  );
};

export const getLessonAttendance = async (params: { lessonId: string }): Promise<any[]> => {
  return callWithFallback(
    () => get(`/business/lessons/${params.lessonId}/attendance`),
    () => []
  );
};

export const startLesson = async (params: { lessonId: string }): Promise<{ success: boolean }> => {
  return callWithFallback(
    () => post(`/business/lessons/${params.lessonId}/start`),
    () => ({ success: true })
  );
};

export const getStudents = async (): Promise<any[]> => {
  return callWithFallback(
    () => get('/business/students'),
    () => []
  );
};

export const addStudent = async (params: any): Promise<any> => {
  return callWithFallback(
    () => post('/business/students', params),
    () => params
  );
};

export const bindStudent = async (params: { studentNo: string; phone: string }): Promise<{ success: boolean }> => {
  return callWithFallback(
    () => post('/parent/bind-student', params),
    () => ({ success: true })
  );
};

export const getParentStudents = async (): Promise<any[]> => {
  return callWithFallback(
    () => get('/parent/students'),
    () => []
  );
};
