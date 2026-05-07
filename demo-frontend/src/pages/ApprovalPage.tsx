import { useState, useEffect } from 'react';
import { Table, Button, Space, message, Modal, Input } from 'antd';
import { CheckOutlined, CloseOutlined, EyeOutlined } from '@ant-design/icons';
import { approvalApi, TaskDto } from '../api/approval';

const ApprovalPage: React.FC = () => {
  const [data, setData] = useState<TaskDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [detailJson, setDetailJson] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const tasks = await approvalApi.pending();
      setData(tasks);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleApprove = async (submissionId: number) => {
    try {
      await approvalApi.approve(submissionId);
      message.success('已批准');
      load();
    } catch (err: any) { message.error(err.message || '操作失败'); }
  };

  const handleReject = async (submissionId: number) => {
    Modal.confirm({
      title: '驳回原因',
      content: <Input.TextArea id="reject-reason" rows={2} placeholder="可选填写驳回原因" />,
      onOk: async () => {
        const el = document.getElementById('reject-reason') as HTMLTextAreaElement;
        await approvalApi.reject(submissionId, el?.value);
        message.success('已驳回');
        load();
      },
    });
  };

  const showDetail = (json: string) => {
    try {
      const parsed = JSON.parse(json);
      setDetailJson(JSON.stringify(parsed, null, 2));
    } catch {
      setDetailJson(json);
    }
    setDetailVisible(true);
  };

  const columns = [
    { title: '表单名称', dataIndex: 'templateName', width: 160 },
    { title: '任务名称', dataIndex: 'name', width: 120 },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
    {
      title: '审批数据', width: 120,
      render: (_: any, record: TaskDto) =>
        record.submissionData ? (
          <Button type="link" size="small" icon={<EyeOutlined />}
            onClick={() => showDetail(record.submissionData!)}>查看数据</Button>
        ) : '-',
    },
    { title: '操作', width: 180, render: (_: any, record: TaskDto) => {
        const submissionId = Number(record.variables?.submissionId);
        return (
        <Space>
          <Button type="primary" size="small" icon={<CheckOutlined />}
            onClick={() => handleApprove(submissionId)}>批准</Button>
          <Button danger size="small" icon={<CloseOutlined />}
            onClick={() => handleReject(submissionId)}>驳回</Button>
        </Space>
      );
    }},
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>待审批列表</h3>
      <Table columns={columns} dataSource={data} rowKey="taskId" loading={loading} pagination={false} />
      <Modal
        title="审批数据详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={<Button onClick={() => setDetailVisible(false)}>关闭</Button>}
        width={640}
      >
        <pre style={{ whiteSpace: 'pre-wrap', background: '#f5f5f5', padding: 16, borderRadius: 4, maxHeight: 480, overflow: 'auto' }}>
          {detailJson}
        </pre>
      </Modal>
    </div>
  );
};

export default ApprovalPage;
