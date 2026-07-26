import { StudentHourAccount } from '@/types';

export default function mockGetHourAccount(params: { studentId: string }): { account: StudentHourAccount } {
  console.log('[Mock] getHourAccount:', params);
  
  const account: StudentHourAccount = {
    id: '1',
    studentId: params.studentId,
    balance: 45.5,
    totalRecharge: 100,
    totalConsume: 50,
    totalRefund: 4.5
  };
  
  return { account };
}
