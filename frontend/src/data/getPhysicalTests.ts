import { PhysicalTestRecord } from '@/types';

export default function mockGetPhysicalTests(params: { studentId: string }): { records: PhysicalTestRecord[] } {
  console.log('[Mock] getPhysicalTests:', params);
  
  const records: PhysicalTestRecord[] = [
    {
      id: '1',
      recordNo: 'PTR20240701001',
      studentId: params.studentId,
      testerId: '1',
      testDate: '2024-07-01',
      totalScore: 85.5,
      overallLevel: 'GOOD',
      status: 'CONFIRMED',
      remark: '暑期体能测试'
    },
    {
      id: '2',
      recordNo: 'PTR20240601001',
      studentId: params.studentId,
      testerId: '1',
      testDate: '2024-06-01',
      totalScore: 78.2,
      overallLevel: 'PASS',
      status: 'CONFIRMED'
    },
    {
      id: '3',
      recordNo: 'PTR20240501001',
      studentId: params.studentId,
      testerId: '1',
      testDate: '2024-05-01',
      totalScore: 92.0,
      overallLevel: 'EXCELLENT',
      status: 'CONFIRMED'
    }
  ];
  
  return { records };
}
