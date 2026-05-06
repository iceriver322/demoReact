import request from './request';

export interface LoginParams {
  username: string;
  password: string;
}

export interface RegisterParams {
  username: string;
  password: string;
  email?: string;
}

export interface LoginResult {
  token: string;
  userId: number;
  username: string;
  email: string;
  roles: string[];
}

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  status: number;
  passwordExpireDate: string;
  roles: string[];
}

export const authApi = {
  login: (params: LoginParams) =>
    request.post<any, LoginResult>('/auth/login', params),

  register: (params: RegisterParams) =>
    request.post('/auth/register', params),

  getMe: () => request.get<any, UserInfo>('/auth/me'),
};
