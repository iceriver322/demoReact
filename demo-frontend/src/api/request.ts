import axios from 'axios';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// 请求拦截器：自动附加 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一处理
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data;
    if (code === 200) {
      return data;
    }
    const err = new Error(message || '请求失败');
    (err as any).__apiCode = code;
    (err as any).__apiData = data;
    return Promise.reject(err);
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default request;
