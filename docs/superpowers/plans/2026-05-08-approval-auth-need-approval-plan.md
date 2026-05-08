# 审批+认证+无需审批 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实施 7 项功能增强：审批弹窗、不可审批过滤、取消按钮、数据格式化展示、admin强制改密、登录锁定、表单无需审批。

**Architecture:** 后端（Spring Boot + MyBatis-Plus + Camunda）+ 前端（React + Ant Design 5）。后端新增/修改实体字段、Service 逻辑、Controller 接口；前端新增共享组件、修改页面。实施顺序由底层到上层：先改后端实体/枚举，再建前端共享组件，再改各页面。

**Tech Stack:** Java 17, Spring Boot 3.2.5, MyBatis-Plus 3.5.6, Camunda 7, React 18, Vite 5, Ant Design 5, TypeScript

---

### Task 1: 后端 — 新增 SubmissionStatus.SUBMITTED + FormTemplate.needApproval + 提交流程判断

**Files:**
- Modify: `demo-common/src/main/java/com/demoform/common/enums/SubmissionStatus.java`
- Modify: `demo-form-engine/src/main/java/com/demoform/formengine/entity/FormTemplate.java`
- Modify: `demo-form-engine/src/main/java/com/demoform/formengine/dto/TemplateCreateRequest.java`
- Modify: `demo-form-engine/src/main/java/com/demoform/formengine/dto/TemplateUpdateRequest.java`
- Modify: `demo-web/src/main/java/com/demoform/web/controller/FormSubmissionController.java`
- Modify: `demo-web/src/main/java/com/demoform/web/controller/FormTemplateController.java`

- [ ] **Step 1: 添加 SUBMITTED 枚举常量**

在 SubmissionStatus.java 中增加一行：
```java
SUBMITTED("已提交"),
```

- [ ] **Step 2: FormTemplate 实体增加 needApproval 字段**

```java
/** 是否需要审批（默认 true） */
private Boolean needApproval;
```

- [ ] **Step 3: TemplateCreateRequest 增加 needApproval 字段**

```java
private Boolean needApproval;
```

- [ ] **Step 4: TemplateUpdateRequest 增加 needApproval 字段**

```java
private Boolean needApproval;
```

- [ ] **Step 5: FormTemplateController 传递 needApproval**

在 create() 中 `templateService.create(userId, request)` — needApproval 在 request 中会自动被 FormTemplateService.create() 使用。同样 update() 中也会通过 request 传。
需要检查 `FormTemplateService.create()` 和 `update()` 方法，它们目前直接用 request 设到 entity 上（常见模式）。如果它们是手写映射的则需加一行。

- [ ] **Step 6: FormSubmissionController.submit() 判断 needApproval**

修改 submit 方法：
```java
@PostMapping("/submissions")
public ApiResponse<FormSubmission> submit(@Valid @RequestBody SubmissionRequest request,
                                            Authentication auth) {
    Long userId = (Long) auth.getPrincipal();
    // 先获取模板信息判断是否需要审批
    com.demoform.formengine.mapper.FormTemplateMapper templateMapper = /* inject */;
    FormTemplate template = templateMapper.selectById(request.getTemplateId());
    if (template == null) {
        throw new BusinessException(ResultCode.FORM_NOT_FOUND);
    }
    
    FormSubmission submission = submissionService.submit(request.getTemplateId(), userId, request.getDataJson());
    
    if (Boolean.TRUE.equals(template.getNeedApproval())) {
        // 需要审批 → 启动 Camunda 流程
        approvalService.startApproval(submission.getId());
    } else {
        // 无需审批 → 直接设置为已提交状态
        submission.setStatus(SubmissionStatus.SUBMITTED.name());
        submissionMapper.updateById(submission);
    }
    
    return ApiResponse.success(submission);
}
```

- [ ] **Step 7: 在 FormSubmissionController 中注入 FormTemplateMapper**

在 `FormSubmissionController` 中增加：
```java
private final FormTemplateMapper templateMapper;
```
并添加对应的 import。由于 `@RequiredArgsConstructor` 会自动注入，只需声明字段即可。

- [ ] **Step 8: 更新 ApprovalService.startApproval 校验逻辑**

目前 startApproval 会校验 submission 必须是 PENDING 状态。对于 SUBMITTED 状态的提交不会进入此方法，但为安全起见无需修改（因为只有 PENDING 才会进入）。

- [ ] **Step 9: 更新现有后端测试**

