# 表单数据平台 - 设计文档

> 日期：2026-05-06 | 状态：已确认

## 概述

前后台分离的 Web 应用。后台基于 Java/Maven/Spring Boot，前台基于 React。为具备表单设计、填报、审批功能的在线数据收集平台。

## 技术选型

### 后端

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 3.x |
| 安全 | Spring Security + jjwt (JWT) |
| ORM | MyBatis + MyBatis-Plus |
| 数据库 | H2 (开发) / MySQL 8.x (生产) |
| 流程引擎 | Camunda 7 Community (embedded) |
| 构建 | Maven 3.9+，本地仓库 `/Users/fan/software/apache-maven-repo` |
| 测试 | JUnit 5 + Mockito + H2 |

### 前端

| 用途 | 技术 |
|------|------|
| 框架 | React 18 + TypeScript |
| 构建 | Vite |
| 路由 | React Router v6 |
| UI 组件库 | Ant Design 5 |
| 拖拽 | @dnd-kit/core |
| HTTP | Axios |
| 状态管理 | React Context + useReducer |
| 测试 | Vitest + React Testing Library |

## 项目结构

```
demo-form/
├── pom.xml                          # 父 POM
├── demo-common/                     # 公共模块：实体基类、DTO、枚举、工具类、异常
├── demo-user/                       # 用户模块：实体/Repository/Service
├── demo-form-engine/                # 表单模块：表单定义、填报、数据查询
├── demo-workflow/                   # 流程引擎：Camunda 7 BPMN、审批 Delegate
├── demo-web/                        # 主启动模块：Controller、Security、全局异常处理
└── demo-frontend/                   # React 前端（Vite）
```

模块依赖链：`common ← user ← form-engine ← workflow ← web`

## 数据模型

### 用户与角色

```
sys_user                          sys_role
┌──────────────────────┐          ┌──────────────────┐
│ id          BIGINT PK│          │ id       BIGINT PK│
│ username    VARCHAR  │    N:M   │ name     VARCHAR  │
│ password    VARCHAR  │◄────────►│ code     VARCHAR  │
│ email       VARCHAR  │          └──────────────────┘
│ status      TINYINT  │
│ password_expire_date │          sys_user_role (中间表)
│ created_at  DATETIME │
│ updated_at  DATETIME │
└──────────────────────┘
```

- 角色预设：`ROLE_ADMIN`（管理员）、`ROLE_PRIVILEGED`（特权用户）、`ROLE_USER`（普通用户）
- 初始化 admin/admin，角色 ROLE_ADMIN，首次登录强制修改密码

### 密码策略

- 强度：至少 8 位，含大写字母 + 小写字母 + 数字
- 过期：90 天，到期后登录强制修改
- 锁定：3 次登录失败锁定 30 分钟（Redis 计数器，本地开发可降级为内存实现）

### 表单与数据

```
form_template                     form_submission
┌────────────────────────┐        ┌──────────────────────────┐
│ id            BIGINT PK│        │ id               BIGINT PK│
│ name          VARCHAR  │        │ template_id      BIGINT FK│
│ description   VARCHAR  │        │ submitter_id     BIGINT FK│
│ owner_id      BIGINT FK│◄───────│ data             JSON     │
│ schema        JSON     │        │ status (待审/通过/驳回)    │
│ status(草稿/已发布/已停用)│      │ approver_id      BIGINT FK│
│ created_at    DATETIME │        │ approved_at      DATETIME │
│ updated_at    DATETIME │        │ created_at       DATETIME │
└────────────────────────┘        └──────────────────────────┘
```

- `schema`：表单字段定义 JSON（字段名、类型、标签、必填、选项等）
- `data`：填报数据 JSON，key 对应 schema 字段名
- 数据生效流程：**提交 → 待审批 → 特权用户批准/驳回 → 生效/结束**

## API 设计

统一响应格式：`{ code: 200, message: "操作成功", data: {} }`

### 认证 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 登录，返回 JWT |
| POST | `/api/auth/logout` | 退出 |
| GET | `/api/auth/me` | 当前用户信息 |

### 用户管理 `/api/users`（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/users` | 用户列表（分页） |
| GET | `/api/users/{id}` | 用户详情 |
| PUT | `/api/users/{id}` | 编辑用户 |
| DELETE | `/api/users/{id}` | 删除用户 |
| PUT | `/api/users/{id}/roles` | 分配角色 |

### 表单模板 `/api/forms/templates`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/forms/templates` | 创建表单模板 |
| GET | `/api/forms/templates` | 我的表单列表 |
| GET | `/api/forms/templates/{id}` | 模板详情 |
| PUT | `/api/forms/templates/{id}` | 编辑模板 |
| DELETE | `/api/forms/templates/{id}` | 删除模板 |
| PUT | `/api/forms/templates/{id}/publish` | 发布 |
| PUT | `/api/forms/templates/{id}/disable` | 停用 |

