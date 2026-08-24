import { get, post } from '@/utils/request';
import { callWithFallback } from './cloud';
import { LoginParams, LoginResult, User, TeacherProfile, ParentProfile, Student } from '@/types';
import { Lesson, PhysicalTestRecord, StudentHourAccount, HourTransaction } from '@/types';

type AnyMap = Record<string, any>;

function toIdStr(v: any): string {
  return v == null ? '' : String(v);
}

function buildUserFromAuth(data: AnyMap | null): User {
  const d = data || {};
  return {
    id: toIdStr(d.userId ?? d.id),
    username: d.username || '',
    role: (d.role as any) || 'TEACHER',
    phone: d.phone || undefined,
    avatar: d.avatar || undefined,
  };
}

function buildProfileFromAuth(data: AnyMap | null): TeacherProfile | ParentProfile | null {
  const d = data || {};
  const role = d.role;
  const pid = toIdStr(d.profileId ?? d.userId ?? d.id);
  const uid = toIdStr(d.userId ?? d.id);
  if (!pid) return null;
  const name = d.username || (role === 'PARENT' ? '家长' : '老师');
  if (role === 'PARENT') {
    const pp: ParentProfile = {
      id: pid,
      userId: uid,
      name,
      avatar: d.avatar || undefined,
      phone: d.phone || undefined,
      students: [],
    };
    return pp;
  }
  const tp: TeacherProfile = {
    id: pid,
    userId: uid,
    name,
    avatar: d.avatar || undefined,
    school: d.school || undefined,
    title: d.title || undefined,
  };
  return tp;
}

function toStudent(d: AnyMap | null | undefined): Student {
  if (!d) return {} as any;
  return {
    id: toIdStr(d.id),
    name: d.name || '',
    age: d.age ?? undefined,
    gender: (d.gender as any) || undefined,
    parentId: d.parentId != null ? toIdStr(d.parentId) : undefined,
    parentName: d.parentName || undefined,
    parentPhone: d.parentPhone || undefined,
    teacherId: d.primaryTeacherId != null ? toIdStr(d.primaryTeacherId) : (d.teacherId != null ? toIdStr(d.teacherId) : undefined),
    studentNo: d.studentNo || undefined,
    createTime: d.createdAt || d.createTime || undefined,
    birthDate: d.birthDate || undefined,
    phone: d.phone || undefined,
    medicalNotes: d.medicalNotes || d.medicalAllergies || undefined,
    remark: d.remark || undefined,
  };
}

function toLesson(d: AnyMap | null | undefined): Lesson {
  if (!d) return {} as any;
  return {
    id: toIdStr(d.id),
    lessonNo: d.lessonNo || '',
    courseId: d.courseId != null ? toIdStr(d.courseId) : '',
    classGroupId: d.classGroupId != null ? toIdStr(d.classGroupId) : '',
    teacherId: d.teacherId != null ? toIdStr(d.teacherId) : '',
    lessonDate: d.lessonDate || '',
    startTime: d.startTime || '',
    endTime: d.endTime || '',
    status: d.status || 'SCHEDULED',
    location: d.location || undefined,
    consumeHours: d.consumeHours ?? undefined,
  };
}

function toPhysicalRecord(d: AnyMap | null | undefined): PhysicalTestRecord {
  if (!d) return {} as any;
  return {
    id: toIdStr(d.id),
    studentId: toIdStr(d.studentId),
    testerId: d.testerId != null ? toIdStr(d.testerId) : undefined,
    testDate: d.testDate || d.createTime || '',
    status: d.status || 'DRAFT',
    totalScore: d.totalScore ?? undefined,
    gradeLevel: d.gradeLevel ?? undefined,
    scoreLevel: d.scoreLevel ?? undefined,
    details: Array.isArray(d.details) ? d.details : [],
    items: Array.isArray(d.items) ? d.items : [],
  };
}

function toHourAccount(d: AnyMap | null | undefined): StudentHourAccount {
  if (!d) return {} as any;
  return {
    id: toIdStr(d.id),
    studentId: toIdStr(d.studentId),
    studentName: d.studentName || undefined,
    studentNo: d.studentNo || undefined,
    parentName: d.parentName || undefined,
    parentPhone: d.parentPhone || undefined,
    totalHours: d.totalHours ?? d.totalRecharged ?? 0,
    usedHours: d.usedHours ?? d.totalConsumed ?? 0,
    remainingHours: d.remainingHours ?? 0,
    paidHours: d.paidHours ?? 0,
    giftHours: d.giftHours ?? 0,
    createTime: d.createTime ?? d.createdAt ?? undefined,
  };
}