检查 `FormSubmissionServiceTest.java` — 确保 submit 方法的单元测试不受影响（它只创建记录，状态判断在 Controller）。

Run: `$MVN test $REPO`
Expected: 27 tests passing

- [ ] **Step 10: Commit**

```bash
git add -A && git commit -m "feat: add SUBMITTED status and needApproval field"
```

---

### Task 2: 后端 — ApprovalService 过滤不可审批 + TaskDto 增加 schemaJson

**Files:**
- Modify: `demo-workflow/src/main/java/com/demoform/workflow/dto/TaskDto.java`
- Modify: `demo-workflow/src/main/java/com/demoform/workflow/service/ApprovalService.java`
- Modify: `demo-workflow/src/main/java/com/demoform/workflow/service/impl/ApprovalServiceImpl.java`
- Modify: `demo-web/src/main/java/com/demoform/web/controller/ApprovalController.java`
- Modify: `demo-web/src/test/java/com/demoform/web/controller/ApprovalControllerTest.java`

- [ ] **Step 1: TaskDto 增加 schemaJson 字段**

```java
@Data @Builder
public class TaskDto {
    private String taskId;
    private String processInstanceId;
    private String name;
    private Date createTime;
    private Map<String, Object> variables;
    private String submissionData;
    private String templateName;
    private String schemaJson;  // NEW: 模板字段定义 JSON
}
```

- [ ] **Step 2: ApprovalService 接口 getPendingTasks 增加 userId 参数**

```java
List<TaskDto> getPendingTasks(Long userId);
```

- [ ] **Step 3: ApprovalServiceImpl.getPendingTasks 修改**

```java
@Override
public List<TaskDto> getPendingTasks(Long userId) {
    List<Task> tasks = taskService.createTaskQuery()
            .initializeFormKeys()
            .list();
    return tasks.stream()
            .map(task -> {
                Map<String, Object> variables = taskService.getVariables(task.getId());
                String submissionData = null;
                String templateName = null;
                String schemaJson = null;
                Object submissionIdObj = variables.get("submissionId");
                if (submissionIdObj instanceof Number num) {
                    Long submissionId = num.longValue();
                    FormSubmission submission = submissionMapper.selectById(submissionId);
                    if (submission != null) {
                        // 过滤：跳过当前用户自己的提交
                        if (submission.getSubmitterId().equals(userId)) {
                            return null;
                        }
                        submissionData = submission.getDataJson();
                        FormTemplate template = templateMapper.selectById(submission.getTemplateId());
                        if (template != null) {
                            templateName = template.getName();
                            schemaJson = template.getSchemaJson();  // NEW
                        }
                    }
                }
                return TaskDto.builder()
                        .taskId(task.getId())
                        .processInstanceId(task.getProcessInstanceId())
                        .name(task.getName())
                        .createTime(task.getCreateTime())
                        .variables(variables)
                        .submissionData(submissionData)
                        .templateName(templateName)
                        .schemaJson(schemaJson)  // NEW
                        .build();
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
}
```

- [ ] **Step 4: ApprovalController.pending() 传入 userId**

```java
@GetMapping("/pending")
public ApiResponse<List<TaskDto>> pending(Authentication auth) {
    Long userId = (Long) auth.getPrincipal();
    return ApiResponse.success(approvalService.getPendingTasks(userId));
}
```

- [ ] **Step 5: 更新 ApprovalControllerTest**

```java
class ApprovalControllerTest {
    // ... existing mocks unchanged ...

    @Test
    void shouldRejectSelfApproval() throws Exception {
        // same as before - tests self-approval prevention at approve/reject endpoints
        // This test remains unchanged
    }

    @Test
    void shouldRejectSelfRejection() throws Exception {
        // unchanged
    }

    @Test
    void shouldAllowApproveOtherSubmission() throws Exception {
        // unchanged
    }
}
```

Note: The existing tests mock `approvalService` completely, so the interface change (`getPendingTasks(Long userId)`) doesn't affect them. The filter logic in `getPendingTasks` is unit-tested at the service level.

- [ ] **Step 6: Run tests**

Run: `$MVN test $REPO`
Expected: All tests passing

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "feat: filter self-submissions from pending list and add schemaJson to TaskDto"
```

---

### Task 3: 前端 — 创建 SubmissionDataDisplay 共享组件

**Files:**
- Create: `demo-frontend/src/components/SubmissionDataDisplay.tsx`

- [ ] **Step 1: 创建 SubmissionDataDisplay 组件**

```tsx
import React from 'react';
import { Descriptions, Tag } from 'antd';

