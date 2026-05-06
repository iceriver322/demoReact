# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: form-platform.spec.ts >> 表单数据平台 E2E >> 管理员登录后访问用户管理页面
- Location: e2e/form-platform.spec.ts:44:3

# Error details

```
Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:5173/login
Call log:
  - navigating to "http://localhost:5173/login", waiting until "load"

```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | 
  3  | /**
  4  |  * 表单数据平台 E2E 测试
  5  |  *
  6  |  * 前提：后端 (localhost:8080) 和前端 (localhost:5173) 均已启动
  7  |  * 运行：npx playwright test
  8  |  */
  9  | 
  10 | const BASE_URL = 'http://localhost:5173';
  11 | 
  12 | test.describe('表单数据平台 E2E', () => {
  13 | 
  14 |   test('管理员登录并进入首页', async ({ page }) => {
  15 |     await page.goto(BASE_URL + '/login');
  16 | 
  17 |     // 填写登录表单
  18 |     await page.fill('input[id="username"]', 'admin');
  19 |     await page.fill('input[id="password"]', 'admin');
  20 |     await page.click('button[type="submit"]');
  21 | 
  22 |     // 等待跳转到首页
  23 |     await page.waitForURL('**/');
  24 |     await expect(page.locator('text=欢迎，admin')).toBeVisible();
  25 |   });
  26 | 
  27 |   test('未登录用户访问首页跳转登录页', async ({ page }) => {
  28 |     await page.goto(BASE_URL + '/');
  29 | 
  30 |     // 应该被重定向到登录页
  31 |     await page.waitForURL('**/login');
  32 |     await expect(page.locator('text=表单数据平台 - 登录')).toBeVisible();
  33 |   });
  34 | 
  35 |   test('注册页面可访问', async ({ page }) => {
  36 |     await page.goto(BASE_URL + '/register');
  37 | 
  38 |     await expect(page.locator('text=用户注册')).toBeVisible();
  39 |     // 检查注册表单元素存在
  40 |     await expect(page.locator('input[id="username"]')).toBeVisible();
  41 |     await expect(page.locator('input[id="password"]')).toBeVisible();
  42 |   });
  43 | 
  44 |   test('管理员登录后访问用户管理页面', async ({ page }) => {
  45 |     // 登录
> 46 |     await page.goto(BASE_URL + '/login');
     |                ^ Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:5173/login
  47 |     await page.fill('input[id="username"]', 'admin');
  48 |     await page.fill('input[id="password"]', 'admin');
  49 |     await page.click('button[type="submit"]');
  50 |     await page.waitForURL('**/');
  51 | 
  52 |     // 导航到用户管理页面
  53 |     await page.goto(BASE_URL + '/admin/users');
  54 |     await page.waitForSelector('table');
  55 |     await expect(page.locator('text=用户管理')).toBeVisible();
  56 |   });
  57 | 
  58 |   test('管理员登录后访问表单模板列表', async ({ page }) => {
  59 |     // 登录
  60 |     await page.goto(BASE_URL + '/login');
  61 |     await page.fill('input[id="username"]', 'admin');
  62 |     await page.fill('input[id="password"]', 'admin');
  63 |     await page.click('button[type="submit"]');
  64 |     await page.waitForURL('**/');
  65 | 
  66 |     // 导航到我的表单
  67 |     await page.goto(BASE_URL + '/forms/templates');
  68 |     await expect(page.locator('text=我的表单')).toBeVisible();
  69 |     await expect(page.locator('text=新建表单')).toBeVisible();
  70 |   });
  71 | 
  72 |   test('完整用户流程：注册 → 登录 → 浏览表单', async ({ page }) => {
  73 |     // 注册新用户
  74 |     await page.goto(BASE_URL + '/register');
  75 |     const testUser = 'e2e_test_' + Date.now();
  76 |     await page.fill('input[id="username"]', testUser);
  77 |     await page.fill('input[id="password"]', 'Abc12345');
  78 |     await page.click('button[type="submit"]');
  79 | 
  80 |     // 注册成功后跳转到登录页
  81 |     await page.waitForURL('**/login');
  82 | 
  83 |     // 用新用户登录
  84 |     await page.fill('input[id="username"]', testUser);
  85 |     await page.fill('input[id="password"]', 'Abc12345');
  86 |     await page.click('button[type="submit"]');
  87 |     await page.waitForURL('**/');
  88 | 
  89 |     // 验证登录成功
  90 |     await expect(page.locator(`text=欢迎，${testUser}`)).toBeVisible();
  91 | 
  92 |     // 浏览可填报表单
  93 |     await page.goto(BASE_URL + '/forms/submit');
  94 |     await expect(page.locator('text=可填报表单')).toBeVisible();
  95 |   });
  96 | });
  97 | 
```