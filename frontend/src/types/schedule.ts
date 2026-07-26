export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';

export interface ScheduleSeries {
  id: string;
  classGroupId: string;
  teacherId: string;
  courseId: string;
  scheduleName: string;
  scheduleType: 'SINGLE' | 'RECURRING';
  weekDays?: string[];
  startTime: string;
  endTime: string;
  startDate: string;
  endDate: string;
  description?: string;
  createTime?: string;
}

export interface Lesson {
  id: string;
  lessonNo: string;
  scheduleSeriesId?: string;
  courseId: string;
  classGroupId: string;
  teacherId: string;
  courseName?: string;
  teacherName?: string;
  lessonDate: string;
  startTime: string;
  endTime: string;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  cancelReason?: string;
  createTime?: string;
  studentAttendance?: Record<string, AttendanceStatus>;
}

export interface LessonStudent {
  id: string;
  lessonId: string;
  studentId: string;
  attendanceStatus?: AttendanceStatus;
  signInAt?: string;
  consumeHours?: number;
  shouldConsume: boolean;
  hourTransactionId?: string;
  attendanceRemark?: string;
  confirmedAt?: string;
}

export interface AttendanceRequest {
  lessonId: string;
  studentAttendances: {
    studentId: string;
    attendanceStatus: AttendanceStatus;
    remark?: string;
  }[];
}
