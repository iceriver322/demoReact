import { useState, useEffect } from 'react';
import { Table, Button, Tag, Space, Modal, Select, message, Popconfirm } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { userApi, UserVO } from '../api/user';

const UserManagementPage: React.FC = () => {
  const [data, setData] = useState<UserVO[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);

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

  const handleAssignRoles = (user: UserVO) => {
    Modal.confirm({
      title: `分配角色 - ${user.username}`,
      content: (
        <Select mode="multiple" style={{ width: '100%' }} defaultValue={user.roles}
          onChange={(values: string[]) => {
            const roleMap: Record<string, number> = {
              ROLE_ADMIN: 1, ROLE_PRIVILEGED: 2, ROLE_USER: 3,
            };
            const roleIds = values.map(v => roleMap[v]).filter(Boolean);
            userApi.assignRoles(user.id, roleIds).then(() => {
              message.success('已更新');
              load();
            });
          }}
          options={[
            { label: '管理员', value: 'ROLE_ADMIN' },
            { label: '特权用户', value: 'ROLE_PRIVILEGED' },
            { label: '普通用户', value: 'ROLE_USER' },
          ]}
        />
      ),
      onOk: () => {},
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
      <h3 style={{ marginBottom: 16 }}>用户管理</h3>
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading}
        pagination={{ total, current: page, onChange: setPage }} />
    </div>
  );
};

export default UserManagementPage;
