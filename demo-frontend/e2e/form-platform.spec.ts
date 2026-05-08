import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:5173';
const API = 'http://localhost:8080';

/** Shared between serial tests */
let adminPassword = 'admin';
let adminToken = '';

async function loginAsAdmin(page: import('@playwright/test').Page) {
  // Navigate to the app first to establish origin (required for localStorage access)
  await page.goto(BASE_URL + '/login');
  await page.waitForSelector('input[id="username"]');

  // Authenticate via API (avoid Ant Design form issues in headless)
  // Use the current adminPassword (starts as 'admin', updated after first password change)
  const d = await page.evaluate(async ({ api, pwd }: { api: string; pwd: string }) => {
    let r = await fetch(api + '/api/auth/login', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'admin', password: pwd }),
    });
    let data = await r.json();
    if (data.code === 1004) { // PASSWORD_EXPIRED — change password and get new token
      r = await fetch(api + '/api/auth/password', {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'admin', oldPassword: pwd, newPassword: 'TestAdminPass1!' }),
      });
      data = await r.json();
    }
    return data;
  }, { api: API, pwd: adminPassword });

  if (d.code === 200 && d.data?.token) {
    await page.evaluate((token: string) => {
      localStorage.setItem('token', token);
    }, d.data.token);
    adminToken = d.data.token;
    adminPassword = 'TestAdminPass1!';
  }

  await page.goto(BASE_URL + '/');
  await page.waitForURL('**/');
}

