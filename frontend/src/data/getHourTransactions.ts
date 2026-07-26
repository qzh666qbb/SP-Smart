import { HourTransaction } from '@/types';

export default function mockGetHourTransactions(params: { studentId: string }): { transactions: HourTransaction[] } {
  console.log('[Mock] getHourTransactions:', params);
  
  const today = new Date();
  const formatDate = (d: Date) => {
    return d.toISOString().split('T')[0] + ' ' + d.toTimeString().split(' ')[0];
  };
  
  const transactions: HourTransaction[] = [
    {
      id: '1',
      transactionNo: 'HT20240725001',
      studentId: params.studentId,
      transactionType: 'CONSUMPTION',
      amount: -1.5,
      balanceBefore: 47,
      balanceAfter: 45.5,
      relatedLessonId: '1',
      operatorId: '1',
      remark: '课程消费',
      idempotencyKey: 'key-001',
      createTime: formatDate(today)
    },
    {
      id: '2',
      transactionNo: 'HT20240720001',
      studentId: params.studentId,
      transactionType: 'RECHARGE',
      amount: 50,
      balanceBefore: 2,
      balanceAfter: 52,
      operatorId: '1',
      remark: '充值50课时',
      idempotencyKey: 'key-002',
      createTime: formatDate(new Date(today.getTime() - 5 * 86400000))
    },
    {
      id: '3',
      transactionNo: 'HT20240715001',
      studentId: params.studentId,
      transactionType: 'CONSUMPTION',
      amount: -3,
      balanceBefore: 55,
      balanceAfter: 52,
      relatedLessonId: '2',
      operatorId: '1',
      remark: '课程消费',
      idempotencyKey: 'key-003',
      createTime: formatDate(new Date(today.getTime() - 10 * 86400000))
    },
    {
      id: '4',
      transactionNo: 'HT20240710001',
      studentId: params.studentId,
      transactionType: 'REFUND',
      amount: 4.5,
      balanceBefore: 50,
      balanceAfter: 54.5,
      operatorId: '1',
      remark: '退款',
      idempotencyKey: 'key-004',
      createTime: formatDate(new Date(today.getTime() - 15 * 86400000))
    },
    {
      id: '5',
      transactionNo: 'HT20240701001',
      studentId: params.studentId,
      transactionType: 'RECHARGE',
      amount: 50,
      balanceBefore: 0,
      balanceAfter: 50,
      operatorId: '1',
      remark: '首次充值',
      idempotencyKey: 'key-005',
      createTime: formatDate(new Date(today.getTime() - 24 * 86400000))
    }
  ];
  
  return { transactions };
}