export interface SubmissionDataDisplayProps {
  schemaJson: string;
  dataJson: string;
  extra?: { label: string; value: React.ReactNode }[];
}

interface SchemaField {
  name: string;
  label: string;
  type: string;
}

const SubmissionDataDisplay: React.FC<SubmissionDataDisplayProps> = ({ schemaJson, dataJson, extra }) => {
  let schemaFields: SchemaField[] = [];
  try {
    schemaFields = JSON.parse(schemaJson || '[]');
  } catch { /* ignore */ }

  let data: Record<string, any> = {};
  try {
    data = JSON.parse(dataJson || '{}');
  } catch { /* ignore */ }

  const items: { label: string; value: React.ReactNode }[] = [];

  for (const field of schemaFields) {
    const val = data[field.name];
    let displayVal: React.ReactNode = '-';
    if (val !== undefined && val !== null) {
      if (Array.isArray(val)) {
        displayVal = val.join(', ');
      } else {
        displayVal = String(val);
      }
    }
    items.push({ label: field.label || field.name, value: displayVal });
  }

  if (extra) {
    items.push(...extra);
  }

  return (
    <Descriptions column={1} size="small" bordered>
      {items.map((item, idx) => (
        <Descriptions.Item key={idx} label={item.label}>{item.value}</Descriptions.Item>
      ))}
    </Descriptions>
  );
};

export default SubmissionDataDisplay;
```

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "feat: add SubmissionDataDisplay shared component"
```

---

### Task 4: 前端 — ApprovalPage 弹窗展示 + 移除行按钮 + 集成 schemaJson

**Files:**
- Modify: `demo-frontend/src/pages/ApprovalPage.tsx`
- Modify: `demo-frontend/src/api/approval.ts`

- [ ] **Step 1: TaskDto 增加 schemaJson**

```typescript
export interface TaskDto {
  taskId: string;
  processInstanceId: string;
  name: string;
  createTime: string;
  variables: Record<string, any>;
  submissionData?: string;
  templateName?: string;
  schemaJson?: string;  // NEW
}
```

- [ ] **Step 2: 重写 ApprovalPage**

```tsx
import { useState, useEffect } from 'react';
import { Table, Button, Space, message, Modal, Input } from 'antd';
import { CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { approvalApi, TaskDto } from '../api/approval';
import SubmissionDataDisplay from '../components/SubmissionDataDisplay';

const ApprovalPage: React.FC = () => {
  const [data, setData] = useState<TaskDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedTask, setSelectedTask] = useState<TaskDto | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const tasks = await approvalApi.pending();
      setData(tasks);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleApprove = async (submissionId: number) => {
    try {
      await approvalApi.approve(submissionId);
      message.success('已批准');
      setModalVisible(false);
      load();
    } catch (err: any) { message.error(err.message || '操作失败'); }
  };

  const handleReject = async (submissionId: number) => {
    Modal.confirm({
      title: '驳回原因',
      content: <Input.TextArea id="reject-reason" rows={2} placeholder="可选填写驳回原因" />,
      onOk: async () => {
        const el = document.getElementById('reject-reason') as HTMLTextAreaElement;
        await approvalApi.reject(submissionId, el?.value);
        message.success('已驳回');
        setModalVisible(false);
        load();
      },
    });
  };

  const showDetail = (record: TaskDto) => {
    setSelectedTask(record);
    setModalVisible(true);
  };

  const submissionId = selectedTask ? Number(selectedTask.variables?.submissionId) : null;

  const columns = [
    { title: '表单名称', dataIndex: 'templateName', width: 160 },
    { title: '任务名称', dataIndex: 'name', width: 120 },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>待审批列表</h3>
      <Table
        columns={columns}
        dataSource={data}
        rowKey="taskId"
        loading={loading}
        pagination={false}
        onRow={(record) => ({
          onClick: () => showDetail(record),
          style: { cursor: 'pointer' },
        })}
      />
      <Modal
        title="审批详情"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={640}
        footer={
          submissionId ? (
            <Space>
              <Button type="primary" icon={<CheckOutlined />}
                onClick={() => handleApprove(submissionId)}>通过</Button>
              <Button danger icon={<CloseOutlined />}
                onClick={() => handleReject(submissionId)}>拒绝</Button>
              <Button onClick={() => setModalVisible(false)}>关闭</Button>
            </Space>
          ) : null
        }
      >
        {selectedTask && selectedTask.schemaJson && selectedTask.submissionData ? (
          <SubmissionDataDisplay
            schemaJson={selectedTask.schemaJson}
            dataJson={selectedTask.submissionData}
          />
        ) : (
          <p>暂无数据</p>
        )}
      </Modal>
    </div>
  );
};

export default ApprovalPage;
```

