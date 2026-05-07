# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository

https://github.com/iceriver322/demoReact

## Build & Test Commands

```bash
# Maven path and local repo (required for all Maven commands)
MVN="/Users/fan/software/apache-maven-3.9.9/bin/mvn"
REPO="-Dmaven.repo.local=/Users/fan/software/apache-maven-repo"

# Build all modules (skip tests)
$MVN install -DskipTests $REPO

# Run all backend tests (27 tests, 0 failures)
$MVN test $REPO

# Run single test class
$MVN test -pl demo-user -Dtest="UserServiceTest" $REPO

# Start backend (port 8080, H2 in-memory)
$MVN -f demo-web/pom.xml spring-boot:run $REPO

# Frontend (cd demo-frontend first)
npm install && npm run dev       # Dev server port 5173, proxies /api → :8080
npm run build                    # tsc && vite build
npm run test:e2e                 # Playwright E2E (requires backend + frontend running)
npx tsc --noEmit                 # TypeScript type check
```

## Architecture

Maven multi-module project (Java 17, Spring Boot 3.2.5) with a React 18 frontend.

**Module dependency chain:** `common ← user ← form-engine ← workflow ← web`

| Module | Package | Role |
|--------|---------|------|
| `demo-common` | `com.demoform.common` | BaseEntity, ApiResponse, PageResult, enums (ResultCode/FormStatus/SubmissionStatus), BusinessException, DTOs |
| `demo-user` | `com.demoform.user` | SysUser/SysRole/UserRole entities, MyBatis-Plus mappers, UserService (password policy: 8+ chars, upper+lower+digit, 90-day expiry, bcrypt) |
| `demo-form-engine` | `com.demoform.formengine` | FormTemplate/FormSubmission entities, form CRUD + submit + CSV export services |
| `demo-workflow` | `com.demoform.workflow` | Camunda 7 embedded engine, approval.bpmn, ApprovalService (directly updates submission status) |
| `demo-web` | `com.demoform.web` | Spring Boot entry point (port 8080), 5 REST controllers, JWT auth, SecurityConfig, DataInitializer |
| `demo-frontend` | — | React 18 + Vite 5 + TypeScript + Ant Design 5 + @dnd-kit (drag-and-drop form builder) |

## Test Coverage

| Test Class | Module | Cases | Type |
|-----------|--------|-------|------|
| UserServiceTest | demo-user | 6 | Unit (Mockito) |
| FormTemplateServiceTest | demo-form-engine | 3 | Unit (Mockito) |
| FormSubmissionServiceTest | demo-form-engine | 6 | Unit (Mockito) |
| ApprovalServiceImplTest | demo-workflow | 2 | Unit (Mockito) |
| AuthControllerTest | demo-web | 3 | Integration (@WebMvcTest) |
| UserControllerTest | demo-web | 4 | Integration (@WebMvcTest) |
| ApprovalControllerTest | demo-web | 3 | Integration (@WebMvcTest) |
| form-platform.spec.ts | demo-frontend | 6 | E2E (Playwright) |
| **Total** | | **27 + 6** | |

## Key Design Decisions

- **Auth:** JWT stateless; userId (Long) as SecurityContext principal; roles as `ROLE_` prefix; `@PreAuthorize` on controllers
- **Database:** H2 in-memory for dev (`jdbc:h2:mem:demoform;MODE=MySQL`), MySQL for prod; init scripts in `demo-web/src/main/resources/db/`
- **ORM:** MyBatis-Plus 3.5.6; `@MapperScan({"com.demoform.user.mapper", "com.demoform.formengine.mapper"})`; `@TableLogic` for soft delete
- **Forms:** Schema stored as JSON in `form_template.schema_json`; submissions as JSON in `form_submission.data_json`; drag-and-drop builder serializes `FormField[]` to schema JSON
- **Workflow:** Camunda 7 Community embedded; approval triggered from Controller (not Service, avoiding cyclic dependency); **approve/reject directly updates submission status in Service layer** (not via BPMN Delegate — Delegate not triggered reliably)
- **Password:** `admin/admin` initial login; `DataInitializer` re-encodes password + sets expire=yesterday at startup forcing first-login change
- **Frontend auth:** JWT in localStorage; axios interceptor auto-attaches Bearer token and unwraps `ApiResponse.data`; 401 clears token → redirect /login
- **CSV export:** Bypasses axios interceptor (uses raw axios with `responseType: 'blob'`) since interceptor treats all responses as JSON `{code, message, data}`

