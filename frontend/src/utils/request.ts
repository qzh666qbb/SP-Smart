import Taro from '@tarojs/taro';
import useAuthStore from '@/store/auth';

const BASE_URL = 'http://localhost:8080/api';

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: Record<string, any>;
  header?: Record<string, string>;
}

export const request = async <T = any>(
  url: string,
  options: RequestOptions = {}
): Promise<T> => {
  const { method = 'GET', data = {}, header = {} } = options;
  const { token } = useAuthStore.getState();
  
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
        const { code, message, data } = res.data;
        if (code === 0 || code === 200) {
          resolve(data);
        } else {
          Taro.showToast({
            title: message || '请求失败',
            icon: 'none'
          });
          reject(new Error(message));
        }
      },
      fail: (err) => {
        Taro.showToast({
          title: '网络错误',
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