- [ ] **Step 3: Run frontend type check**

Run: `npx tsc --noEmit` (in demo-frontend directory)
Expected: No type errors

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: redesign ApprovalPage with data modal and remove row buttons"
```

---

### Task 5: 前端 — FormDesignerPage 取消按钮 + needApproval 开关

**Files:**
- Modify: `demo-frontend/src/pages/FormDesignerPage.tsx`
- Modify: `demo-frontend/src/api/form.ts`

- [ ] **Step 1: FormTemplate 类型增加 needApproval**

```typescript
export interface FormTemplate {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  schemaJson: string;
  status: string;
  needApproval?: boolean;  // NEW
  createdAt: string;
}
```

- [ ] **Step 2: FormDesignerPage 增加取消按钮和 needApproval 开关**

修改 import 部分，增加 `Switch`:
```tsx
import { Row, Col, Input, Button, Space, Switch, message } from 'antd';
```

在 state 中增加：
```tsx
const [needApproval, setNeedApproval] = useState(true);
```

在 `useEffect` 加载模板时增加：
```tsx
setNeedApproval(t.needApproval !== false); // 默认 true
```

修改 handleSave 发送 needApproval:
```tsx
if (isEdit) {
  await formTemplateApi.update(Number(id), { name, description, schemaJson: schema, needApproval });
} else {
  const created = await formTemplateApi.create({ name, description, schemaJson: schema, needApproval });
}
```

在 header Space 中保存按钮前加入：
```tsx
<Space>
  <span>需要审批</span>
  <Switch checked={needApproval} onChange={setNeedApproval} />
</Space>
```

在保存按钮旁加入取消按钮：
```tsx
<Button onClick={() => navigate(-1)}>取消</Button>
```

完整修改后的 button area:
```tsx
<Space>
  <Space>
    <span>需要审批</span>
    <Switch checked={needApproval} onChange={setNeedApproval} />
  </Space>
  <Button icon={<SaveOutlined />} onClick={handleSave}>保存</Button>
  <Button onClick={() => navigate(-1)}>取消</Button>
  {isEdit && (
    <Button type="primary" icon={<SendOutlined />} onClick={handlePublish}>发布</Button>
  )}
</Space>
```

- [ ] **Step 3: Run type check**

Run: `npx tsc --noEmit`
Expected: No errors

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: add cancel button and needApproval switch to FormDesignerPage"
```

---

### Task 6: 前端 — 各页面 SUBMITTED 状态显示 + SubmissionListPage 格式化展示

**Files:**
- Modify: `demo-frontend/src/pages/FormSubmitPage.tsx`
- Modify: `demo-frontend/src/pages/SubmissionListPage.tsx`
- Modify: `demo-frontend/src/pages/MySubmissionsPage.tsx`
- Modify: `demo-frontend/src/pages/FormTemplateListPage.tsx`

**Step 1: FormSubmitPage — 添加 SUBMITTED 状态**

```tsx
const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red', SUBMITTED: 'blue',
};
const statusText: Record<string, string> = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', SUBMITTED: '已提交',
};
```

提交成功消息改为动态：
```tsx
await formSubmissionApi.submit(Number(id!), JSON.stringify(formattedValues));
// 在 loadData 之后判断最新提交状态
loadData();
message.success('提交成功');
```

**Step 2: SubmissionListPage — 使用 SubmissionDataDisplay + SUBMITTED 状态**

```tsx
import SubmissionDataDisplay from '../components/SubmissionDataDisplay';

const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red', SUBMITTED: 'blue',
};

const statusText: Record<string, string> = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', SUBMITTED: '已提交',
};
```

新增状态列使用 statusText:
```tsx
{ title: '状态', dataIndex: 'status', width: 100,
  render: (s: string) => <Tag color={statusColor[s]}>{statusText[s] || s}</Tag> },
```

在 ID 前增加数据展示列 — 通过 `<a>` 点击弹出 Modal 或直接增加可展开行。对于更简洁的方式，增加一个"数据"操作按钮，点击弹出 Modal 展示格式化数据：

