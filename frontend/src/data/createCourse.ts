import { Lesson } from '@/types';

export default function mockCreateCourse(params: { lessonDate: string; startTime: string; endTime: string; courseName: string; studentIds: string[] }): { lesson: Lesson } {
  console.log('[Mock] createCourse:', params);
  
  const lesson: Lesson = {
    id: 'L' + Date.now(),
    lessonNo: 'L' + String(Math.random()).slice(2, 6),
    courseId: 'C001',
    classGroupId: 'G001',
    teacherId: 'T001',
    lessonDate: params.lessonDate,
    startTime: params.startTime,
    endTime: params.endTime,
    status: 'SCHEDULED'
  };
  
  return { lesson };
}