### 表单填报 `/api/forms/submissions`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/forms/submissions` | 提交填报 |
| GET | `/api/forms/submissions/my` | 我的填报记录 |
| GET | `/api/forms/templates/{id}/submissions` | 查看填报数据 |
| GET | `/api/forms/templates/{templateId}/submissions/export` | 导出 CSV |

### 审批 `/api/approvals`（特权用户）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/approvals/pending` | 待审批列表 |
| PUT | `/api/approvals/{submissionId}/approve` | 批准 |
| PUT | `/api/approvals/{submissionId}/reject` | 驳回 |

## 安全设计

### JWT 认证流程

- Token 结构 `{ sub: userId, username, roles: [...] }`，24h 过期
- 前端存 localStorage，每次请求带 `Authorization: Bearer <token>`
- 登出前端清除 Token（无状态登出）

### 过滤器链

```
请求 → CorsFilter → JwtAuthFilter → @PreAuthorize → Controller
```

### 角色权限矩阵

| 操作 | ADMIN | PRIVILEGED | USER |
|------|-------|------------|------|
| 用户管理 | ✅ | ❌ | ❌ |
| 创建/编辑/发布表单 | ✅ | ✅ | ✅ |
| 删除表单 | ✅ | ✅(自己) | ✅(自己) |
| 填报数据 | ✅ | ✅ | ✅ |
| 查看/导出自己表单数据 | ✅ | ✅ | ✅ |
| 审批数据 | ✅ | ✅ | ❌ |
| 分配角色 | ✅ | ❌ | ❌ |

## Camunda 7 流程引擎

### 集成方式

- Camunda 7 Community embedded engine，通过 `camunda-bpm-spring-boot-starter` 集成
- 与业务表共享数据源（H2/MySQL）
- 仅在 `demo-workflow` 模块内通过 Java API 调用

### 审批流程 BPMN

```
开始 → 待审批 → 批准 → 数据生效
               → 驳回 → 结束
```

- 流程变量：`submissionId`、`approverId`、`approved`、`comment`
- 数据提交时启动流程实例，审批通过 Camunda Delegate 回调更新 `form_submission.status`
- BPMN 文件存放于 `demo-workflow/src/main/resources/processes/`

### 对外服务接口

```java
public interface ApprovalService {
    void startApproval(Long submissionId);
    void approve(String taskId, Long approverId);
    void reject(String taskId, Long approverId, String reason);
    List<TaskDto> getPendingTasks(Long userId);
}
```

## 前端架构

### 路由

| 路径 | 页面 | 权限 |
|------|------|------|
| `/login` | 登录页 | 公开 |
| `/register` | 注册页 | 公开 |
| `/` | 仪表盘 | 登录用户 |
| `/forms/templates` | 我的表单 | 登录用户 |
| `/forms/templates/new` | 新建表单（设计器） | 登录用户 |
| `/forms/templates/:id` | 编辑表单 | 表单所有者 |
| `/forms/templates/:id/submissions` | 查看填报数据 | 表单所有者 |
| `/forms/submit` | 可用表单列表 | 登录用户 |
| `/forms/submit/:id` | 填写表单 | 登录用户 |
| `/forms/submissions/my` | 我的填报记录 | 登录用户 |
| `/approvals/pending` | 待审批 | 特权用户 |
| `/admin/users` | 用户管理 | 管理员 |

### 核心组件

- **FormDesignerPage**：拖拽表单设计器
  - `FieldPalette`（左侧字段类型面板）
  - `DesignCanvas`（中间拖放画布）
  - `FieldConfigPanel`（右侧属性配置）
- **FormSubmitPage**：动态渲染表单，根据 schema 字段类型映射对应 Ant Design 组件
- **ProtectedRoute**：鉴权包装组件

### 状态管理

- `AuthContext`：当前用户信息、Token、登录/登出
- `FormDesignContext`：画布字段列表、选中字段、拖拽状态（仅设计器内）

## 错误处理

### 后端

- `@RestControllerAdvice` 全局异常处理，映射 HTTP 状态码
- 自定义 `BusinessException` 含错误码
- 参数校验用 `jakarta.validation`，校验失败返回字段级错误

### 前端

- Axios 拦截器统一处理：401 → 跳转登录、403 → 无权限提示、网络异常 → Toast 提示

## 测试策略

| 层级 | 工具 | 覆盖重点 |
|------|------|---------|
| Service | JUnit 5 + Mockito | 业务逻辑、边界条件 |
| Repository | JUnit 5 + H2 | SQL 正确性 |
| Controller | MockMvc | 接口契约、权限校验 |
| 前端组件 | Vitest + React Testing Library | 渲染、交互行为 |
| E2E | Playwright | 注册→登录→创建表单→填报→审批 全流程 |

目标：后端核心模块覆盖率 > 70%，E2E 覆盖主流程。

## 开发环境搭建

```bash
git clone <repo-url>
cd demo-form
mvn clean install -DskipTests
cd demo-web && mvn spring-boot:run      # 后端 :8080
cd demo-frontend && npm install && npm run dev  # 前端 :5173
```

访问 `http://localhost:5173`，admin/admin 登录。Maven 本地仓库路径：`/Users/fan/software/apache-maven-repo`。