在 columns 中增加：
```tsx
{
  title: '数据', width: 120,
  render: (_: any, record: FormSubmission) => {
    const [schemaJson, setSchemaJson] = useState<string>('');
    // ... 在父组件中处理
  }
}
```

实际上更简洁的做法：在页面加载时一起获取模板的 schemaJson（通过 `formTemplateApi.detail(Number(id!))`），然后在 Table 中增加一个可展开行或 Modal。

推荐方案：在现有页面加载 submission 时已获取到 templateId。当前页面通过 id (url param) 传入 templateId。修改为在 `useEffect` 中也加载模板信息：

```tsx
const [template, setTemplate] = useState<FormTemplate | null>(null);

useEffect(() => {
  if (id) {
    setLoading(true);
    Promise.all([
      formSubmissionApi.listByTemplate(Number(id), { page: 1, size: 100 }),
      formTemplateApi.detail(Number(id)),
    ])
    .then(([r, t]) => {
      setData(r.records);
      setTemplate(t);
    })
    .finally(() => setLoading(false));
  }
}, [id]);
```

增加"查看"操作按钮：
```tsx
{
  title: '操作', width: 100,
  render: (_: any, record: FormSubmission) => (
    <Button type="link" size="small"
      onClick={() => openDetail(record)}>查看</Button>
  ),
}
```

增加 Modal：
```tsx
const [detailVisible, setDetailVisible] = useState(false);
const [detailSubmission, setDetailSubmission] = useState<FormSubmission | null>(null);

const openDetail = (record: FormSubmission) => {
  setDetailSubmission(record);
  setDetailVisible(true);
};

// ... in JSX:
<Modal
  title="填报数据详情"
  open={detailVisible}
  onCancel={() => setDetailVisible(false)}
  footer={<Button onClick={() => setDetailVisible(false)}>关闭</Button>}
  width={640}
>
  {detailSubmission && template && (
    <SubmissionDataDisplay
      schemaJson={template.schemaJson}
      dataJson={detailSubmission.dataJson}
      extra={[
        { label: '提交人', value: String(detailSubmission.submitterId) },
        { label: '提交时间', value: detailSubmission.createdAt },
        { label: '状态', value: <Tag color={statusColor[detailSubmission.status]}>{statusText[detailSubmission.status] || detailSubmission.status}</Tag> },
      ]}
    />
  )}
</Modal>
```

**Step 3: MySubmissionsPage — 添加 SUBMITTED 状态**

```tsx
const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red', SUBMITTED: 'blue',
};
```

**Step 4: FormTemplateListPage — 如需要，添加 needApproval 列显示**

查看 `demo-frontend/src/pages/FormTemplateListPage.tsx` 是否需要显示是否需要审批标记。可选添加 Tag 显示。

- [ ] **Step 5: Run type check**

Run: `npx tsc --noEmit`
Expected: No errors

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: add SUBMITTED status display and SubmissionDataDisplay integration"
```

---

### Task 7: 后端 — SysUser 增加 loginAttempts/lockTime + UserService 锁定逻辑

**Files:**
- Modify: `demo-user/src/main/java/com/demoform/user/entity/SysUser.java`
- Modify: `demo-user/src/main/java/com/demoform/user/service/UserService.java`
- Modify: `demo-user/src/main/java/com/demoform/user/service/impl/UserServiceImpl.java`
- Modify: `demo-user/src/test/java/com/demoform/user/UserServiceTest.java` (if exists)

- [ ] **Step 1: SysUser 增加字段**

```java
/** 登录失败次数 */
private Integer loginAttempts;

/** 锁定时间（null=未锁定） */
private LocalDateTime lockTime;
```

- [ ] **Step 2: UserService 增加方法**

```java
/** 记录登录失败，检查是否需要锁定 */
void recordLoginFailure(String username);

/** 重置登录失败计数（登录成功时调用） */
void resetLoginAttempts(String username);

/** 检查账户是否被锁定 */
void checkAccountLocked(String username);

/** 管理员解锁用户 */
void unlockUser(Long userId);
```

- [ ] **Step 3: UserServiceImpl 实现**

```java
@Override
@Transactional
public void recordLoginFailure(String username) {
    SysUser user = findByUsername(username);
    if (user == null) return;
    
    int attempts = (user.getLoginAttempts() == null ? 0 : user.getLoginAttempts()) + 1;
    user.setLoginAttempts(attempts);
    
    if (attempts >= 3) {
        user.setLockTime(LocalDateTime.now());
    }
    
    userMapper.updateById(user);
}

