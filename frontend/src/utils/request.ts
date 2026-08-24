import Taro from '@tarojs/taro';

declare const API_BASE_URL: string;

const getApiBaseUrl = (): string => {
  if (process.env.TARO_ENV === 'h5') {
    return '/api';
  }
  if (typeof API_BASE_URL !== 'undefined' && API_BASE_URL) {
    return API_BASE_URL;
  }
  return 'http://localhost:8080/api';
};

const BASE_URL = getApiBaseUrl();
const AUTH_STORAGE_KEY = 'AUTH_TOKEN';
const AUTH_USER_KEY = 'AUTH_USER';
const AUTH_PROFILE_KEY = 'AUTH_PROFILE';

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: Record<string, any>;
  params?: Record<string, any>;
  header?: Record<string, string>;
}

export function getStoredToken(): string | null {
  try { return Taro.getStorageSync(AUTH_STORAGE_KEY) || null; } catch (_) { return null; }
}

export function setStoredToken(token: string | null): void {
  try {
    if (token == null) Taro.removeStorageSync(AUTH_STORAGE_KEY);
    else Taro.setStorageSync(AUTH_STORAGE_KEY, token);
  } catch (_) { /* noop */ }
}

export function getStoredUser(): any | null {
  try { return Taro.getStorageSync(AUTH_USER_KEY) || null; } catch (_) { return null; }
}

export function setStoredUser(user: any | null): void {
  try {
    if (user == null) Taro.removeStorageSync(AUTH_USER_KEY);
    else Taro.setStorageSync(AUTH_USER_KEY, user);
  } catch (_) { /* noop */ }
}

export function getStoredProfile(): any | null {
  try { return Taro.getStorageSync(AUTH_PROFILE_KEY) || null; } catch (_) { return null; }
}

export function setStoredProfile(profile: any | null): void {
  try {
    if (profile == null) Taro.removeStorageSync(AUTH_PROFILE_KEY);
    else Taro.setStorageSync(AUTH_PROFILE_KEY, profile);
  } catch (_) { /* noop */ }
}

export function clearAuthStorage(): void {
  try {
    Taro.removeStorageSync(AUTH_STORAGE_KEY);
    Taro.removeStorageSync(AUTH_USER_KEY);
    Taro.removeStorageSync(AUTH_PROFILE_KEY);
  } catch (_) { /* noop */ }
}

export const request = async <T = any>(
  url: string,
  options: RequestOptions = {}
): Promise<T> => {
  const { method = 'GET', data = {}, header = {} } = options;
  let token: string | null = null;
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const useAuthStore = require('@/store/auth').default;
    token = useAuthStore?.getState?.()?.token || getStoredToken();
  } catch (_) {
    token = getStoredToken();
  }

  const defaultHeader: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };

  return new Promise((resolve, reject) => {
    Taro.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: { ...defaultHeader, ...header },
      success: (res) => {
        const payload =
          res && res.data != null && typeof res.data === 'object'
            ? res.data
            : { code: -1, message: '服务器返回为空', data: null };
        const code: number = payload?.code ?? -1;
        const message: string = payload?.message || '请求失败';
        const body = (payload as any)?.data ?? null;
        if (code === 0 || code === 200) {
          resolve(body as T);
        } else if (code === 401 || code === 20001 || code === 20002 || code === 20003) {
          clearAuthStorage();
          try {
            // eslint-disable-next-line @typescript-eslint/no-var-requires
            const useAuthStore = require('@/store/auth').default;
            useAuthStore?.getState?.()?.logout?.();
          } catch (_) { /* noop */ }
          Taro.showToast({ title: message || '请重新登录', icon: 'none' });
          reject(new Error(message || 'Unauthorized'));
        } else {
          Taro.showToast({ title: message, icon: 'none' });
          const err = new Error(message) as any;
          err.code = code;
          err.payload = payload;
          reject(err);
        }
      },
      fail: (err) => {
        const msg = (err && typeof err === 'object' && (err as any).errMsg) ? (err as any).errMsg : '网络错误';
        Taro.showToast({
          title: msg,
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

export const get = <T = any>(url: string, data?: Record<string, any>) => {
  return request<T>(url, { method: 'GET', data });
};

export const post = <T = any>(url: string, data?: Record<string, any>) => {
  return request<T>(url, { method: 'POST', data });
};

export const put = <T = any>(url: string, data?: Record<string, any>) => {
  return request<T>(url, { method: 'PUT', data });
};

export const del = <T = any>(url: string, data?: Record<string, any>) => {
  return request<T>(url, { method: 'DELETE', data });
};
