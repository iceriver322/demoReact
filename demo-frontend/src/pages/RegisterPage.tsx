import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const onFinish = async (values: { username: string; password: string; email?: string }) => {
    try {
      await register(values);
      message.success('注册成功，请登录');
      navigate('/login');
    } catch (err: any) {
      const apiData = (err as any).__apiData;
      message.error(apiData?.message || err.message || '注册失败');
    }
  };

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', background: '#f0f2f5'
    }}>
      <Card title="用户注册" style={{ width: 400 }}>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="username"
            rules={[
              { required: true, message: '请输入用户名' },
              { min: 3, max: 50, message: '用户名长度3-50位' }
            ]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码至少8位' },
              { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, message: '需含大小写字母和数字' }
            ]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item name="email">
            <Input prefix={<MailOutlined />} placeholder="邮箱（选填）" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>注册</Button>
          </Form.Item>
          <div style={{ textAlign: 'center' }}>
            已有账号？ <Link to="/login">去登录</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default RegisterPage;
