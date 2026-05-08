import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Table, Tag, Button, message, Modal } from 'antd';
import { DownloadOutlined, EyeOutlined } from '@ant-design/icons';
import { formSubmissionApi, formTemplateApi, FormSubmission, FormTemplate } from '../api/form';
import SubmissionDataDisplay from '../components/SubmissionDataDisplay';

const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red', SUBMITTED: 'blue',
};
const statusText: Record<string, string> = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', SUBMITTED: '已提交',
};

const SubmissionListPage: React.FC = () => {
  const { id } = useParams();
  const [data, setData] = useState<FormSubmission[]>([]);
  const [loading, setLoading] = useState(false);
  const [template, setTemplate] = useState<FormTemplate | null>(null);
  const [detailVisible, setDetailVisible] = useState(false);
  const [detailSubmission, setDetailSubmission] = useState<FormSubmission | null>(null);

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

  const openDetail = (record: FormSubmission) => {
    setDetailSubmission(record);
    setDetailVisible(true);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '提交者ID', dataIndex: 'submitterId', width: 100 },
    { title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{statusText[s] || s}</Tag> },
    { title: '审批人ID', dataIndex: 'approverId', width: 100 },
    { title: '审批时间', dataIndex: 'approvedAt', width: 180 },
    { title: '提交时间', dataIndex: 'createdAt', width: 180 },
    { title: '操作', width: 80,
      render: (_: any, record: FormSubmission) => (
        <Button type="link" size="small" icon={<EyeOutlined />}
          onClick={() => openDetail(record)}>查看</Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h3>填报数据</h3>
        <Button icon={<DownloadOutlined />} onClick={handleExport}>导出 CSV</Button>
      </div>
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading} pagination={false} />

      <Modal
        title="填报数据详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={<Button onClick={() => setDetailVisible(false)}>关闭</Button>}
        width={640}
      >
        {detailSubmission && template && (
          <SubmissionDataDisplay
            schemaJson={template.schemaJson}
            dataJson={detailSubmission.dataJson}
            extra={[
              { label: '提交人', value: String(detailSubmission.submitterId) },
              { label: '提交时间', value: detailSubmission.createdAt },
              { label: '状态', value: <Tag color={statusColor[detailSubmission.status]}>{statusText[detailSubmission.status] || detailSubmission.status}</Tag> },
            ]}
          />
        )}
      </Modal>
    </div>
  );
};

export default SubmissionListPage;
