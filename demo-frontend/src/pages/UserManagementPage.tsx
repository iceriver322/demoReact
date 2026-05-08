import { useState, useEffect } from 'react';
import { Table, Button, Tag, Space, Modal, Select, message, Popconfirm, Form, Input } from 'antd';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { userApi, UserVO } from '../api/user';
import { authApi } from '../api/auth';

const UserManagementPage: React.FC = () => {
  const [data, setData] = useState<UserVO[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addLoading, setAddLoading] = useState(false);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const result = await userApi.list({ page, size: 10 });
      setData(result.records);
      setTotal(result.total);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [page]);

  const handleDelete = async (id: number) => {
    await userApi.delete(id);
    message.success('已删除');
    load();
  };

  /** 新增用户 */
  const handleAddUser = async (values: { username: string; password: string; email?: string; roles?: string[] }) => {
    setAddLoading(true);
    try {
      // 1. 注册用户（默认为 ROLE_USER）
      await authApi.register({ username: values.username, password: values.password, email: values.email });
      // 2. 如果管理员指定了角色，重新分配
      if (values.roles && values.roles.length > 0) {
        const roleMap: Record<string, number> = { ROLE_ADMIN: 1, ROLE_PRIVILEGED: 2, ROLE_USER: 3 };
        const roleIds = values.roles.map(v => roleMap[v]).filter(Boolean);
        // 先查出新用户 ID
        const users = await userApi.list({ page: 1, size: 1, username: values.username });
        if (users.records.length > 0) {
          await userApi.assignRoles(users.records[0].id, roleIds);
        }
      }
      message.success('用户创建成功');
      setAddModalOpen(false);
      form.resetFields();
      load();
    } catch (err: any) {
      message.error((err as any).__apiData?.message || err.message || '创建失败');
    } finally { setAddLoading(false); }
  };

  const handleAssignRoles = (user: UserVO) => {
    let selectedRoles: string[] = [...user.roles];
    Modal.confirm({
      title: `分配角色 - ${user.username}`,
      content: (
        <Select mode="multiple" style={{ width: '100%' }} defaultValue={user.roles}
          onChange={(values: string[]) => { selectedRoles = values; }}
          options={[
            { label: '管理员', value: 'ROLE_ADMIN' },
            { label: '特权用户', value: 'ROLE_PRIVILEGED' },
            { label: '普通用户', value: 'ROLE_USER' },
          ]}
        />
      ),
      onOk: () => {
        const roleMap: Record<string, number> = { ROLE_ADMIN: 1, ROLE_PRIVILEGED: 2, ROLE_USER: 3 };
        const roleIds = selectedRoles.map(v => roleMap[v]).filter(Boolean);
        return userApi.assignRoles(user.id, roleIds).then(() => {
          message.success('已更新');
          load();
        });
      },
    });
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '用户名', dataIndex: 'username' },
    { title: '邮箱', dataIndex: 'email' },
    { title: '角色', dataIndex: 'roles', render: (roles: string[]) => (
        <>{roles?.map(r => <Tag key={r} color="blue">{r}</Tag>)}</>
      ),
    },
    { title: '状态', dataIndex: 'status', width: 80,
      render: (s: number) => s === 1 ? <Tag color="green">正常</Tag> : <Tag color="red">禁用</Tag> },
    { title: '创建时间', dataIndex: 'createdAt', width: 180 },
    { title: '操作', width: 180, render: (_: any, record: UserVO) => (
        <Space>
          <Button size="small" onClick={() => handleAssignRoles(record)}>角色</Button>
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h3>用户管理</h3>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setAddModalOpen(true)}>
          新增用户
        </Button>
      </div>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading}
        pagination={{ total, current: page, onChange: setPage }} />

      {/* 新增用户弹窗 */}
      <Modal title="新增用户" open={addModalOpen}
        onCancel={() => { setAddModalOpen(false); form.resetFields(); }}
        footer={null} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={handleAddUser}>
          <Form.Item name="username" label="用户名"
            rules={[{ required: true, message: '请输入用户名' }, { min: 3, max: 50, message: '3-50位' }]}>
            <Input placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" label="密码"
            rules={[{ required: true, message: '请输入密码' }, { min: 8, message: '至少8位' },
              { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, message: '需含大小写字母和数字' }]}>
            <Input.Password placeholder="密码" />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input placeholder="邮箱（选填）" />
          </Form.Item>
          <Form.Item name="roles" label="角色（默认普通用户）">
            <Select mode="multiple" placeholder="选择角色" defaultValue={['ROLE_USER']}
              options={[
                { label: '管理员', value: 'ROLE_ADMIN' },
                { label: '特权用户', value: 'ROLE_PRIVILEGED' },
                { label: '普通用户', value: 'ROLE_USER' },
              ]} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={addLoading} block>创建</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserManagementPage;
