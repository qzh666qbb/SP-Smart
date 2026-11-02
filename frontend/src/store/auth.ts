import { create } from 'zustand';
import { User, TeacherProfile, ParentProfile } from '@/types';
import {
  getStoredToken, setStoredToken,
  getStoredUser, setStoredUser,
  getStoredProfile, setStoredProfile,
  getStoredRefreshToken, setStoredRefreshToken,
  clearAuthStorage,
} from '@/utils/request';

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: User | null;
  profile: TeacherProfile | ParentProfile | null;
  isLoggedIn: boolean;
  isValidated: boolean;
  sessionVersion: number;

  hydrate: () => void;
  login: (token: string, user: User, profile: TeacherProfile | ParentProfile | null, refreshToken?: string | null) => void;
  logout: () => void;
  setProfile: (profile: TeacherProfile | ParentProfile | null) => void;
  setValidated: (validated: boolean) => void;
}

const initialState: Pick<AuthState, 'token' | 'refreshToken' | 'user' | 'profile' | 'isLoggedIn' | 'isValidated'> = {
  token: null,
  refreshToken: null,
  user: null,
  profile: null,
  isLoggedIn: false,
  isValidated: false,
};

const useAuthStore = create<AuthState>((set, get) => ({
  ...initialState,
  sessionVersion: 0,

  hydrate: () => {
    const token = getStoredToken();
    const user = getStoredUser() as User | null;
    const profile = getStoredProfile() as TeacherProfile | ParentProfile | null;
    const refreshToken = getStoredRefreshToken();
    if (token && user) {
      set({ token, refreshToken, user, profile, isLoggedIn: true, isValidated: false, sessionVersion: get().sessionVersion + 1 });
    } else {
      set({ ...initialState, isValidated: true, sessionVersion: get().sessionVersion + 1 });
    }
  },

  login: (token, user, profile, refreshToken) => {
    setStoredToken(token);
    setStoredUser(user);
    setStoredProfile(profile);
    if (refreshToken !== undefined) setStoredRefreshToken(refreshToken);
    set({ token, refreshToken: refreshToken === undefined ? get().refreshToken : refreshToken, user, profile, isLoggedIn: true, isValidated: true, sessionVersion: get().sessionVersion + 1 });
  },

  logout: () => {
    clearAuthStorage();
    set({ token: null, refreshToken: null, user: null, profile: null, isLoggedIn: false, isValidated: true, sessionVersion: get().sessionVersion + 1 });
  },

  setProfile: (profile) => {
    setStoredProfile(profile);
    set({ profile });
  },

  setValidated: (validated) => set({ isValidated: validated }),
}));

// 小程序启动时 hydrate 一次
try {
  useAuthStore.getState().hydrate();
} catch (_) { /* noop */ }

export default useAuthStore;
