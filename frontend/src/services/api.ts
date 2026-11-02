import { get, post } from '@/utils/request';
import { callWithFallback } from './cloud';
import { collectPages } from '@/utils/pagination';
import { LoginParams, LoginResult, User, UserRole, TeacherProfile, ParentProfile, Student } from '@/types';
import { Lesson, PhysicalTestRecord, PhysicalScoreDetail, StudentHourAccount, HourTransaction } from '@/types';

type AnyMap = Record<string, any>;

interface PhysicalScoreResponse {
  id?: string | number | null;
  recordId?: string | number | null;
  itemId?: string | number | null;
  itemName?: string | null;
  rawValue?: number | null;
  convertedScore?: number | null;
  level?: PhysicalScoreDetail['level'] | null;
  remark?: string | null;
  score?: number | null;
  unit?: string | null;
  name?: string | null;
  value?: number | null;
  convertedLevel?: PhysicalScoreDetail['level'] | null;
}

function toIdStr(v: any): string {
  return v == null ? '' : String(v);
}

function buildUserFromAuth(data: AnyMap | null): User {
  const d = data || {};
  return {
    id: toIdStr(d.userId ?? d.id),
    username: d.username || '',
    nickname: d.nickname || undefined,
    role: (d.role as any) || 'TEACHER',
    phone: d.phone || undefined,
    avatar: d.avatar || undefined,
    wechatBound: Boolean(d.wechatBound),
    profileComplete: Boolean(d.profileComplete),
  };
}

function buildProfileFromAuth(data: AnyMap | null): TeacherProfile | ParentProfile | null {
  const d = data || {};
  const role = d.role;
  const pid = toIdStr(d.profileId);
  const uid = toIdStr(d.userId ?? d.id);
  if (!pid) return null;
  const name = d.nickname || d.username || (role === 'PARENT' ? '家长' : '老师');
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
  const rawStatus = d.status || 'PENDING';
  return {
    id: toIdStr(d.id),
    lessonNo: d.lessonNo || '',
    courseId: d.courseId != null ? toIdStr(d.courseId) : '',
    classGroupId: d.classGroupId != null ? toIdStr(d.classGroupId) : '',
    teacherId: d.teacherId != null ? toIdStr(d.teacherId) : '',
    courseName: d.courseName || undefined,
    teacherName: d.teacherName || undefined,
    lessonDate: d.lessonDate || '',
    startTime: d.startTime || '',
    endTime: d.endTime || '',
    status: rawStatus === 'PENDING' ? 'SCHEDULED' : rawStatus,
    cancelReason: d.cancelReason || undefined,
    location: d.location || undefined,
    consumeHours: d.consumeHours ?? undefined,
    createTime: d.createdAt || d.createTime || undefined,
    studentAttendance: d.studentAttendance && typeof d.studentAttendance === 'object'
      ? d.studentAttendance
      : undefined,
  };
}

function toPhysicalRecord(d: AnyMap | null | undefined): PhysicalTestRecord {
  if (!d) return {} as any;
  const rawDetails: PhysicalScoreResponse[] = Array.isArray(d.details) ? d.details : (Array.isArray(d.scores) ? d.scores : []);
  const details: PhysicalScoreDetail[] = rawDetails.map((item) => ({
    id: item.id == null ? undefined : toIdStr(item.id),
    recordId: item.recordId == null ? undefined : toIdStr(item.recordId),
    itemId: toIdStr(item.itemId),
    itemName: item.itemName ?? item.name ?? undefined,
    rawValue: item.rawValue ?? item.value ?? undefined,
    convertedScore: item.convertedScore ?? item.score ?? undefined,
    level: item.level ?? item.convertedLevel ?? undefined,
    remark: item.remark ?? undefined,
    score: item.score ?? item.convertedScore ?? undefined,
    unit: item.unit ?? undefined,
  }));
  return {
    id: toIdStr(d.id),
    recordNo: d.recordNo || undefined,
    studentId: toIdStr(d.studentId),
    testerId: d.testerId != null ? toIdStr(d.testerId) : undefined,
    testDate: d.testDate || d.createTime || '',
    status: d.status || 'DRAFT',
    totalScore: d.totalScore ?? undefined,
    overallLevel: d.overallLevel ?? undefined,
    gradeLevel: d.gradeLevel ?? undefined,
    scoreLevel: d.scoreLevel ?? undefined,
    remark: d.remark ?? undefined,
    teacherName: d.teacherName ?? undefined,
    studentName: d.studentName ?? undefined,
    createTime: d.createdAt || d.createTime || undefined,
    scores: details,
    details,
    items: details,
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
    return { token, refreshToken: body?.refreshToken || undefined, user, profile };
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
    return { token, refreshToken: body?.refreshToken || undefined, user, profile };
  }, undefined);
}

