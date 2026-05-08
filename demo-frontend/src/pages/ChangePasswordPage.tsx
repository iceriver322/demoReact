import { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '../api/auth';

const ChangePasswordPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const username = searchParams.get('username') || '';
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: { oldPassword: string; newPassword: string; confirmPassword: string }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('两次输入的密码不一致');
      return;
    }
    setLoading(true);
    try {
      const result = await authApi.changePassword({
        username,
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      });
      localStorage.setItem('token', result.token);
      message.success('密码修改成功');
      navigate('/');
    } catch (err: any) {
      message.error((err as any).__apiData?.message || err.message || '修改密码失败');
    } finally {
      setLoading(false);
    }
  };

  if (!username) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
        <Card title="修改密码" style={{ width: 400 }}>
          <p>缺少用户名参数，请重新登录。</p>
          <Button type="primary" onClick={() => navigate('/login')}>返回登录</Button>
        </Card>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
      <Card title="修改密码" style={{ width: 400 }}>
        <p style={{ marginBottom: 16 }}>用户：{username}</p>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="oldPassword" rules={[{ required: true, message: '请输入当前密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="当前密码" />
          </Form.Item>
          <Form.Item name="newPassword" rules={[{ required: true, message: '请输入新密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="新密码（至少8位，大小写字母+数字）" />
          </Form.Item>
          <Form.Item name="confirmPassword" rules={[{ required: true, message: '请确认新密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="确认新密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>确认修改</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default ChangePasswordPage;