function toHourTransaction(d: AnyMap | null | undefined): HourTransaction {
  if (!d) return {} as any;
  return {
    id: toIdStr(d.id),
    transactionNo: d.transactionNo || '',
    studentId: toIdStr(d.studentId),
    transactionType: d.transactionType || '',
    changeAmount: d.changeAmount ?? 0,
    beforeBalance: d.beforeBalance ?? 0,
    afterBalance: d.afterBalance ?? 0,
    reason: d.reason || '',
    hourSource: d.hourSource || undefined,
    operatorId: d.operatorId != null ? toIdStr(d.operatorId) : undefined,
    operatorRole: d.operatorRole || undefined,
    expireDate: d.expireDate ?? undefined,
    createTime: d.createTime ?? d.createdAt ?? undefined,
  };
}

export const login = async (params: LoginParams): Promise<LoginResult> => {
  return callWithFallback(async () => {
    const body: AnyMap = await post<AnyMap>('/auth/login', {
      username: params.username,
      password: params.password,
    });
    const token: string = body?.accessToken || body?.token || '';
    const user = buildUserFromAuth(body);
    const profile = buildProfileFromAuth(body);
    return { token, user, profile };
  }, undefined);
};

export async function registerParent(params: {
  phone: string;
  password: string;
  realName: string;
  defaultRelation: 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER';
}): Promise<LoginResult> {
  return callWithFallback(async () => {
    const body: AnyMap = await post<AnyMap>('/auth/register-parent', {
      phone: params.phone,
      password: params.password,
      realName: params.realName,
      defaultRelation: params.defaultRelation,
    });
    const token: string = body?.accessToken || body?.token || '';
    const user = buildUserFromAuth(body);
    const profile = buildProfileFromAuth(body);
    return { token, user, profile };
  }, undefined);
}

export async function wechatExchangeCode(
  code: string,
  phoneEncryptedData?: string,
  phoneIv?: string
): Promise<{
  openid: string;
  unionid?: string;
  phone?: string;
  needReview?: boolean;
  pendingRole?: string;
  pendingApplyId?: number | string;
}> {
  return callWithFallback(async () => {
    const payload: AnyMap = { code };
    if (phoneEncryptedData) payload.phoneEncryptedData = phoneEncryptedData;
    if (phoneIv) payload.phoneIv = phoneIv;
    const body: AnyMap = await post<AnyMap>('/auth/wechat/exchange', payload);
    return {
      openid: body?.openid || '',
      unionid: body?.unionid ?? undefined,
      phone: body?.phone ?? undefined,
      needReview: body?.needReview ?? undefined,
      pendingRole: body?.pendingRole ?? undefined,
      pendingApplyId: body?.pendingApplyId ?? undefined,
    };
  }, undefined);
}

export async function wechatLogin(
  openid: string,
  unionid?: string | null
): Promise<LoginResult> {
  return callWithFallback(async () => {
    const payload: AnyMap = { openid };
    if (unionid != null) payload.unionid = unionid;
    const body: AnyMap = await post<AnyMap>('/auth/login/wechat', payload);
    const token: string = body?.accessToken || body?.token || '';
    const user = buildUserFromAuth(body);
    const profile = buildProfileFromAuth(body);
    return { token, user, profile };
  }, undefined);
}

export async function createChildByParent(params: {
  name: string;
  gender: 'MALE' | 'FEMALE' | 'UNKNOWN';
  birthDate?: string;
  phone?: string;
  medicalNotes?: string;
  remark?: string;
}): Promise<Student> {
  return callWithFallback(async () => {
    const payload: AnyMap = { ...params };
    const body: AnyMap = await post<AnyMap>('/parent/students', payload);
    return toStudent(body);
  }, undefined);
}

export async function listParentChildren(): Promise<Student[]> {
  return callWithFallback(async () => {
    const list: AnyMap[] = await get<any[]>('/parent/students') || [];
    return list.map(toStudent);
  }, undefined);
}

export const getCurrentUser = async (): Promise<{ user: User; profile: TeacherProfile | ParentProfile | null }> => {
  return callWithFallback(async () => {
    const body: AnyMap = await get<AnyMap>('/auth/me');
    const user = buildUserFromAuth(body);
    const profile = buildProfileFromAuth(body);
    return { user, profile };
  }, undefined);
};

export const getSchedules = async (params?: { date?: string }): Promise<Lesson[]> => {
  return callWithFallback(async () => {
    const q: AnyMap = {};
    if (params?.date) q.date = params.date;
    const list: AnyMap[] = await get<any[]>('/business/lessons', q) || [];
    return list.map(toLesson);
  }, undefined);
};

export const getLessons = async (params: { studentId: string }): Promise<Lesson[]> => {
  return callWithFallback(async () => {
    const today = new Date();
    const startDate = today.toISOString().split('T')[0];
    const endDate = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    const list: AnyMap[] = await get<any[]>('/parent/schedules', {
      studentId: params.studentId,
      startDate,
      endDate,
    }) || [];
    return list.map(toLesson);
  }, undefined);
};

