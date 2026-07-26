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

const mockFunctions: Record<string, (data?: Record<string, any>) => any> = {
  login: mockLogin,
  getSchedules: mockGetSchedules,
  getLessons: mockGetLessons,
  getPhysicalTests: mockGetPhysicalTests,
  addPhysicalTest: mockAddPhysicalTest,
  getHourAccount: mockGetHourAccount,
  getHourTransactions: mockGetHourTransactions,
  createCourse: mockCreateCourse,
  signIn: mockSignIn,
  completeCourse: mockCompleteCourse,
};

export async function callFunction<T = any>(
  name: string,
  data?: Record<string, any>
): Promise<T> {
  const mockFn = mockFunctions[name];
  if (mockFn) {
    console.log('[Mock] callFunction:', name, data);
    return mockFn(data) as T;
  }
  
  console.error(`[Cloud] Mock function not found: ${name}`);
  throw new Error(`未找到 ${name} 的 mock 数据`);
}

export function getDatabase() {
  return null;
}

export async function callWithFallback<T = any>(
  apiFn: () => Promise<T>,
  mockFn: (data?: Record<string, any>) => T,
  data?: Record<string, any>
): Promise<T> {
  try {
    return await apiFn();
  } catch (err) {
    console.warn('[API] Request failed, falling back to mock:', err);
    return mockFn(data) as T;
  }
}
