-- ===============================================
-- 表单数据平台 - 初始数据 (H2)
-- ===============================================

-- 初始化角色
MERGE INTO sys_role (id, name, code) KEY(id) VALUES (1, '管理员', 'ROLE_ADMIN');
MERGE INTO sys_role (id, name, code) KEY(id) VALUES (2, '特权用户', 'ROLE_PRIVILEGED');
MERGE INTO sys_role (id, name, code) KEY(id) VALUES (3, '普通用户', 'ROLE_USER');

-- 初始化管理员用户 (密码: admin, bcrypt 加密)
-- 首次登录时密码为过期状态，强制修改
MERGE INTO sys_user (id, username, password, email, status, password_expire_date) KEY(id)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        'admin@demoform.com', 1, DATEADD('DAY', -1, CURRENT_DATE));

-- 分配管理员角色
MERGE INTO sys_user_role (user_id, role_id) KEY(user_id, role_id) VALUES (1, 1);
