import { useState, useEffect } from 'react';
import { Table, Button } from 'antd';
import { FileTextOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { formTemplateApi, FormTemplate } from '../api/form';

const FormSubmitListPage: React.FC = () => {
  const [data, setData] = useState<FormTemplate[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    formTemplateApi.listPublished({ page: 1, size: 100 })
      .then(r => setData(r.records))
      .finally(() => setLoading(false));
  }, []);

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '创建时间', dataIndex: 'createdAt', width: 180 },
    { title: '操作', width: 120, render: (_: any, record: FormTemplate) => (
        <Button type="primary" size="small" icon={<FileTextOutlined />}
          onClick={() => navigate(`/forms/submit/${record.id}`)}>填报</Button>
      ),
    },
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>可填报表单</h3>
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} pagination={false} />
    </div>
  );
};

export default FormSubmitListPage;
