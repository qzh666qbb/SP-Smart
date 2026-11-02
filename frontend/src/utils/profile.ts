import { ParentProfile, TeacherProfile } from '@/types';

type Profile = TeacherProfile | ParentProfile | null;

export function mergeProfile(current: Profile, incoming: Profile): Profile {
  if (!incoming) return current;
  if (!current || current.id !== incoming.id) return incoming;
  const meaningfulEntries = Object.entries(incoming).filter(([key, value]) => {
    if (value === undefined || value === null || value === '') return false;
    if (key === 'students' && Array.isArray(value) && value.length === 0) return false;
    return true;
  });
  return { ...current, ...Object.fromEntries(meaningfulEntries) } as Profile;
}
