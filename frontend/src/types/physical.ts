export interface PhysicalTestItem {
  id: string;
  itemCode: string;
  itemName: string;
  itemType: 'HEIGHT' | 'WEIGHT' | 'RUNNING' | 'JUMPING' | 'THROWING' | 'FLEXIBILITY';
  unit: string;
  genderRequired: boolean;
  minValue?: number;
  maxValue?: number;
  status: 'ENABLED' | 'DISABLED';
  sortOrder: number;
  createTime?: string;
}

export interface PhysicalScoreDetail {
  id?: string;
  recordId?: string;
  itemId: string;
  itemName?: string;
  rawValue: number;
  convertedScore: number;
  level: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL';
  remark?: string;
  score?: number;
  unit?: string;
}

export interface PhysicalTestRecord {
  id: string;
  recordNo: string;
  studentId: string;
  testerId: string;
  testDate: string;
  totalScore: number;
  overallLevel: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL';
  status: 'DRAFT' | 'CONFIRMED';
  remark?: string;
  createTime?: string;
  scores?: PhysicalScoreDetail[];
  teacherName?: string;
  evaluation?: string;
  studentName?: string;
}

export interface PhysicalTestCreateRequest {
  studentId: string;
  testDate: string;
  scores: {
    itemId: string;
    rawValue: number;
    convertedScore: number;
    level: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL';
    remark?: string;
  }[];
  remark?: string;
}
