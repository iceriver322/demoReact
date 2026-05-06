# 表单数据平台 (Demo Form Platform)

前后台分离的 Web 应用，支持用户管理、可视化表单设计、数据填报与审批。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 |
| 安全 | Spring Security + JWT (jjwt) |
| ORM | MyBatis + MyBatis-Plus 3.5.6 |
| 数据库 | H2 (开发) / MySQL 8.x (生产) |
| 流程引擎 | Camunda 7 Community (embedded) |
| 前端框架 | React 18 + TypeScript |
| 构建工具 | Vite 5 |
| UI 组件库 | Ant Design 5 |
| 拖拽 | @dnd-kit/core |

## 项目结构

```
demo-form/
├── pom.xml                    # 父 POM
├── demo-common/               # 公共模块：BaseEntity、DTO、枚举、异常
├── demo-user/                 # 用户模块：实体、Mapper、Service
├── demo-form-engine/          # 表单模块：模板/填报 实体、Mapper、Service
├── demo-workflow/             # 流程引擎：Camunda 7 BPMN、审批 Delegate
├── demo-web/                  # 主启动模块：Controller、Security 配置
└── demo-frontend/             # React 前端：Vite + TypeScript + Ant Design
```

模块依赖链：`common ← user ← form-engine ← workflow ← web`

## 功能模块

### 用户管理
- 用户注册/登录/退出（JWT 无状态认证）
- 预设三种角色：管理员(ROLE_ADMIN)、特权用户(ROLE_PRIVILEGED)、普通用户(ROLE_USER)
- 初始化管理员：admin/admin（首次登录强制改密）
- 密码策略：至少8位含大小写字母+数字，90天过期，3次失败锁定30分钟

### 表单设计器
- 拖拽式可视化表单构建（基于 @dnd-kit）
- 支持字段类型：文本、多行文本、数字、日期、下拉选择、单选、多选、文件上传
- 表单模板 CRUD、发布/停用

### 数据填报
- 动态表单渲染（根据 schema JSON 生成）
- 数据提交后进入审批流程
- 仅通过审批的数据生效
- CSV 导出填报数据

### 审批流程
- 基于 Camunda 7 嵌入式流程引擎
- 提交 → 待审批 → 特权用户批准/驳回
- 审批结果回调更新数据状态

## API 概览

| 路径前缀 | 说明 |
|---------|------|
| `/api/auth` | 认证：注册、登录、获取当前用户 |
| `/api/users` | 用户管理（管理员） |
| `/api/forms/templates` | 表单模板 CRUD、发布/停用 |
| `/api/forms/submissions` | 数据填报、查看、导出 |
| `/api/approvals` | 审批操作（特权用户） |

统一响应格式：`{ code: 200, message: "操作成功", data: {} }`

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.9+
- Node.js 18+

### 启动后端

```bash
# 构建全部模块
mvn clean install -DskipTests

# 启动开发服务器（H2 内存数据库，端口 8080）
cd demo-web && mvn spring-boot:run
```

### 启动前端

```bash
cd demo-frontend
npm install
npm run dev     # 端口 5173，自动代理 /api → localhost:8080
```

### 访问
- 前端：http://localhost:5173
- H2 控制台：http://localhost:8080/h2-console
- 默认管理员：admin / admin

### 配置说明
- Maven 本地仓库路径：`/Users/fan/software/apache-maven-repo`
- 开发环境使用 H2 内存数据库，数据不持久化
- 生产环境配置 `application-prod.yml`，需提供 MySQL 连接信息

## 开发规范
- 后端代码遵循阿里巴巴 Java 开发规范
- 所有 Java 类包含中文注释
- 使用 Lombok 简化代码
- 单元测试覆盖率目标 > 70%
- Git 提交信息采用约定式格式
