import { Lesson } from '@/types';

export default function mockGetLessons(params: { studentId: string }): { lessons: Lesson[] } {
  console.log('[Mock] getLessons:', params);
  
  const today = new Date();
  const formatDate = (d: Date) => {
    return d.toISOString().split('T')[0];
  };
  
  const lessons: Lesson[] = [
    {
      id: '1',
      lessonNo: 'LSN20240725001',
      courseId: '1',
      classGroupId: '1',
      teacherId: '1',
      courseName: '篮球训练课',
      teacherName: '张老师',
      lessonDate: formatDate(today),
      startTime: '09:00',
      endTime: '10:30',
      status: 'IN_PROGRESS',
      studentAttendance: { [params.studentId]: 'PRESENT' }
    },
    {
      id: '2',
      lessonNo: 'LSN20240720001',
      courseId: '1',
      classGroupId: '1',
      teacherId: '1',
      courseName: '篮球训练课',
      teacherName: '张老师',
      lessonDate: formatDate(new Date(today.getTime() - 5 * 86400000)),
      startTime: '09:00',
      endTime: '10:30',
      status: 'COMPLETED',
      studentAttendance: { [params.studentId]: 'PRESENT' }
    },
    {
      id: '3',
      lessonNo: 'LSN20240715001',
      courseId: '2',
      classGroupId: '2',
      teacherId: '1',
      courseName: '体能训练课',
      teacherName: '张老师',
      lessonDate: formatDate(new Date(today.getTime() - 10 * 86400000)),
      startTime: '14:00',
      endTime: '15:30',
      status: 'COMPLETED',
      studentAttendance: { [params.studentId]: 'LATE' }
    },
    {
      id: '4',
      lessonNo: 'LSN20240726001',
      courseId: '1',
      classGroupId: '1',
      teacherId: '1',
      courseName: '篮球训练课',
      teacherName: '张老师',
      lessonDate: formatDate(new Date(today.getTime() + 86400000)),
      startTime: '09:00',
      endTime: '10:30',
      status: 'SCHEDULED'
    },
    {
      id: '5',
      lessonNo: 'LSN20240727001',
      courseId: '2',
      classGroupId: '2',
      teacherId: '1',
      courseName: '体能训练课',
      teacherName: '张老师',
      lessonDate: formatDate(new Date(today.getTime() + 2 * 86400000)),
      startTime: '14:00',
      endTime: '15:30',
      status: 'SCHEDULED'
    }
  ];
  
  return { lessons };
}
