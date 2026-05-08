import { useState, useEffect } from 'react';
import { Table, Button, Space, message, Modal, Input } from 'antd';
import { CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { approvalApi, TaskDto } from '../api/approval';
import SubmissionDataDisplay from '../components/SubmissionDataDisplay';

const ApprovalPage: React.FC = () => {
  const [data, setData] = useState<TaskDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedTask, setSelectedTask] = useState<TaskDto | null>(null);

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
      setModalVisible(false);
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
        setModalVisible(false);
        load();
      },
    });
  };

  const showDetail = (record: TaskDto) => {
    setSelectedTask(record);
    setModalVisible(true);
  };

  const submissionId = selectedTask ? Number(selectedTask.variables?.submissionId) : null;

  const columns = [
    { title: '表单名称', dataIndex: 'templateName', width: 160 },
    { title: '任务名称', dataIndex: 'name', width: 120 },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>待审批列表</h3>
      <Table
        columns={columns}
        dataSource={data}
        rowKey="taskId"
        loading={loading}
        pagination={false}
        onRow={(record) => ({
          onClick: () => showDetail(record),
          style: { cursor: 'pointer' },
        })}
      />
      <Modal
        title="审批详情"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={640}
        footer={
          submissionId ? (
            <Space>
              <Button type="primary" icon={<CheckOutlined />}
                onClick={() => handleApprove(submissionId)}>通过</Button>
              <Button danger icon={<CloseOutlined />}
                onClick={() => handleReject(submissionId)}>拒绝</Button>
              <Button onClick={() => setModalVisible(false)}>关闭</Button>
            </Space>
          ) : null
        }
      >
        {selectedTask && selectedTask.schemaJson && selectedTask.submissionData ? (
          <SubmissionDataDisplay
            schemaJson={selectedTask.schemaJson}
            dataJson={selectedTask.submissionData}
          />
        ) : (
          <p>暂无数据</p>
        )}
      </Modal>
    </div>
  );
};

export default ApprovalPage;