## API Routes

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/api/auth/login` | Public | Returns JWT |
| POST | `/api/auth/register` | Public | Default ROLE_USER |
| GET | `/api/auth/me` | Authenticated | Current user info |
| GET | `/api/users` | ROLE_ADMIN | Paginated user list |
| PUT | `/api/users/{id}/roles` | ROLE_ADMIN | Role assignment |
| DELETE | `/api/users/{id}` | ROLE_ADMIN | Soft delete |
| POST | `/api/forms/templates` | Authenticated | Create form |
| GET | `/api/forms/templates` | Authenticated | My forms |
| GET | `/api/forms/templates/published` | Authenticated | Published forms (for submission) |
| GET | `/api/forms/templates/{id}` | Authenticated | Template detail |
| PUT | `/api/forms/templates/{id}` | Authenticated | Update (owner only) |
| DELETE | `/api/forms/templates/{id}` | Authenticated | Delete (owner only) |
| PUT | `/api/forms/templates/{id}/publish` | Authenticated | Publish form |
| PUT | `/api/forms/templates/{id}/disable` | Authenticated | Disable form |
| POST | `/api/forms/submissions` | Authenticated | Submit data (`templateId` + `dataJson` in body) |
| GET | `/api/forms/submissions/my` | Authenticated | My submissions |
| GET | `/api/forms/templates/{id}/submissions` | Authenticated | View submissions (owner only) |
| GET | `/api/forms/submissions/my/template/{templateId}` | Authenticated | My submissions by template |
| GET | `/api/forms/templates/{id}/submissions/export` | Authenticated | Export CSV (owner only, raw blob) |
| GET | `/api/approvals/pending` | ROLE_PRIVILEGED/ADMIN | Pending tasks |
| PUT | `/api/approvals/{submissionId}/approve` | ROLE_PRIVILEGED/ADMIN | Approve |
| PUT | `/api/approvals/{submissionId}/reject` | ROLE_PRIVILEGED/ADMIN | Reject |

Unified response: `{ code: int, message: string, data: T }`

## Known Issues & Fixes Applied

1. **BPMN `historyTimeToLive`** — Camunda 7 enforces this; set `camunda:historyTimeToLive="1"` on `<bpmn:process>`
2. **`@MapperScan` must target exact packages** — `com.demoform` scans Service interfaces as MyBatis mappers; use `{"com.demoform.user.mapper", "com.demoform.formengine.mapper"}`
3. **`-parameters` compiler flag** — Added to parent POM for Spring constructor injection parameter names
4. **UserVO needs `@NoArgsConstructor` + `@AllArgsConstructor`** — `@Builder` alone breaks MyBatis result mapping
5. **`camunda:assignee` → `camunda:candidateGroups`** — `${assignee}` variable unset causes PropertyNotFoundException; use `candidateGroups="privileged"`
6. **`SubmissionRequest` must include `templateId`** — Frontend sends in body, not query param
7. **ApprovalService directly updates submission status** — `ApprovalCompleteDelegate` (JavaDelegate) not reliably triggered by BPMN; approve/reject call `updateSubmissionStatus()` inline
8. **Approval frontend: `variables.submissionId` not `processInstanceId`** — Camunda processInstanceId is UUID; approval API needs numeric submissionId from process variables
9. **CSV export bypasses axios interceptor** — Interceptor parses all responses as JSON `{code,message,data}`; export uses raw axios with `responseType:'blob'`
10. **`@WebMvcTest` + `Authentication auth` controller parameter** — When using `addFilters=false`, the `Authentication` param in controllers is resolved by `PrincipalMethodArgumentResolver` via `request.getUserPrincipal()`, not from `SecurityContextHolder`. Use a custom `RequestPostProcessor` to set it directly: `.with(request -> { request.setUserPrincipal(auth); return request; })`.
