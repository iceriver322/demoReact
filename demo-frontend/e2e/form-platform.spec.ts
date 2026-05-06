import { test, expect } from '@playwright/test';

/**
 * 表单数据平台 E2E 测试
 *
 * 前提：后端 (localhost:8080) 和前端 (localhost:5173) 均已启动
 * 运行：npx playwright test
 */

const BASE_URL = 'http://localhost:5173';

test.describe('表单数据平台 E2E', () => {

  test('管理员登录并进入首页', async ({ page }) => {
    await page.goto(BASE_URL + '/login');

    // 填写登录表单
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin');
    await page.click('button[type="submit"]');

    // 等待跳转到首页
    await page.waitForURL('**/');
    await expect(page.locator('text=欢迎，admin')).toBeVisible();
  });

  test('未登录用户访问首页跳转登录页', async ({ page }) => {
    await page.goto(BASE_URL + '/');

    // 应该被重定向到登录页
    await page.waitForURL('**/login');
    await expect(page.locator('text=表单数据平台 - 登录')).toBeVisible();
  });

  test('注册页面可访问', async ({ page }) => {
    await page.goto(BASE_URL + '/register');

    await expect(page.locator('text=用户注册')).toBeVisible();
    // 检查注册表单元素存在
    await expect(page.locator('input[id="username"]')).toBeVisible();
    await expect(page.locator('input[id="password"]')).toBeVisible();
  });

  test('管理员登录后访问用户管理页面', async ({ page }) => {
    // 登录
    await page.goto(BASE_URL + '/login');
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/');

    // 导航到用户管理页面
    await page.goto(BASE_URL + '/admin/users');
    await page.waitForSelector('table');
    await expect(page.locator('text=用户管理')).toBeVisible();
  });

  test('管理员登录后访问表单模板列表', async ({ page }) => {
    // 登录
    await page.goto(BASE_URL + '/login');
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/');

    // 导航到我的表单
    await page.goto(BASE_URL + '/forms/templates');
    await expect(page.locator('text=我的表单')).toBeVisible();
    await expect(page.locator('text=新建表单')).toBeVisible();
  });

  test('完整用户流程：注册 → 登录 → 浏览表单', async ({ page }) => {
    // 注册新用户
    await page.goto(BASE_URL + '/register');
    const testUser = 'e2e_test_' + Date.now();
    await page.fill('input[id="username"]', testUser);
    await page.fill('input[id="password"]', 'Abc12345');
    await page.click('button[type="submit"]');

    // 注册成功后跳转到登录页
    await page.waitForURL('**/login');

    // 用新用户登录
    await page.fill('input[id="username"]', testUser);
    await page.fill('input[id="password"]', 'Abc12345');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/');

    // 验证登录成功
    await expect(page.locator(`text=欢迎，${testUser}`)).toBeVisible();

    // 浏览可填报表单
    await page.goto(BASE_URL + '/forms/submit');
    await expect(page.locator('text=可填报表单')).toBeVisible();
  });
});
