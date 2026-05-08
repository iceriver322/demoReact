# Design: 审批系统增强 + 登录安全 + 无需审批选项

**Date:** 2026-05-08
**Project:** demoReact

## Context

对现有表单审批系统进行 7 项功能增强，涵盖审批体验优化、登录安全加固、表单流程扩展。

---

## Features

### 1. 待审批页面 — 弹窗展示审批数据

审批列表行移除"通过/拒绝"按钮，改为点击行弹出 Modal 展示审批数据的格式化键值对（与导出 CSV 格式一致，非 JSON）。Modal 底部放置通过/拒绝按钮。

### 2. 待审批页面 — 过滤不可审批条目

后端查询待审批任务时，自动排除当前用户自己提交的条目（self-approval prevention）。

### 3. 新建表单加"取消"按钮

表单设计器保存按钮旁增加"取消"按钮，点击返回上一页，无确认提示。

### 4. 数据按钮展示填报数据（格式化展示）

"我的表单"→"数据"按钮打开页面，使用 `<Descriptions>` 组件展示 `label: value` 格式的填报数据，包含字段值 + 提交人 + 提交时间 + 审批状态。

### 5. Admin 首次登录强制修改密码

密码过期用户登录时返回特定状态码 `PASSWORD_EXPIRED (1003)`，不签发 JWT。前端检测后跳转 `/change-password` 页面，用户输入旧密码+新密码完成修改后获取正常 JWT。

### 6. 登录密码3次错误锁定

`sys_user` 表新增 `login_attempts`(int) 和 `lock_time`(datetime) 字段。登录失败3次锁定30分钟，自动解锁。Admin 可通过 `PUT /api/users/{id}/unlock` 手动解锁。

### 7. 表单无需审批选项

- `SubmissionStatus` 新增 `SUBMITTED("已提交")` 状态
- `form_template` 新增 `need_approval` 字段（boolean, default true）
- 提交时 `need_approval == false` → 直接设为 `SUBMITTED`，跳过 Camunda 流程
- `need_approval == true`（默认）→ 现有流程：PENDING → 审批

---

## Shared Component

### SubmissionDataDisplay

`demo-frontend/src/components/SubmissionDataDisplay.tsx`

Props:
```typescript
interface SubmissionDataDisplayProps {
  schemaJson: string;  // 字段定义 JSON
  dataJson: string;    // 提交数据 JSON
  extra?: { label: string; value: string }[];  // 额外字段
}
```

Used by: ApprovalPage (弹窗), SubmissionListPage (数据页面)

---

## Files To Change

### Backend

| File | Change |
|------|--------|
| `demo-common/.../SubmissionStatus.java` | Add `SUBMITTED` |
| `demo-form-engine/.../FormTemplate.java` | Add `needApproval` |
| `demo-form-engine/.../TemplateCreateRequest.java` | Add `needApproval` |
| `demo-form-engine/.../TemplateUpdateRequest.java` | Add `needApproval` |
| `demo-user/.../SysUser.java` | Add `loginAttempts`, `lockTime` |
| `demo-user/.../UserService.java` | Add lock/unlock/password-change logic |
| `demo-workflow/.../ApprovalService.java` | Add `userId` param to `getPendingTasks()` |
| `demo-workflow/.../impl/ApprovalServiceImpl.java` | Filter by `submitterId`, add `schemaJson` to TaskDto |
| `demo-web/.../AuthController.java` | Login: expired check + lock check + new changePassword endpoint |
| `demo-web/.../UserController.java` | New unlock endpoint |
| `demo-web/.../FormSubmissionController.java` | Check `needApproval` to decide approval flow |
| `demo-web/.../FormTemplateController.java` | Pass `needApproval` in create/update |
| `demo-web/.../ApprovalController.java` | Pass `userId` to service |

### Frontend

| File | Change |
|------|--------|
| `demo-frontend/src/components/SubmissionDataDisplay.tsx` | New shared component |
| `demo-frontend/src/pages/ApprovalPage.tsx` | Modal + remove row buttons + filter |
| `demo-frontend/src/pages/FormDesignerPage.tsx` | Add cancel button + needApproval switch |
| `demo-frontend/src/pages/SubmissionListPage.tsx` | Use SubmissionDataDisplay |
| `demo-frontend/src/pages/FormSubmitPage.tsx` | Add SUBMITTED status display |
| `demo-frontend/src/pages/MySubmissionsPage.tsx` | Add SUBMITTED status display |
| `demo-frontend/src/pages/LoginPage.tsx` | Handle PASSWORD_EXPIRED + ACCOUNT_LOCKED |
| `demo-frontend/src/pages/ChangePasswordPage.tsx` | New page |
| `demo-frontend/src/router.tsx` | Add change-password route |

---

## Implementation Order

1. **Step 7 (need approval option)** — Enum, DB field, judgment logic
2. **Step 4 (data display component)** — Build shared component first
3. **Step 1+2 (approval modal + filtering)** — Reuse component
4. **Step 3 (cancel button)** — Independent, trivial
5. **Step 5+6 (auth security)** — Backend core logic, do last

---

## Verification

1. `$MVN test $REPO` — All backend tests pass
2. `npm run build` — Frontend builds without errors
3. Manual testing:
   - Need-approval toggle works in form designer
   - Non-approval form submissions show "已提交" status, not in pending list
   - Approval modal shows formatted data, row buttons removed
   - Pending list excludes own submissions
   - Cancel button navigates back
   - Data page shows formatted field values
   - Admin first login redirects to change password
   - 3 wrong attempts locks account with proper message
