import { Layout, Menu, Button, Dropdown, Avatar } from 'antd';
import {
  FormOutlined, FileTextOutlined, CheckCircleOutlined,
  UserOutlined, DashboardOutlined, LogoutOutlined, KeyOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const { Header, Sider, Content } = Layout;

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, hasRole } = useAuth();

  const menuItems = [
    { key: '/', icon: <DashboardOutlined />, label: '首页' },
    { key: '/forms/templates', icon: <FormOutlined />, label: '我的表单' },
    { key: '/forms/submit', icon: <FileTextOutlined />, label: '填报数据' },
    ...(hasRole('ROLE_PRIVILEGED') || hasRole('ROLE_ADMIN')
      ? [{ key: '/approvals/pending', icon: <CheckCircleOutlined />, label: '待审批' }]
      : []),
    ...(hasRole('ROLE_ADMIN')
      ? [{ key: '/admin/users', icon: <UserOutlined />, label: '用户管理' }]
      : []),
  ];

  const userMenuItems = [
    { key: 'profile', label: `${user?.username}` },
    { key: 'change-password', icon: <KeyOutlined />, label: '修改密码' },
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true },
  ];

  const handleMenuClick = ({ key }: { key: string }) => navigate(key);

  const handleUserMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') {
      logout();
      navigate('/login');
    } else if (key === 'change-password') {
      navigate(`/change-password?username=${user?.username ?? ''}`);
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <div style={{
          height: 64, display: 'flex', alignItems: 'center',
          justifyContent: 'center', color: '#fff', fontSize: 18, fontWeight: 'bold'
        }}>
          表单平台
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header style={{
          background: '#fff', padding: '0 24px', display: 'flex',
          justifyContent: 'flex-end', alignItems: 'center'
        }}>
          <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }}>
            <Button type="text" icon={<Avatar size="small" icon={<UserOutlined />} />}>
              {user?.username}
            </Button>
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, padding: 24, background: '#fff', borderRadius: 8 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
