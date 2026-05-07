# 填报数据页面改造 + CSV 导出增强

## 概述

改造"填报数据"菜单（`/forms/submit`）下的表单提交流程，使其支持：
1. 提交历史记录展示（schema 动态列）
2. 新增/修改提交（弹窗预填、生成新 PENDING 记录走审批）
3. 禁止自审批
4. CSV 导出增强

## 架构

### 涉及的模块

| 模块 | 改动点 |
|------|--------|
| `demo-frontend` — `FormSubmitPage.tsx` | 页面重写：分区域布局（表单信息 + 历史记录 + 新增/修改弹窗） |
| `demo-frontend` — `form.ts` | 新增 `listMyByTemplate()` API |
| `demo-form-engine` — 后端 | 新增 `listMyByTemplateId()` 接口及其实现 + Mapper 查询 |
| `demo-web` — `FormSubmissionController.java` | 新增 `/submissions/my/template/{templateId}` 端点 + CSV 导出增强 |
| `demo-web` — `ApprovalController.java` | approve/reject 增加提交者自审批校验 |
| `demo-common` — `ResultCode.java` | 新增 `CANNOT_APPROVE_SELF` 错误码 |

### 数据流

```
用户访问 /forms/submit/:id
  → 加载模板详情（formTemplateApi.detail(id)）
  → 加载该模板下自己的提交记录（formSubmissionApi.listMyByTemplate(id)）
  → 渲染：表单标题/描述 + 历史表格（schema 动态列） + 新增/修改按钮

新增提交：
  点击"新增提交" → Modal 弹出空表单（根据 schemaJson 渲染字段）
  → 填写 → 提交 → POST /api/forms/submissions → 刷新历史列表

修改提交：
  点击某行"修改" → Modal 弹出预填数据的表单
  → 修改 → 提交 → POST /api/forms/submissions（新记录）→ 刷新历史列表
```

## 详细设计

### 1. 前端 FormSubmitPage.tsx 重写

**页面布局：**

```
┌─ 表单信息区 ───────────────────────────┐
│  员工信息表                               │
│  这是一个测试表单                          │
│  [＋新增提交]                             │
├─ 提交历史 ───────────────────────────────┤
│  ┌─ 表格（schema 动态列）───────────────┐│
│  │  姓名 │ 年龄 │ 状态   │ 操作         ││
│  │ ─────┼──────┼───────┼────────────── ││
│  │  张三 │  30  │ 已通过 │ [修改]       ││
│  │  李四 │  25  │ 待审批 │ [修改]       ││
│  └──────────────────────────────────────┘│
└──────────────────────────────────────────┘
```

**关键设计：**

- 获取模板后，同时调用新 API 查询该模板下当前用户的所有提交记录
- 历史表格列头由 `schemaJson` 中的字段 `name`（或 `label`）动态生成
- 每行数据将 `dataJson` JSON.parse 后，按字段名取对应值显示；若值类型为对象（如 date 为 dayjs）则格式化显示
- 额外固定列：`status`（状态标签）、`操作`（修改按钮）
- "新增提交" 和 "修改" 共用同一个 Modal 表单组件，通过 `initialValues` 区分
- 提交成功后统一刷新历史列表

**弹窗设计：**

- 使用 Ant Design `Modal` + `Form`
- 表单字段根据 `schemaJson` 动态渲染（复用现有 `renderField` 逻辑）
- "新增"模式：初始化空表单
- "修改"模式：从该行 `dataJson` JSON.parse 后作为 `initialValues`

### 2. 新增后端 API

**新增端点：**

```
GET /api/forms/submissions/my/template/{templateId}
```

返回当前用户在该模板下的所有提交记录（不分页）。

**Controller：**

```java
@GetMapping("/submissions/my/template/{templateId}")
public ApiResponse<List<FormSubmission>> listMyByTemplate(
    @PathVariable Long templateId, Authentication auth) {
    Long userId = (Long) auth.getPrincipal();
    return ApiResponse.success(submissionService.listMyByTemplateId(templateId, userId));
}
```

**Service 新增：**

```java
List<FormSubmission> listMyByTemplateId(Long templateId, Long submitterId);
```

实现：调用 Mapper 新增方法，按 `templateId + submitterId` 查询。

**Mapper 新增：**

```java
List<FormSubmission> selectMyByTemplateId(@Param("templateId") Long templateId,
                                          @Param("submitterId") Long submitterId);
```

XML：`WHERE template_id = #{templateId} AND submitter_id = #{submitterId} AND deleted = 0 ORDER BY created_at DESC`

### 3. 禁止自审批

**改动位置：** `ApprovalController.java` 的 `approve` 和 `reject` 方法。

**逻辑：**

在 `approve` 和 `reject` 方法中，通过 `submissionService.findById(submissionId)` 获取提交记录，检查 `submitterId` 是否等于当前用户 ID，若相等则抛出业务异常。

**新增错误码：** `CANNOT_APPROVE_SELF(3002, "不能审批自己的提交")`

### 4. CSV 导出增强

增强现有 `/templates/{templateId}/submissions/export` 端点：

- 加载 `FormTemplate`，解析 `schemaJson` 获取字段名列表（取 `name` 或 `label`）
- 列头动态：schema 字段名 + `状态` + `填写人` + `填写时间`
- 每行数据从 `dataJson` 解析后按列取值
- 查询 `SysUserMapper` 将 `submitterId` 转为 `username`

### 5. 前端 API 新增

```typescript
// form.ts
listMyByTemplate: (templateId: number) =>
  request.get<any, FormSubmission[]>(`/forms/submissions/my/template/${templateId}`),
```

## 测试计划

### 后端单元测试

| 测试 | 覆盖 |
|------|------|
| `FormSubmissionServiceTest` | 新增 `listMyByTemplateId()` 方法测试 |
| `ApprovalControllerTest` | 自审批校验生效测试（提交者不能批准自己） |

### 前端验证

- 浏览器打开 `/forms/submit/:id` 验证布局
- 新增提交 → 弹窗 → 提交 → 刷新历史列表
- 修改已有提交 → 预填数据 → 修改 → 新记录
- 验证旧记录仍保留
