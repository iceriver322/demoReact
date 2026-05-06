import { Card, Row, Col, Statistic } from 'antd';
import { FormOutlined, FileTextOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';

const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div>
      <h2>欢迎，{user?.username}</h2>
      <p style={{ color: '#666', marginBottom: 24 }}>
        角色：{user?.roles?.join(', ')}
      </p>
      <Row gutter={16}>
        <Col span={8}>
          <Card>
            <Statistic title="我的表单模板" value={0} prefix={<FormOutlined />} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="我的填报记录" value={0} prefix={<FileTextOutlined />} />
          </Card>
        </Col>
        {(user?.roles?.includes('ROLE_PRIVILEGED') || user?.roles?.includes('ROLE_ADMIN')) && (
          <Col span={8}>
            <Card>
              <Statistic title="待审批" value={0} prefix={<CheckCircleOutlined />} />
            </Card>
          </Col>
        )}
      </Row>
    </div>
  );
};

export default DashboardPage;
