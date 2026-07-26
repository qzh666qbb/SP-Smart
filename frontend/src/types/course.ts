export type CourseType = 'PHYSICAL' | 'DANCE' | 'MUSIC' | 'ART';

export interface Course {
  id: string;
  courseCode: string;
  courseName: string;
  courseType: CourseType;
  defaultDurationMinutes: number;
  defaultConsumeHours: number;
  description?: string;
  status: 'ENABLED' | 'DISABLED';
  createTime?: string;
}

export interface ClassGroup {
  id: string;
  classCode: string;
  className: string;
  courseId: string;
  primaryTeacherId: string;
  maxStudents: number;
  status: 'ENABLED' | 'DISABLED';
  startDate?: string;
  endDate?: string;
  createTime?: string;
}

export interface CourseResponse {
  id: string;
  courseCode: string;
  courseName: string;
  courseType: CourseType;
  defaultDurationMinutes: number;
  defaultConsumeHours: number;
  description?: string;
  status: 'ENABLED' | 'DISABLED';
  createdAt?: string;
}
