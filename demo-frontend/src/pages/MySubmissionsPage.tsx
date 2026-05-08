import { useState, useEffect } from 'react';
import { Table, Tag } from 'antd';
import { formSubmissionApi, FormSubmission } from '../api/form';

const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red', SUBMITTED: 'blue',
};
const statusText: Record<string, string> = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', SUBMITTED: '已提交',
};

const MySubmissionsPage: React.FC = () => {
  const [data, setData] = useState<FormSubmission[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    formSubmissionApi.listMy({ page: 1, size: 100 })
      .then(r => setData(r.records))
      .finally(() => setLoading(false));
  }, []);

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '表单模板ID', dataIndex: 'templateId', width: 100 },
    { title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{s}</Tag> },
    { title: '提交时间', dataIndex: 'createdAt', width: 180 },
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>我的填报记录</h3>
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} pagination={false} />
    </div>
  );
};

export default MySubmissionsPage;
