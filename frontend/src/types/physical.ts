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
  rawValue?: number;
  convertedScore?: number;
  level?: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL';
  remark?: string;
  score?: number;
  unit?: string;
}

export interface PhysicalTestRecord {
  id: string;
  recordNo?: string;
  studentId: string;
  testerId?: string;
  testDate: string;
  totalScore?: number;
  overallLevel?: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL';
  scoreLevel?: 'EXCELLENT' | 'GOOD' | 'PASS' | 'FAIL';
  gradeLevel?: string;
  status?: 'DRAFT' | 'SUBMITTED' | 'CONFIRMED';
  remark?: string;
  evaluation?: string;
  createTime?: string;
  scores?: PhysicalScoreDetail[];
  details?: PhysicalScoreDetail[];
  items?: PhysicalScoreDetail[];
  teacherName?: string;
  studentName?: string;
  recordNoGenerated?: string;
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
