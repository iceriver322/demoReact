import request from './request';

export interface UserVO {
  id: number;
  username: string;
  email: string;
  status: number;
  passwordExpireDate: string;
  roles: string[];
  createdAt: string;
}

export interface PageResult<T> {
  total: number;
  page: number;
  size: number;
  records: T[];
}

export const userApi = {
  list: (params: { page: number; size: number; username?: string }) =>
    request.get<any, PageResult<UserVO>>('/users', { params }),

  detail: (id: number) =>
    request.get<any, UserVO>(`/users/${id}`),

  update: (id: number, data: any) =>
    request.put(`/users/${id}`, data),

  delete: (id: number) =>
    request.delete(`/users/${id}`),

  assignRoles: (id: number, roleIds: number[]) =>
    request.put(`/users/${id}/roles`, { roleIds }),

  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    request.put('/users/change-password', data),
};
