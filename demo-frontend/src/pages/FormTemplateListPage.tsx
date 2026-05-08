import { useState, useEffect } from 'react';
import { Table, Button, Space, Tag, Popconfirm, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, DownloadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { formTemplateApi, formSubmissionApi, FormTemplate } from '../api/form';

const timestamp = () => {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}_${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}`;
};

const FormTemplateListPage: React.FC = () => {
  const [data, setData] = useState<FormTemplate[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const load = async () => {
    setLoading(true);
    try {
      const result = await formTemplateApi.listMine({ page: 1, size: 100 });
      setData(result.records);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async (id: number) => {
    await formTemplateApi.delete(id);
    message.success('已删除');
    load();
  };

  const handleExport = async (rec: FormTemplate) => {
    try {
      const response = await formSubmissionApi.exportCsv(rec.id);
      const url = URL.createObjectURL(response.data);
      const a = document.createElement('a');
      a.href = url; a.download = `${rec.name}_${timestamp()}.csv`; a.click();
      URL.revokeObjectURL(url);
    } catch { message.error('导出失败'); }
  };

  const statusColor: Record<string, string> = {
    DRAFT: 'default', PUBLISHED: 'green', DISABLED: 'red',
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '名称', dataIndex: 'name' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{s}</Tag> },
    { title: '创建时间', dataIndex: 'createdAt', width: 180 },
    { title: '操作', width: 280, render: (_: any, record: FormTemplate) => (
        <Space>
          <Button size="small" icon={<EditOutlined />}
            onClick={() => navigate(`/forms/templates/${record.id}`)}>编辑</Button>
          <Button size="small" icon={<EyeOutlined />}
            onClick={() => navigate(`/forms/templates/${record.id}/submissions`)}>数据</Button>
          <Button size="small" icon={<DownloadOutlined />}
            onClick={() => handleExport(record)}>导出</Button>
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
        <h3>我的表单</h3>
        <Button type="primary" icon={<PlusOutlined />}
          onClick={() => navigate('/forms/templates/new')}>新建表单</Button>
      </Space>
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} pagination={false} />
    </div>
  );
};

export default FormTemplateListPage;
