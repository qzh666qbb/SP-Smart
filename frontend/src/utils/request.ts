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
const AUTH_REFRESH_TOKEN_KEY = 'AUTH_REFRESH_TOKEN';
const AUTH_USER_KEY = 'AUTH_USER';
const AUTH_PROFILE_KEY = 'AUTH_PROFILE';

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: Record<string, any>;
  params?: Record<string, any>;
  header?: Record<string, string>;
  skipAuthRefresh?: boolean;
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

export function getStoredRefreshToken(): string | null {
  try { return Taro.getStorageSync(AUTH_REFRESH_TOKEN_KEY) || null; } catch (_) { return null; }
}

export function setStoredRefreshToken(token: string | null): void {
  try {
    if (token == null) Taro.removeStorageSync(AUTH_REFRESH_TOKEN_KEY);
    else Taro.setStorageSync(AUTH_REFRESH_TOKEN_KEY, token);
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
    Taro.removeStorageSync(AUTH_REFRESH_TOKEN_KEY);
    Taro.removeStorageSync(AUTH_USER_KEY);
    Taro.removeStorageSync(AUTH_PROFILE_KEY);
  } catch (_) { /* noop */ }
}

let refreshInFlight: Promise<string> | null = null;
let redirectingToLogin = false;

function isAuthError(code: number): boolean {
  return [401, 20001, 20002, 20003].includes(code);
}

class RefreshTokenError extends Error {
  constructor(message: string, readonly unauthorized: boolean) {
    super(message);
  }
}

function getSessionVersion(): number | undefined {
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    return require('@/store/auth').default?.getState?.()?.sessionVersion;
  } catch (_) { return undefined; }
}

function redirectToLogin(): void {
  if (redirectingToLogin) return;
  const pages = Taro.getCurrentPages();
  const currentRoute = pages[pages.length - 1]?.route;
  if (currentRoute === 'pages/login/index') return;
  redirectingToLogin = true;
  Taro.reLaunch({ url: '/pages/login/index' }).finally(() => {
    redirectingToLogin = false;
  });
}

function invalidateAuth(message: string): void {
  clearAuthStorage();
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    require('@/store/auth').default?.getState?.()?.logout?.();
  } catch (_) { /* noop */ }
  Taro.showToast({ title: message, icon: 'none' });
  redirectToLogin();
}

function refreshAccessToken(): Promise<string> {
  if (refreshInFlight) return refreshInFlight;
  const refreshToken = getStoredRefreshToken();
  if (!refreshToken) return Promise.reject(new Error('登录已过期'));
  const sessionVersion = getSessionVersion();

  refreshInFlight = new Promise<string>((resolve, reject) => {
    Taro.request({
      url: `${BASE_URL}/auth/refresh`,
      method: 'POST',
      data: { refreshToken },
      header: { 'Content-Type': 'application/json', 'X-Client-Version': '20261102' },
      success: (res) => {
        if (getSessionVersion() !== sessionVersion || getStoredRefreshToken() !== refreshToken) {
          reject(new RefreshTokenError('登录状态已变化，请重试', false));
          return;
        }
        const payload: any = res?.data;
        const body = payload?.data;
        const nextToken = body?.accessToken || body?.token;
        if ((payload?.code === 0 || payload?.code === 200) && nextToken) {
          setStoredToken(nextToken);
          if (body?.refreshToken) setStoredRefreshToken(body.refreshToken);
          try {
            // eslint-disable-next-line @typescript-eslint/no-var-requires
            const useAuthStore = require('@/store/auth').default;
            useAuthStore?.setState?.({
              token: nextToken,
              refreshToken: body?.refreshToken || refreshToken,
              isLoggedIn: true,
            });
          } catch (_) { /* noop */ }
          resolve(nextToken);
        } else {
          reject(new RefreshTokenError(
            payload?.message || '登录续期失败，请重试',
            isAuthError(payload?.code) || res.statusCode === 401,
          ));
        }
      },
      fail: reject,
    });
  }).finally(() => {
    refreshInFlight = null;
  });
  return refreshInFlight;
}

export const request = async <T = any>(
  url: string,
  options: RequestOptions = {}
): Promise<T> => {
  const { method = 'GET', data = {}, header = {}, skipAuthRefresh = false } = options;
  const sessionVersion = getSessionVersion();
  let token: string | null = null;
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const useAuthStore = require('@/store/auth').default;
    token = useAuthStore?.getState?.()?.token || getStoredToken();
  } catch (_) {
    token = getStoredToken();
  }

  const defaultHeader: Record<string, string> = {
    'Content-Type': 'application/json', 'X-Client-Version': '20261102',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };

  return new Promise((resolve, reject) => {
    Taro.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: { ...defaultHeader, ...header },
      success: async (res) => {
        const payload =
          res && res.data != null && typeof res.data === 'object'
            ? res.data
            : { code: res?.statusCode || -1, message: '服务器响应格式错误', data: null };
        const code: number = payload?.code ?? -1;
        const message: string = payload?.message || `服务器响应异常（HTTP ${res?.statusCode || '未知'}）`;
        const body = (payload as any)?.data ?? null;
        if (code === 0 || code === 200) {
          resolve(body as T);
        } else if (isAuthError(code)) {
          if (getSessionVersion() !== sessionVersion) {
            reject(new Error('登录状态已变化，请重试'));
            return;
          }
          if (!skipAuthRefresh && url !== '/auth/refresh' && getStoredRefreshToken()) {
            try {
              await refreshAccessToken();
            } catch (error) {
              if (!(error instanceof RefreshTokenError) || !error.unauthorized) {
                Taro.showToast({ title: error?.message || error?.errMsg || '登录续期失败，请重试', icon: 'none' });
                reject(error);
                return;
              }
              // 只有续期接口明确拒绝认证时才清理登录状态。
              invalidateAuth(error.message);
              reject(error);
              return;
            }
            // 重试的业务或网络错误直接交给调用方处理。
            request<T>(url, { ...options, skipAuthRefresh: true }).then(resolve, reject);
            return;
          }
          invalidateAuth(message || '请重新登录');
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
