export type TransactionType = 'RECHARGE' | 'CONSUMPTION' | 'REFUND' | 'ADJUSTMENT';

export interface StudentHourAccount {
  id: string;
  studentId: string;
  studentName?: string;
  studentNo?: string;
  parentName?: string;
  parentPhone?: string;
  /** 旧 mock 字段兼容 */
  balance?: number;
  totalRecharge?: number;
  totalConsume?: number;
  totalRefund?: number;
  /** 后端真实字段 */
  totalHours?: number;
  totalRecharged?: number;
  usedHours?: number;
  totalConsumed?: number;
  totalRefunded?: number;
  remainingHours?: number;
  paidHours?: number;
  giftHours?: number;
  createTime?: string;
  updateTime?: string;
}

export interface HourTransaction {
  id: string;
  transactionNo?: string;
  studentId: string;
  transactionType?: TransactionType | string;
  /** 旧字段兼容 */
  amount?: number;
  balanceBefore?: number;
  balanceAfter?: number;
  idempotencyKey?: string;
  /** 后端真实字段 */
  changeAmount?: number;
  beforeBalance?: number;
  afterBalance?: number;
  reason?: string;
  remark?: string;
  hourSource?: string;
  relatedLessonId?: string;
  operatorId?: string;
  operatorRole?: string;
  expireDate?: string;
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
