import { PhysicalTestRecord } from '@/types';

export default function mockAddPhysicalTest(params: { studentId: string; scores: any[] }): { record: PhysicalTestRecord } {
  console.log('[Mock] addPhysicalTest:', params);
  
  const totalScore = params.scores.reduce((sum, s) => sum + s.convertedScore, 0);
  let level: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL' = 'PASS';
  if (totalScore >= 90) level = 'EXCELLENT';
  else if (totalScore >= 80) level = 'GOOD';
  else if (totalScore >= 60) level = 'PASS';
  else level = 'FAIL';
  
  const record: PhysicalTestRecord = {
    id: 'new-' + Date.now(),
    recordNo: 'PTR' + Date.now(),
    studentId: params.studentId,
    testerId: '1',
    testDate: new Date().toISOString().split('T')[0],
    totalScore: Math.round(totalScore * 10) / 10,
    overallLevel: level,
    status: 'CONFIRMED',
    scores: params.scores.map((s, i) => ({
      id: 'detail-' + i,
      recordId: 'new-' + Date.now(),
      itemId: s.itemId,
      rawValue: s.rawValue,
      convertedScore: s.convertedScore,
      level: s.level,
      remark: s.remark
    }))
  };
  
  return { record };
}
