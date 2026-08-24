import { create } from 'zustand';
import { User, TeacherProfile, ParentProfile } from '@/types';
import {
  getStoredToken, setStoredToken,
  getStoredUser, setStoredUser,
  getStoredProfile, setStoredProfile,
  clearAuthStorage,
} from '@/utils/request';

interface AuthState {
  token: string | null;
  user: User | null;
  profile: TeacherProfile | ParentProfile | null;
  isLoggedIn: boolean;

  hydrate: () => void;
  login: (token: string, user: User, profile: TeacherProfile | ParentProfile | null) => void;
  logout: () => void;
  setProfile: (profile: TeacherProfile | ParentProfile | null) => void;
}

const initialState: Pick<AuthState, 'token' | 'user' | 'profile' | 'isLoggedIn'> = {
  token: null,
  user: null,
  profile: null,
  isLoggedIn: false,
};

const useAuthStore = create<AuthState>((set, get) => ({
  ...initialState,

  hydrate: () => {
    const token = getStoredToken();
    const user = getStoredUser() as User | null;
    const profile = getStoredProfile() as TeacherProfile | ParentProfile | null;
    if (token) {
      set({ token, user, profile, isLoggedIn: true });
    }
  },

  login: (token, user, profile) => {
    setStoredToken(token);
    setStoredUser(user);
    setStoredProfile(profile);
    set({ token, user, profile, isLoggedIn: true });
  },

  logout: () => {
    clearAuthStorage();
    set({ token: null, user: null, profile: null, isLoggedIn: false });
  },

  setProfile: (profile) => {
    setStoredProfile(profile);
    set({ profile });
  }
}));

// 小程序启动时 hydrate 一次
try {
  useAuthStore.getState().hydrate();
} catch (_) { /* noop */ }

export default useAuthStore;

