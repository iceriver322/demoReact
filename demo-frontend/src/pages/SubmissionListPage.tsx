import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Table, Tag, Button, message } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';

const timestamp = () => {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}_${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}`;
};
import { formSubmissionApi, formTemplateApi, FormSubmission, FormTemplate } from '../api/form';

const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red', SUBMITTED: 'blue',
};
const statusText: Record<string, string> = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', SUBMITTED: '已提交',
};

interface SchemaField {
  name: string;
  label: string;
  type?: string;
}

const SubmissionListPage: React.FC = () => {
  const { id } = useParams();
  const [data, setData] = useState<FormSubmission[]>([]);
  const [loading, setLoading] = useState(false);
  const [template, setTemplate] = useState<FormTemplate | null>(null);

  useEffect(() => {
    if (id) {
      setLoading(true);
      Promise.all([
        formSubmissionApi.listByTemplate(Number(id), { page: 1, size: 100 }),
        formTemplateApi.detail(Number(id)),
      ])
      .then(([r, t]) => {
        setData(r.records);
        setTemplate(t);
      })
      .finally(() => setLoading(false));
    }
  }, [id]);

  const handleExport = async () => {
    if (!id) return;
    try {
      const response = await formSubmissionApi.exportCsv(Number(id));
      const url = URL.createObjectURL(response.data);
      const a = document.createElement('a');
      a.href = url; a.download = `${template?.name || `submissions_${id}`}_${timestamp()}.csv`; a.click();
      URL.revokeObjectURL(url);
    } catch (err: any) {
      console.error('导出失败', err);
      message.error(err?.response?.data?.message || err?.message || '导出失败');
    }
  };

  const schemaFields: SchemaField[] = (() => {
    if (!template) return [];
    try { return JSON.parse(template.schemaJson || '[]'); } catch { return []; }
  })();

  const dynamicColumns = schemaFields.map(field => ({
    title: field.label || field.name,
    key: field.name,
    width: 120,
    ellipsis: true,
    render: (_: unknown, record: FormSubmission) => {
      try {
        const data = JSON.parse(record.dataJson || '{}');
        const val = data[field.name];
        if (val === undefined || val === null) return '-';
        if (Array.isArray(val)) return val.join(', ');
        return String(val);
      } catch { return '-'; }
    },
  }));

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '提交者ID', dataIndex: 'submitterId', width: 100 },
    ...dynamicColumns,
    { title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{statusText[s] || s}</Tag> },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h3>填报数据</h3>
        <Button icon={<DownloadOutlined />} onClick={handleExport}>导出 CSV</Button>
      </div>
      <div style={{ overflowX: 'auto' }}>
        <Table columns={columns} dataSource={data} rowKey="id" loading={loading} pagination={false} />
      </div>
    </div>
  );
};

export default SubmissionListPage;
