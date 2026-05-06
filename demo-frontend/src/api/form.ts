import request from './request';

export interface FormTemplate {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  schemaJson: string;
  status: string;
  createdAt: string;
}

export interface FormSubmission {
  id: number;
  templateId: number;
  submitterId: number;
  dataJson: string;
  status: string;
  approverId?: number;
  approvedAt?: string;
  createdAt: string;
}

export interface PageResult<T> {
  total: number;
  page: number;
  size: number;
  records: T[];
}

export const formTemplateApi = {
  create: (data: { name: string; description?: string; schemaJson?: string }) =>
    request.post<any, FormTemplate>('/forms/templates', data),

  listMine: (params: { page: number; size: number }) =>
    request.get<any, PageResult<FormTemplate>>('/forms/templates', { params }),

  listPublished: (params: { page: number; size: number }) =>
    request.get<any, PageResult<FormTemplate>>('/forms/templates/published', { params }),

  detail: (id: number) =>
    request.get<any, FormTemplate>(`/forms/templates/${id}`),

  update: (id: number, data: any) =>
    request.put<any, FormTemplate>(`/forms/templates/${id}`, data),

  delete: (id: number) => request.delete(`/forms/templates/${id}`),

  publish: (id: number) => request.put(`/forms/templates/${id}/publish`),

  disable: (id: number) => request.put(`/forms/templates/${id}/disable`),
};

export const formSubmissionApi = {
  submit: (templateId: number, dataJson: string) =>
    request.post<any, FormSubmission>('/forms/submissions', { templateId, dataJson }),

  listByTemplate: (templateId: number, params: { page: number; size: number }) =>
    request.get<any, PageResult<FormSubmission>>(`/forms/templates/${templateId}/submissions`, { params }),

  listMy: (params: { page: number; size: number }) =>
    request.get<any, PageResult<FormSubmission>>('/forms/submissions/my', { params }),

  exportCsv: (templateId: number) =>
    request.get(`/forms/templates/${templateId}/submissions/export`, {
      responseType: 'blob',
      // Don't transform blob responses
      transformResponse: [(data) => data],
    }),
};
