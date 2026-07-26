import { Lesson } from '@/types';

export default function mockGetSchedules(params?: { date?: string; studentId?: string }): { schedules: Lesson[] } {
  console.log('[Mock] getSchedules:', params);
  
  const today = new Date();
  const formatDate = (d: Date) => {
    return d.toISOString().split('T')[0];
  };
  
  const schedules: Lesson[] = [
    {
      id: '1',
      lessonNo: 'LSN20240725001',
      courseId: '1',
      classGroupId: '1',
      teacherId: '1',
      lessonDate: formatDate(today),
      startTime: '09:00',
      endTime: '10:30',
      status: 'IN_PROGRESS'
    },
    {
      id: '2',
      lessonNo: 'LSN20240725002',
      courseId: '2',
      classGroupId: '2',
      teacherId: '1',
      lessonDate: formatDate(today),
      startTime: '14:00',
      endTime: '15:30',
      status: 'SCHEDULED'
    },
    {
      id: '3',
      lessonNo: 'LSN20240726001',
      courseId: '1',
      classGroupId: '1',
      teacherId: '1',
      lessonDate: formatDate(new Date(today.getTime() + 86400000)),
      startTime: '09:00',
      endTime: '10:30',
      status: 'SCHEDULED'
    },
    {
      id: '4',
      lessonNo: 'LSN20240726002',
      courseId: '3',
      classGroupId: '3',
      teacherId: '1',
      lessonDate: formatDate(new Date(today.getTime() + 86400000)),
      startTime: '14:00',
      endTime: '16:00',
      status: 'SCHEDULED'
    },
    {
      id: '5',
      lessonNo: 'LSN20240727001',
      courseId: '1',
      classGroupId: '1',
      teacherId: '1',
      lessonDate: formatDate(new Date(today.getTime() + 172800000)),
      startTime: '09:00',
      endTime: '10:30',
      status: 'SCHEDULED'
    }
  ];
  
  return { schedules };
}