@Override
@Transactional
public void resetLoginAttempts(String username) {
    SysUser user = findByUsername(username);
    if (user == null) return;
    user.setLoginAttempts(0);
    user.setLockTime(null);
    userMapper.updateById(user);
}

@Override
public void checkAccountLocked(String username) {
    SysUser user = findByUsername(username);
    if (user == null) return;
    
    LocalDateTime lockTime = user.getLockTime();
    if (lockTime != null) {
        // 检查是否已过30分钟
        if (lockTime.plusMinutes(30).isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        } else {
            // 已过30分钟，自动解锁
            user.setLoginAttempts(0);
            user.setLockTime(null);
            userMapper.updateById(user);
        }
    }
}

@Override
@Transactional
public void unlockUser(Long userId) {
    SysUser user = userMapper.selectById(userId);
    if (user == null) {
        throw new BusinessException(ResultCode.USER_NOT_FOUND);
    }
    user.setLoginAttempts(0);
    user.setLockTime(null);
    userMapper.updateById(user);
}
```

- [ ] **Step 3: 更新 UserServiceTest**

如果 UserServiceTest 存在，需要更新 mock 以处理新字段。新增测试：
```java
@Test
void shouldLockAccountAfterThreeFailures() {
    SysUser user = new SysUser();
    user.setId(1L);
    user.setUsername("testuser");
    user.setLoginAttempts(0);
    when(userMapper.selectOne(any())).thenReturn(user);
    
    userService.recordLoginFailure("testuser");
    assertEquals(1, user.getLoginAttempts());
    assertNull(user.getLockTime());
    
    userService.recordLoginFailure("testuser");
    assertEquals(2, user.getLoginAttempts());
    
    userService.recordLoginFailure("testuser");
    assertEquals(3, user.getLoginAttempts());
    assertNotNull(user.getLockTime());
}

@Test
void shouldUnlockAfter30Minutes() {
    SysUser user = new SysUser();
    user.setId(1L);
    user.setUsername("testuser");
    user.setLoginAttempts(3);
    user.setLockTime(LocalDateTime.now().minusMinutes(31));
    when(userMapper.selectOne(any())).thenReturn(user);
    
    // 不应抛出异常
    userService.checkAccountLocked("testuser");
    assertEquals(0, user.getLoginAttempts());
    assertNull(user.getLockTime());
}
```

- [ ] **Step 4: Run tests**

Run: `$MVN test $REPO`
Expected: All tests passing

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: add login lockout logic (3 attempts, 30min unlock)"
```

---

### Task 8: 后端 — AuthController 登录锁定检测 + 密码过期检测 + changePassword 端点

**Files:**
- Modify: `demo-web/src/main/java/com/demoform/web/controller/AuthController.java`
- Modify: `demo-web/src/test/java/com/demoform/web/controller/AuthControllerTest.java`

- [ ] **Step 1: AuthController.login() 增加锁定和密码过期检测**

```java
/** 用户登录 —— 验证用户名密码，返回 JWT Token */
@PostMapping("/login")
public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    // 先检查账户是否被锁定
    userService.checkAccountLocked(request.getUsername());
    
    try {
        // Spring Security 认证
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    } catch (org.springframework.security.authentication.BadCredentialsException e) {
        // 记录登录失败
        userService.recordLoginFailure(request.getUsername());
        throw new BusinessException(ResultCode.PASSWORD_WRONG);
    }
    
    // 查询用户信息
    SysUser user = userService.findByUsername(request.getUsername());
    
    // 检查账户状态
    if (user.getStatus() == 0) {
        throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
    }
    
    // 检查密码是否过期
    if (user.getPasswordExpireDate() != null && 
        user.getPasswordExpireDate().isBefore(java.time.LocalDate.now())) {
        throw new BusinessException(ResultCode.PASSWORD_EXPIRED);
    }
    
    // 登录成功，重置失败计数
    userService.resetLoginAttempts(request.getUsername());
    
    // 生成 JWT Token
    List<String> roles = userService.getUserRoleCodes(user.getId());
    String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);
    
    // 构建响应
    LoginResponse resp = new LoginResponse(token, user.getId(), user.getUsername(),
            user.getEmail(), roles);
    return ApiResponse.success(resp);
}
```

- [ ] **Step 2: 添加 changePassword 端点到 AuthController**

