# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Maven path and local repo (required for all Maven commands)
MVN="/Users/fan/software/apache-maven-3.9.9/bin/mvn"
REPO="-Dmaven.repo.local=/Users/fan/software/apache-maven-repo"

# Build all modules (skip tests)
$MVN install -DskipTests $REPO

# Run all backend tests
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
| `demo-workflow` | `com.demoform.workflow` | Camunda 7 embedded engine, approval.bpmn (submit→pending→approve/reject), ApprovalCompleteDelegate |
| `demo-web` | `com.demoform.web` | Spring Boot entry point (port 8080), 5 REST controllers, JWT auth (jjwt 0.12.5), SecurityConfig (stateless), CORS, GlobalExceptionHandler |
| `demo-frontend` | — | React 18 + Vite 5 + TypeScript + Ant Design 5 + @dnd-kit (drag-and-drop form builder) |

## Key Design Decisions

- **Auth:** JWT stateless; userId as SecurityContext principal; roles as `ROLE_` prefix; `@PreAuthorize` on controllers
- **Database:** H2 in-memory for dev (`jdbc:h2:mem:demoform;MODE=MySQL`), MySQL for prod; SQL init scripts in `demo-web/src/main/resources/db/`
- **ORM:** MyBatis-Plus 3.5.6; `@MapperScan` on user and form-engine mapper packages only; `@TableLogic` for soft delete
- **Forms:** Schema stored as JSON string in `form_template.schema_json`; form submissions stored as JSON in `form_submission.data_json`; drag-and-drop builder serializes `FormField[]` to schema
- **Workflow:** Camunda 7 Community embedded; approval flow triggered from `FormSubmissionController.submit()` (not from service layer, avoiding cyclic dependency); `ApprovalCompleteDelegate` updates submission status on process end
- **Password:** `admin/admin` initial login; `DataInitializer` CommandLineRunner re-encodes admin password at startup; password_expire_date set to yesterday to force first-login change
- **Frontend auth:** JWT in localStorage; axios interceptor auto-attaches Bearer token; 401 clears token and redirects to /login

## API Routes Summary

| Prefix | Auth | Purpose |
|--------|------|---------|
| `POST /api/auth/login` | Public | Login, returns JWT |
| `POST /api/auth/register` | Public | Register user (ROLE_USER default) |
| `GET /api/auth/me` | Authenticated | Current user info |
| `GET/PUT/DELETE /api/users/*` | ROLE_ADMIN | User management |
| `POST/GET/PUT/DELETE /api/forms/templates/*` | Authenticated | Form CRUD + publish/disable |
| `POST /api/forms/submissions` | Authenticated | Submit form data (templateId in body) |
| `GET /api/forms/templates/{id}/submissions/export` | Owner | CSV export |
| `GET /api/approvals/pending` | ROLE_PRIVILEGED/ADMIN | Pending approval tasks |
| `PUT /api/approvals/{id}/approve\|reject` | ROLE_PRIVILEGED/ADMIN | Approve/reject |

Unified response: `{ code: int, message: string, data: T }`

## Known Issues & Fixes Applied

1. **BPMN must have `camunda:historyTimeToLive`** — Camunda 7 enforces this; set to `1` (1 day) in approval.bpmn
2. **`@MapperScan` must target exact packages** — `com.demoform` scans too broadly and picks up Service interfaces; use `{"com.demoform.user.mapper", "com.demoform.formengine.mapper"}`
3. **`-parameters` compiler flag required** — Added to parent POM for Spring constructor injection parameter name retention
4. **UserVO needs `@NoArgsConstructor`** — MyBatis result mapping requires public no-arg constructor alongside `@Builder`
5. **Camunda assignee → candidateGroups** — Using `camunda:candidateGroups="privileged"` avoids PropertyNotFoundException for unset `${assignee}` variable