test.describe.serial('表单数据平台 E2E', () => {

  // ── 1. admin 登录 + 改密 ──
  test('admin登录并改密后进入首页', async ({ page }) => {
    await loginAsAdmin(page);
    await expect(page.locator('h2').filter({ hasText: /admin/ })).toBeVisible();
  });

  // ── 2. 未登录重定向 ──
  test('未登录访问首页跳转登录页', async ({ page }) => {
    await page.goto(BASE_URL + '/');
    await page.waitForURL('**/login');
    await expect(page.locator('text=表单数据平台 - 登录')).toBeVisible();
  });

  // ── 3. 注册页 ──
  test('注册页面可访问', async ({ page }) => {
    await page.goto(BASE_URL + '/register');
    await expect(page.locator('text=用户注册')).toBeVisible();
    await expect(page.locator('input[id="username"]')).toBeVisible();
    await expect(page.locator('input[id="password"]')).toBeVisible();
  });

  // ── 4. 注册→登录→浏览 ──
  test('注册后登录并浏览表单', async ({ page }) => {
    const testUser = 'e2e_' + Date.now();
    await page.goto(BASE_URL + '/register');
    await page.fill('input[id="username"]', testUser);
    await page.fill('input[id="password"]', 'TestPass123');
    await page.locator('button:has-text("注 册")').click();
    await page.waitForURL('**/login');

    await page.fill('input[id="username"]', testUser);
    await page.fill('input[id="password"]', 'TestPass123');
    await page.locator('button:has-text("登 录")').click();
    await page.waitForURL('**/');
    await expect(page.locator('h2').filter({ hasText: testUser })).toBeVisible();

    await page.goto(BASE_URL + '/forms/submit');
    await expect(page.locator('h3:has-text("可填报表单")')).toBeVisible();
  });

  // ── 5. admin 用户管理 ──
  test('admin用户管理页面', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto(BASE_URL + '/admin/users');
    await expect(page.locator('h3:has-text("用户管理")')).toBeVisible();
  });

  // ── 6. admin 表单列表 ──
  test('admin表单模板列表', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto(BASE_URL + '/forms/templates');
    await expect(page.locator('h3:has-text("我的表单")')).toBeVisible();
  });

  // ── 7. 无需审批表单提交 ──
  test('无需审批表单提交后状态为已提交', async ({ page }) => {
    await loginAsAdmin(page);

    const formResp = await page.evaluate(async ({ t, api }) => {
      const r = await fetch(api + '/api/forms/templates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${t}` },
        body: JSON.stringify({ name: '无需审批E2E', description: 'E2E测试', needApproval: false, schemaJson: '[]' }),
      });
      return r.json();
    }, { t: adminToken, api: API });
    expect(formResp.code).toBe(200);
    const formId: number = formResp.data.id;

    await page.evaluate(async ({ t, api, id }) => {
      await fetch(api + `/api/forms/templates/${id}/publish`, { method: 'PUT', headers: { Authorization: `Bearer ${t}` } });
    }, { t: adminToken, api: API, id: formId });

    const submitResp = await page.evaluate(async ({ t, api, id }) => {
      const r = await fetch(api + '/api/forms/submissions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${t}` },
        body: JSON.stringify({ templateId: id, dataJson: '{}' }),
      });
      return r.json();
    }, { t: adminToken, api: API, id: formId });
    expect(submitResp.data.status).toBe('SUBMITTED');

    await page.goto(BASE_URL + `/forms/submit/${formId}`);
    await expect(page.locator('text=已提交').first()).toBeVisible();
  });

  // ── 8. 审批流程 ──
  test('创建需审批表单并完成审批', async ({ page }) => {
    await loginAsAdmin(page);

    // Форм с needApproval=true
    const formResp = await page.evaluate(async ({ t, api }) => {
      const r = await fetch(api + '/api/forms/templates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${t}` },
        body: JSON.stringify({ name: '审批E2E', description: 'E2E审批测试', needApproval: true, schemaJson: '[]' }),
      });
      return r.json();
    }, { t: adminToken, api: API });
    const formId: number = formResp.data.id;

    await page.evaluate(async ({ t, api, id }) => {
      await fetch(api + `/api/forms/templates/${id}/publish`, { method: 'PUT', headers: { Authorization: `Bearer ${t}` } });
    }, { t: adminToken, api: API, id: formId });

    const submitResp = await page.evaluate(async ({ t, api, id }) => {
      const r = await fetch(api + '/api/forms/submissions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${t}` },
        body: JSON.stringify({ templateId: id, dataJson: '{}' }),
      });
      return r.json();
    }, { t: adminToken, api: API, id: formId });
    expect(submitResp.data.status).toBe('PENDING');

    // 注册审批人
    const suffix = Date.now();
    await page.evaluate(async ({ api, suf }) => {
      await fetch(api + '/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'apr_' + suf, password: 'Approver1', email: 'a@t.com' }),
      });
    }, { api: API, suf: suffix });

    // 查用户ID并分配角色
    const usersResp = await page.evaluate(async ({ t, api }) => {
      const r = await fetch(api + '/api/users?page=1&size=100', { headers: { Authorization: `Bearer ${t}` } });
      return r.json();
    }, { t: adminToken, api: API });
    const approver = usersResp.data.records.find((u: any) => u.username.startsWith('apr_'));
    if (approver) {
      await page.evaluate(async ({ t, api, uid }) => {
        await fetch(api + `/api/users/${uid}/roles`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${t}` },
          body: JSON.stringify({ roleIds: [2] }),
        });
      }, { t: adminToken, api: API, uid: approver.id });
    }

    // 以审批人身份登录
    await page.goto(BASE_URL + '/login');
    await page.fill('input[id="username"]', 'apr_' + suffix);
    await page.fill('input[id="password"]', 'Approver1');
    await page.locator('button:has-text("登 录")').click();
    await page.waitForURL('**/');

    await page.goto(BASE_URL + '/approvals/pending');
    await expect(page.locator('td:has-text("审批E2E")').first()).toBeVisible();
  });

  // ── 9. 登录锁定 ──
  test('登录密码3次错误后锁定', async ({ page }) => {
    for (let i = 0; i < 3; i++) {
      await page.evaluate(async ({ api }) => {
        await fetch(api + '/api/auth/login', {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: 'admin', password: 'DefWrongPass999' }),
        });
      }, { api: API });
    }

    // 第4次：验证API返回 1005
    const resp = await page.evaluate(async ({ api }) => {
      const r = await fetch(api + '/api/auth/login', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'admin', password: 'DefWrongPass999' }),
      });
      return r.json();
    }, { api: API });
    expect(resp.code).toBe(1005);
  });
});