```java
/** 修改密码（密码过期时使用，不需要认证，请求体中包含旧密码用于验证） */
@PutMapping("/password")
public ApiResponse<LoginResponse> changePassword(@Valid @RequestBody com.demoform.user.dto.ChangePasswordRequest request) {
    // 通过用户名查找用户（不需要认证）
    SysUser user = userService.findByUsername(request.getUsername());
    if (user == null) {
        throw new BusinessException(ResultCode.USER_NOT_FOUND);
    }
    
    // 调用 UserService 验证旧密码并更新
    userService.changePassword(user.getId(), request);
    
    // 登录成功，重置失败计数
    userService.resetLoginAttempts(request.getUsername());
    
    // 生成新 JWT
    List<String> roles = userService.getUserRoleCodes(user.getId());
    String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);
    
    LoginResponse resp = new LoginResponse(token, user.getId(), user.getUsername(),
            user.getEmail(), roles);
    return ApiResponse.success(resp);
}
```

注意：需要配合 ChangePasswordRequest 增加 `username` 字段（当前只有 `oldPassword`, `newPassword`, `confirmPassword`）。或者前端的 `/change-password` 页面在 URL 中带 username 参数，或将 username 放在 request body 中。

推荐：ChangePasswordRequest 增加 username 字段。

- [ ] **Step 3: 更新 ChangePasswordRequest DTO**

```java
// 增加 username 字段
@Data
public class ChangePasswordRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
    private String confirmPassword; // optional
}
```

- [ ] **Step 4: 修改 AuthControllerTest**

更新 `shouldLoginSuccessfully` 测试：

```java
@Test
void shouldLoginSuccessfully() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername("admin");
    request.setPassword("admin");

    SysUser user = new SysUser();
    user.setId(1L);
    user.setUsername("admin");
    user.setStatus(1);
    user.setEmail("admin@demoform.com");
    user.setPasswordExpireDate(java.time.LocalDate.now().plusDays(30)); // not expired

    when(authenticationManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken("admin", "admin"));
    when(userService.findByUsername("admin")).thenReturn(user);
    when(userService.getUserRoleCodes(1L)).thenReturn(List.of("ROLE_ADMIN"));
    when(jwtUtil.generateToken(1L, "admin", List.of("ROLE_ADMIN")))
            .thenReturn("test-token");

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").value("test-token"));
}
```

新增密码过期测试和锁定测试。

- [ ] **Step 5: Run tests**

Run: `$MVN test $REPO`
Expected: All tests passing

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: add login lock check, password expiry check, and changePassword endpoint"
```

---

### Task 9: 后端 — UserController 解锁端点

**Files:**
- Modify: `demo-web/src/main/java/com/demoform/web/controller/UserController.java`

- [ ] **Step 1: 增加解锁端点**

```java
/** 解锁用户（管理员） */
@PutMapping("/{id}/unlock")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<Void> unlock(@PathVariable Long id) {
    userService.unlockUser(id);
    return ApiResponse.success();
}
```

- [ ] **Step 2: Run tests**

Run: `$MVN test $REPO`
Expected: All tests passing

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: add admin unlock endpoint"
```

---

### Task 10: 前端 — LoginPage 错误处理 + ChangePasswordPage + 路由

**Files:**
- Create: `demo-frontend/src/pages/ChangePasswordPage.tsx`
- Modify: `demo-frontend/src/pages/LoginPage.tsx`
- Modify: `demo-frontend/src/contexts/AuthContext.tsx`
- Modify: `demo-frontend/src/App.tsx`
- Modify: `demo-frontend/src/api/auth.ts`

- [ ] **Step 1: AuthContext login 支持抛出业务错误码**

修改 AuthContext login 方法，让它直接透传错误，不在 login 内部处理 token。当前的 login 直接保存 token 并设置 user，但如果后端返回 PASSWORD_EXPIRED 等错误，应该不保存 token。

现在的 login 实现：
```typescript
const login = useCallback(async (params: LoginParams) => {
    const result = await authApi.login(params);
    localStorage.setItem('token', result.token);
    const userInfo: UserInfo = { ... };
    setUser(userInfo);
  }, []);
```

修改为仅在成功后才保存：
```typescript
const login = useCallback(async (params: LoginParams) => {
    const result = await authApi.login(params);
    localStorage.setItem('token', result.token);
    const userInfo: UserInfo = {
      id: result.userId,
      username: result.username,
      email: result.email,
      roles: result.roles,
      status: 1,
      passwordExpireDate: '',
    };
    setUser(userInfo);
  }, []);
```