export async function wechatLogin(
  code: string,
  role: Extract<UserRole, 'TEACHER' | 'PARENT'>,
  wechatProfile?: { avatarUrl?: string; nickName?: string }
): Promise<LoginResult> {
  return callWithFallback(async () => {
    const payload: AnyMap = { code, role };
    if (wechatProfile?.avatarUrl) payload.avatarUrl = wechatProfile.avatarUrl;
    if (wechatProfile?.nickName) payload.nickname = wechatProfile.nickName;
    const body: AnyMap = await post<AnyMap>('/auth/login/wechat', payload);
    const token: string = body?.accessToken || body?.token || '';
    const user = buildUserFromAuth(body);
    const profile = buildProfileFromAuth(body);
    return { token, refreshToken: body?.refreshToken || undefined, user, profile };
  }, undefined);
}

export async function bindWechat(
  code: string,
  wechatProfile?: { avatarUrl?: string; nickName?: string }
): Promise<LoginResult> {
  const body: AnyMap = await post<AnyMap>('/auth/wechat/bind', {
    code,
    avatarUrl: wechatProfile?.avatarUrl,
    nickname: wechatProfile?.nickName,
  });
  return {
    token: body?.accessToken || '',
    refreshToken: body?.refreshToken || undefined,
    user: buildUserFromAuth(body),
    profile: buildProfileFromAuth(body),
  };
}

export async function completeParentProfile(params: {
  name: string;
  phone: string;
  relation: 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER';
}): Promise<LoginResult> {
  const body: AnyMap = await post<AnyMap>('/auth/parent/profile/complete', params);
  return {
    token: body?.accessToken || '',
    refreshToken: body?.refreshToken || undefined,
    user: buildUserFromAuth(body),
    profile: buildProfileFromAuth(body),
  };
}

export async function updateWechatProfile(profile: {
  avatarUrl?: string;
  nickName?: string;
}): Promise<{ avatar?: string; nickname?: string }> {
  const body: AnyMap = await post<AnyMap>('/auth/wechat/profile', {
    avatarUrl: profile.avatarUrl,
    nickname: profile.nickName,
  });
  return {
    avatar: body?.avatar || undefined,
    nickname: body?.nickname || undefined,
  };
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

export const getLessonDetail = async (lessonId: string): Promise<Lesson> => {
  return toLesson(await get<AnyMap>(`/business/lessons/${lessonId}`));
};

export const getSchedules = async (params?: { date?: string; startDate?: string; endDate?: string }): Promise<Lesson[]> => {
  return callWithFallback(async () => {
    const q: AnyMap = {};
    if (params?.date) q.date = params.date;
    if (params?.startDate) q.startDate = params.startDate;
    if (params?.endDate) q.endDate = params.endDate;
    const list: AnyMap[] = await get<any[]>('/business/lessons', q) || [];
    return list.map(toLesson);
  }, undefined);
};

export const getPublicTimetable = async (params: { startDate: string; endDate: string }): Promise<Lesson[]> => {
  const list = await get<AnyMap[]>('/parent/timetable', params) || [];
  return list.map(toLesson);
};

export const getLessons = async (params: { studentId: string; startDate?: string; endDate?: string }): Promise<Lesson[]> => {
  return callWithFallback(async () => {
    const today = new Date();
    const startDate = params.startDate || today.toISOString().split('T')[0];
    const endDate = params.endDate || new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
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

export const addPhysicalTest = async (params: { studentId: string; scores: any[]; testerId?: string; items?: any[]; batchId: number | string; testDate?: string }): Promise<PhysicalTestRecord> => {
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
      batchId: Number(params.batchId) || params.batchId,
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

export const parentSignIn = async (params: { lessonId: string; studentId: string; status?: string }): Promise<{ success: boolean }> => {
  return callWithFallback(async () => {
    const lessonId = Number(params.lessonId) || params.lessonId;
    await post(`/parent/lessons/${lessonId}/attendance`, {
      lessonId,
      studentId: Number(params.studentId) || params.studentId,
      attendanceStatus: (params.status || 'PRESENT').toUpperCase(),
      remark: '',
    });
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
      studentNo: d.studentNo || '',
      status: d.attendanceStatus || d.status || 'UNKNOWN',
      signInAt: d.signInAt || undefined,
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

export const logoutSession = async (): Promise<void> => {
  await post('/auth/logout');
};

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

export const getAllTeacherStudents = (keyword?: string, isCurrent?: () => boolean): Promise<Student[]> =>
  collectPages(async (pageNum) => {
    const { students, total } = await getTeacherStudents({ pageNum, pageSize: 200, keyword });
    return { items: students, total };
  }, isCurrent);

export const getAllTeacherPhysicalTests = (isCurrent?: () => boolean): Promise<PhysicalTestRecord[]> =>
  collectPages(async (pageNum) => {
    const { records, total } = await getTeacherPhysicalTests({ pageNum, pageSize: 200 });
    return { items: records, total };
  }, isCurrent);

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

export const getPhysicalTestBatches = async (): Promise<any[]> => {
  return callWithFallback(async () => {
    const list: AnyMap[] = await get<any[]>('/business/physical-tests/batches') || [];
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
