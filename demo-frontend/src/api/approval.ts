import request from './request';

export interface TaskDto {
  taskId: string;
  processInstanceId: string;
  name: string;
  createTime: string;
  variables: Record<string, any>;
  submissionData?: string;
  templateName?: string;
  schemaJson?: string;
}

export const approvalApi = {
  pending: () => request.get<any, TaskDto[]>('/approvals/pending'),

  approve: (submissionId: number) =>
    request.put(`/approvals/${submissionId}/approve`),

  reject: (submissionId: number, reason?: string) =>
    request.put(`/approvals/${submissionId}/reject`, null, { params: { reason } }),
};
