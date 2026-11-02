import { ParentProfile, TeacherProfile } from '@/types';

type Profile = TeacherProfile | ParentProfile | null;

// 身份接口返回完整快照；空值、空孩子列表和资料删除都必须覆盖本地缓存。
export function mergeProfile(_current: Profile, incoming: Profile): Profile {
  return incoming;
}
