export type TransactionType = 'RECHARGE' | 'CONSUMPTION' | 'REFUND' | 'ADJUSTMENT';

export interface StudentHourAccount {
  id: string;
  studentId: string;
  balance: number;
  totalRecharge: number;
  totalConsume: number;
  totalRefund: number;
  createTime?: string;
  updateTime?: string;
}

export interface HourTransaction {
  id: string;
  transactionNo: string;
  studentId: string;
  transactionType: TransactionType;
  amount: number;
  balanceBefore: number;
  balanceAfter: number;
  relatedLessonId?: string;
  operatorId?: string;
  remark?: string;
  idempotencyKey: string;
  createTime?: string;
}

export interface HourRechargeRequest {
  studentId: string;
  amount: number;
  remark?: string;
}

export interface HourRefundRequest {
  studentId: string;
  amount: number;
  remark?: string;
}