export const getPhysicalTests = async (params: { studentId: string }): Promise<PhysicalTestRecord[]> => {
  return callWithFallback(async () => {
    const list: AnyMap[] = await get<any[]>(`/parent/physical-tests/${params.studentId}`) || [];
    return list.map(toPhysicalRecord);
  }, undefined);
};

export const addPhysicalTest = async (params: { studentId: string; scores: any[]; testerId?: string; items?: any[]; batchId?: number | string; testDate?: string }): Promise<PhysicalTestRecord> => {
  return callWithFallback(async () => {
    const normalizedScores: AnyMap[] = (params.scores || []).map((s: any) => {
      const out: AnyMap = {
        itemId: Number(s.itemId) != null ? Number(s.itemId) : s.itemId,
      };
      if (s.rawValue != null) out.rawValue = typeof s.rawValue === 'number' ? s.rawValue : Number(s.rawValue);
      else if (s.value != null) out.rawValue = typeof s.value === 'number' ? s.value : Number(s.value);
      if (s.convertedScore != null) {
        out.convertedScore = typeof s.convertedScore === 'number' ? s.convertedScore : Number(s.convertedScore);
      }
      if (s.level != null) out.level = s.level;
      if (s.remark != null) out.remark = s.remark;
      return out;
    }).filter((s: any) => s.itemId != null && s.itemId !== '' && !Number.isNaN(Number(s.itemId)));
    const payload: AnyMap = {
      batchId: params.batchId != null ? (Number(params.batchId) || 1) : 1,
      studentId: Number(params.studentId) || params.studentId,
      testDate: params.testDate || new Date().toISOString().slice(0, 10),
      scores: normalizedScores,
      ...(params.testerId ? { testerId: Number(params.testerId) || params.testerId } : {}),
    };
    const rec: AnyMap = await post<AnyMap>('/business/physical-tests', payload);
    return toPhysicalRecord(rec);
  }, undefined);
};

export const getHourAccount = async (params: { studentId: string }): Promise<StudentHourAccount> => {
  return callWithFallback(async () => {
    const acc: AnyMap = await get<AnyMap>(`/parent/hours/accounts/${params.studentId}`);
    return toHourAccount(acc);
  }, undefined);
};

export const getHourTransactions = async (params: { studentId: string }): Promise<HourTransaction[]> => {
  return callWithFallback(async () => {
    const list: AnyMap[] = await get<any[]>(`/parent/hours/transactions/${params.studentId}`) || [];
    return list.map(toHourTransaction);
  }, undefined);
};

export const createCourse = async (params: { lessonDate: string; startTime: string; endTime: string; courseName: string; studentIds: string[]; courseId?: string | number; teacherId?: string | number; repeatType?: string; weekdays?: string[]; consumeHours?: number | string; seriesName?: string }): Promise<any> => {
  return callWithFallback(async () => {
    let resolvedCourseId = params.courseId != null ? (Number(params.courseId) || params.courseId) : undefined;
    if (resolvedCourseId == null) {
      try {
        const listData: any = await get<any>('/business/courses', { pageNum: 1, pageSize: 10 });
        const firstCourse = (Array.isArray(listData) ? listData[0] : listData?.list?.[0]) || null;
        if (firstCourse?.id) resolvedCourseId = firstCourse.id;
      } catch (_) { /* fallback: 留空，交给后端参数校验报错明确提示 */ }
    }
    const recurrenceType = (params.repeatType || 'NONE').toString().toUpperCase() === 'WEEKLY' ? 'WEEKLY' : 'NONE';
    const startDate = params.lessonDate || new Date().toISOString().slice(0, 10);
    const consumeHours = params.consumeHours != null
      ? (typeof params.consumeHours === 'number' ? params.consumeHours : Number(params.consumeHours))
      : 1.0;
    const payload: AnyMap = {
      ...(resolvedCourseId != null ? { courseId: resolvedCourseId } : {}),
      ...(params.teacherId != null ? { teacherId: Number(params.teacherId) || params.teacherId } : {}),
      recurrenceType,
      startDate,
      startTime: params.startTime,
      endTime: params.endTime,
      consumeHours,
      ...(params.seriesName ? { seriesName: params.seriesName } : {}),
      ...(recurrenceType === 'WEEKLY' && Array.isArray(params.weekdays) && params.weekdays.length > 0 ? { weekdays: params.weekdays } : {}),
    };
    const data: AnyMap = await post<AnyMap>('/business/schedules', payload);
    return data;
  }, undefined);
};

