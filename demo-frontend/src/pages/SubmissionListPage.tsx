import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Table, Tag, Button, message } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { formSubmissionApi, FormSubmission } from '../api/form';

const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red',
};

const SubmissionListPage: React.FC = () => {
  const { id } = useParams();
  const [data, setData] = useState<FormSubmission[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (id) {
      setLoading(true);
      formSubmissionApi.listByTemplate(Number(id), { page: 1, size: 100 })
        .then(r => setData(r.records))
        .finally(() => setLoading(false));
    }
  }, [id]);

  const handleExport = async () => {
    if (!id) return;
    try {
      const response = await formSubmissionApi.exportCsv(Number(id));
      // responseType:'blob' 时 data 才是 Blob
      const url = URL.createObjectURL(response.data);
      const a = document.createElement('a');
      a.href = url; a.download = `submissions_${id}.csv`; a.click();
      URL.revokeObjectURL(url);
    } catch (err: any) {
      console.error('导出失败', err);
      message.error(err?.response?.data?.message || err?.message || '导出失败');
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '提交者ID', dataIndex: 'submitterId', width: 100 },
    { title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{s}</Tag> },
    { title: '审批人ID', dataIndex: 'approverId', width: 100 },
    { title: '审批时间', dataIndex: 'approvedAt', width: 180 },
    { title: '提交时间', dataIndex: 'createdAt', width: 180 },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h3>填报数据</h3>
        <Button icon={<DownloadOutlined />} onClick={handleExport}>导出 CSV</Button>
      </div>
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} pagination={false} />
    </div>
  );
};

export default SubmissionListPage;