（实际上逻辑没变 — axios 拦截器会拦截错误并抛异常，所以如果后端返回 PASSWORD_EXPIRED, login() 会抛异常，token 不会保存。）

- [ ] **Step 2: LoginPage 处理 ACCOUNT_LOCKED 和 PASSWORD_EXPIRED**

```tsx
const onFinish = async (values: { username: string; password: string }) => {
  try {
    await login(values);
    message.success('登录成功');
    navigate('/');
  } catch (err: any) {
    const code = err.response?.data?.code;
    if (code === 1004) { // PASSWORD_EXPIRED
      // 跳转到修改密码页面
      navigate(`/change-password?username=${encodeURIComponent(values.username)}`);
      return;
    }
    if (code === 1005) { // ACCOUNT_LOCKED
      message.error('账户已被锁定，请30分钟后重试或联系管理员解锁');
    } else {
      message.error('用户名或密码错误');
    }
    // 清空密码
    const passwordField = document.querySelector('input[type="password"]') as HTMLInputElement;
    if (passwordField) passwordField.value = '';
  }
};
```

- [ ] **Step 3: 创建 ChangePasswordPage**

```tsx
import { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '../api/auth';

const ChangePasswordPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const username = searchParams.get('username') || '';
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: { oldPassword: string; newPassword: string; confirmPassword: string }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('两次输入的密码不一致');
      return;
    }
    setLoading(true);
    try {
      const result = await authApi.changePassword({
        username,
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      });
      // 保存新 token
      localStorage.setItem('token', result.token);
      message.success('密码修改成功');
      navigate('/');
    } catch (err: any) {
      message.error(err.response?.data?.message || err.message || '修改密码失败');
    } finally {
      setLoading(false);
    }
  };

  if (!username) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
        <Card title="修改密码" style={{ width: 400 }}>
          <p>缺少用户名参数，请重新登录。</p>
          <Button type="primary" onClick={() => navigate('/login')}>返回登录</Button>
        </Card>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
      <Card title="修改密码" style={{ width: 400 }}>
        <p style={{ marginBottom: 16 }}>用户：{username}</p>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="oldPassword" rules={[{ required: true, message: '请输入当前密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="当前密码" />
          </Form.Item>
          <Form.Item name="newPassword" rules={[{ required: true, message: '请输入新密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="新密码（至少8位，大小写字母+数字）" />
          </Form.Item>
          <Form.Item name="confirmPassword" rules={[{ required: true, message: '请确认新密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="确认新密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>确认修改</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default ChangePasswordPage;
```

- [ ] **Step 4: api/auth.ts 增加 changePassword 方法**

```typescript
export const authApi = {
  login: (data: LoginParams) => request.post<any, LoginResult>('/auth/login', data),
  register: (data: RegisterParams) => request.post('/auth/register', data),
  getMe: () => request.get<any, UserInfo>('/auth/me'),
  changePassword: (data: { username: string; oldPassword: string; newPassword: string }) =>
    request.put<any, LoginResult>('/auth/password', data),
};
```

（需要从 api/request.ts 中 import request）

- [ ] **Step 5: 添加路由到 App.tsx**

在公开路由部分增加：
```tsx
<Route path="/change-password" element={<ChangePasswordPage />} />
```

注意：change-password 应为公开路由（因为用户密码过期时没有有效 JWT）。

- [ ] **Step 6: Run type check**

Run: `npx tsc --noEmit`
Expected: No errors

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "feat: add ChangePasswordPage, login error handling, and route"
```

---

### Verification

1. `$MVN test $REPO` — All backend tests pass
2. `cd demo-frontend && npm run build` — Frontend builds without errors
3. Start backend: `$MVN -f demo-web/pom.xml spring-boot:run $REPO`
4. Start frontend: `cd demo-frontend && npm run dev`
5. Manual testing:
   - 表单设计器：可以看到"需要审批"开关，默认开启；有取消按钮
   - 关闭"需要审批"并发布→提交后状态为"已提交"（蓝色），不在审批列表
   - 打开"需要审批"→提交后状态为"待审批"
   - 审批页面：行上无按钮，点击行弹出格式化数据 Modal，有通过/拒绝按钮
   - 审批列表不显示自己的提交
   - 数据页面展示格式化 Descriptions 数据
   - admin 登录（密码过期时）→跳转修改密码页面→修改后获得 JWT
   - 密码错误→清空密码，提示；3次错误→锁定30分钟提示
6. `npm run test:e2e` — Playwright E2E tests (if applicable)