export const signIn = async (params: { lessonId: string; studentIds: string[]; status?: string }): Promise<{ success: boolean }> => {
  return callWithFallback(async () => {
    const attendanceStatus = (params.status || 'PRESENT').toUpperCase();
    const lessonId = Number(params.lessonId) || params.lessonId;
    const ids = (params.studentIds || []).map((id: any) => Number(id) || id).filter((v: any) => v !== null && v !== undefined && v !== '');
    for (const studentId of ids) {
      await post('/business/attendance', {
        lessonId,
        studentId,
        attendanceStatus,
        remark: '',
      });
    }
    return { success: true };
  }, undefined);
};

export const completeCourse = async (params: { lessonId: string }): Promise<{ success: boolean }> => {
  return callWithFallback(async () => {
    await post(`/business/lessons/${params.lessonId}/complete`);
    return { success: true };
  }, undefined);
};

export const startLesson = async (params: { lessonId: string }): Promise<{ success: boolean }> => {
  return callWithFallback(async () => {
    await post(`/business/lessons/${params.lessonId}/start`);
    return { success: true };
  }, undefined);
};

export const getLessonAttendance = async (params: { lessonId: string }): Promise<any[]> => {
  return callWithFallback(async () => {
    const list: AnyMap[] = await get<any[]>(`/business/lessons/${params.lessonId}/attendance`) || [];
    return list.map((d) => ({
      id: toIdStr(d.id ?? d.studentId),
      studentId: toIdStr(d.studentId),
      studentName: d.studentName || '',
      status: d.status || 'UNKNOWN',
    }));
  }, undefined);
};

export const getStudents = async (): Promise<any[]> => {
  return callWithFallback(async () => {
    const list: AnyMap[] = await get<any[]>('/parent/students') || [];
    return list.map(toStudent);
  }, undefined);
};

export const getParentStudents = async (): Promise<any[]> => getStudents();

export const getTeacherStudents = async (params?: { pageNum?: number; pageSize?: number; keyword?: string }): Promise<{ students: Student[]; total: number }> => {
  return callWithFallback(async () => {
    const q: AnyMap = { pageNum: params?.pageNum ?? 1, pageSize: params?.pageSize ?? 50 };
    if (params?.keyword) q.keyword = params.keyword;
    const data: AnyMap = await get<AnyMap>('/business/students', q) || {};
    const list: AnyMap[] = Array.isArray(data?.list) ? data.list : (Array.isArray(data) ? data : []);
    return { students: list.map(toStudent), total: typeof data?.total === 'number' ? data.total : list.length };
  }, undefined);
};

export const getTeacherPhysicalTests = async (params?: { pageNum?: number; pageSize?: number; keyword?: string }): Promise<{ records: PhysicalTestRecord[]; total: number }> => {
  return callWithFallback(async () => {
    const q: AnyMap = { pageNum: params?.pageNum ?? 1, pageSize: params?.pageSize ?? 50 };
    if (params?.keyword) q.keyword = params.keyword;
    const data: AnyMap = await get<AnyMap>('/business/physical-tests', q) || {};
    const list: AnyMap[] = Array.isArray(data?.list) ? data.list : (Array.isArray(data) ? data : []);
    return { records: list.map(toPhysicalRecord), total: typeof data?.total === 'number' ? data.total : list.length };
  }, undefined);
};

export const getStudentById = async (studentId: string): Promise<Student> => {
  return callWithFallback(async () => {
    const data: AnyMap = await get<AnyMap>(`/business/students/${studentId}`);
    return toStudent(data);
  }, undefined);
};

export const getPhysicalTestItems = async (): Promise<any[]> => {
  return callWithFallback(async () => {
    const list: AnyMap[] = await get<any[]>('/business/physical-tests/items') || [];
    return list;
  }, undefined);
};

export const addStudent = async (params: any): Promise<any> => {
  return callWithFallback(async () => {
    const payload: AnyMap = { ...(params || {}) };
    if (payload.teacherId != null) payload.teacherId = Number(payload.teacherId) || payload.teacherId;
    const data: AnyMap = await post<AnyMap>('/business/students', payload);
    return toStudent(data);
  }, undefined);
};

export interface BindMatchParams {
  studentNo: string;
  studentName: string;
  parentPhone: string;
}

export interface BindConfirmParams {
  studentId: string;
  relationType?: 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER';
}

export const matchStudentForBind = async (params: BindMatchParams): Promise<any> => {
  return callWithFallback(async () => {
    const data: AnyMap = await post<AnyMap>('/parent/bind-match', params);
    return toStudent(data);
  }, undefined);
};

export const bindStudent = async (params: BindConfirmParams): Promise<{ success: boolean }> => {
  return callWithFallback(async () => {
    await post('/parent/bind', {
      studentId: Number(params.studentId) || params.studentId,
      relationType: params.relationType || 'OTHER',
    });
    return { success: true };
  }, undefined);
};
