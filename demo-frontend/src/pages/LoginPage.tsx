import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const onFinish = async (values: { username: string; password: string }) => {
    try {
      await login(values);
      message.success('登录成功');
      navigate('/');
    } catch (err: any) {
      const code = (err as any).__apiCode;
      if (code === 1004) { // PASSWORD_EXPIRED
        navigate(`/change-password?username=${encodeURIComponent(values.username)}`);
        return;
      }
      if (code === 1005) { // ACCOUNT_LOCKED
        message.error('账户已被锁定，请30分钟后重试或联系管理员解锁');
      } else {
        message.error('用户名或密码错误');
      }
      // Clear password field
      const passwordField = document.querySelector('input[type="password"]') as HTMLInputElement;
      if (passwordField) passwordField.value = '';
    }
  };

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', background: '#f0f2f5'
    }}>
      <Card title="表单数据平台 - 登录" style={{ width: 400 }}>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>登录</Button>
          </Form.Item>
          <div style={{ textAlign: 'center' }}>
            没有账号？ <Link to="/register">立即注册</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;
