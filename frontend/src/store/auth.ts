import { create } from 'zustand';
import { User, TeacherProfile, ParentProfile } from '@/types';

interface AuthState {
  token: string | null;
  user: User | null;
  profile: TeacherProfile | ParentProfile | null;
  isLoggedIn: boolean;
  
  login: (token: string, user: User, profile: TeacherProfile | ParentProfile | null) => void;
  logout: () => void;
  setProfile: (profile: TeacherProfile | ParentProfile | null) => void;
}

const useAuthStore = create<AuthState>((set) => ({
  token: null,
  user: null,
  profile: null,
  isLoggedIn: false,
  
  login: (token, user, profile) => set({
    token,
    user,
    profile,
    isLoggedIn: true
  }),
  
  logout: () => set({
    token: null,
    user: null,
    profile: null,
    isLoggedIn: false
  }),
  
  setProfile: (profile) => set({ profile })
}));

export default useAuthStore;
