import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './components/MainLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import FormTemplateListPage from './pages/FormTemplateListPage';
import FormDesignerPage from './pages/FormDesignerPage';
import FormSubmitListPage from './pages/FormSubmitListPage';
import FormSubmitPage from './pages/FormSubmitPage';
import MySubmissionsPage from './pages/MySubmissionsPage';
import SubmissionListPage from './pages/SubmissionListPage';
import ApprovalPage from './pages/ApprovalPage';
import UserManagementPage from './pages/UserManagementPage';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* 公开路由 */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* 需要认证的路由 */}
            <Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/forms/templates" element={<FormTemplateListPage />} />
              <Route path="/forms/templates/new" element={<FormDesignerPage />} />
              <Route path="/forms/templates/:id" element={<FormDesignerPage />} />
              <Route path="/forms/templates/:id/submissions" element={<SubmissionListPage />} />
              <Route path="/forms/submit" element={<FormSubmitListPage />} />
              <Route path="/forms/submit/:id" element={<FormSubmitPage />} />
              <Route path="/forms/submissions/my" element={<MySubmissionsPage />} />
              <Route path="/approvals/pending" element={
                <ProtectedRoute requiredRole={undefined}><ApprovalPage /></ProtectedRoute>
              } />
              <Route path="/admin/users" element={
                <ProtectedRoute requiredRole="ROLE_ADMIN"><UserManagementPage /></ProtectedRoute>
              } />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
