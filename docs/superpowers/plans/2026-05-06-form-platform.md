# 表单数据平台 - 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建前后端分离的表单数据平台，支持用户管理、可视化表单设计、数据填报与审批。

**Architecture:** Maven 多模块后端（common → user → form-engine → workflow → web），React 前端（Vite + Ant Design），Spring Security JWT 认证，Camunda 7 嵌入式流程引擎。

**Tech Stack:** Java 17, Spring Boot 3.x, MyBatis-Plus, Camunda 7, H2/MySQL, React 18, TypeScript, Ant Design 5, @dnd-kit/core

---

## Phase 1: 项目脚手架

### Task 1: 创建 Maven 父 POM 和模块目录结构

**Files:**
- Create: `pom.xml`
- Create: `demo-common/pom.xml`
- Create: `demo-user/pom.xml`
- Create: `demo-form-engine/pom.xml`
- Create: `demo-workflow/pom.xml`
- Create: `demo-web/pom.xml`

- [ ] **Step 1: 创建父 POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.demoform</groupId>
    <artifactId>demo-form</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>demo-form</name>
    <description>表单数据平台 - 父项目</description>

    <modules>
        <module>demo-common</module>
        <module>demo-user</module>
        <module>demo-form-engine</module>
        <module>demo-workflow</module>
        <module>demo-web</module>
    </modules>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.6</mybatis-plus.version>
        <camunda.version>7.21.0</camunda.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- 内部模块 -->
            <dependency>
                <groupId>com.demoform</groupId>
                <artifactId>demo-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.demoform</groupId>
                <artifactId>demo-user</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.demoform</groupId>
                <artifactId>demo-form-engine</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.demoform</groupId>
                <artifactId>demo-workflow</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- MyBatis-Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>

            <!-- Camunda 7 -->
            <dependency>
                <groupId>org.camunda.bpm.springboot</groupId>
                <artifactId>camunda-bpm-spring-boot-starter</artifactId>
                <version>${camunda.version}</version>
            </dependency>

            <!-- JWT -->
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jjwt.version}</version>
                <scope>runtime</scope>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jjwt.version}</version>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- 所有模块共用：Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- 所有模块共用：测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建模块目录和 pom.xml**

创建目录结构：
```bash
mkdir -p demo-common/src/main/java/com/demoform/common/{entity,dto,enums,exception,util}
mkdir -p demo-common/src/test/java/com/demoform/common
mkdir -p demo-user/src/main/java/com/demoform/user/{entity,mapper,service/impl}
mkdir -p demo-user/src/main/resources/mapper
mkdir -p demo-user/src/test/java/com/demoform/user
mkdir -p demo-form-engine/src/main/java/com/demoform/formengine/{entity,mapper,service/impl}
mkdir -p demo-form-engine/src/main/resources/mapper
mkdir -p demo-form-engine/src/test/java/com/demoform/formengine
mkdir -p demo-workflow/src/main/java/com/demoform/workflow/{service/impl,delegate,config}
mkdir -p demo-workflow/src/main/resources/processes
mkdir -p demo-workflow/src/test/java/com/demoform/workflow
mkdir -p demo-web/src/main/java/com/demoform/web/{config,filter,controller,handler}
mkdir -p demo-web/src/main/resources
mkdir -p demo-web/src/test/java/com/demoform/web
```

- [ ] **Step 3: 创建 demo-common/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.demoform</groupId>
        <artifactId>demo-form</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>demo-common</artifactId>
    <name>demo-common</name>
    <description>公共模块：基础实体、DTO、枚举、异常、工具类</description>
</project>
```

- [ ] **Step 4: 创建 demo-user/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.demoform</groupId>
        <artifactId>demo-form</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>demo-user</artifactId>
    <name>demo-user</name>
    <description>用户模块：用户实体、Mapper、Service</description>
    <dependencies>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 5: 创建 demo-form-engine/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.demoform</groupId>
        <artifactId>demo-form</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>demo-form-engine</artifactId>
    <name>demo-form-engine</name>
    <description>表单模块：表单定义、数据填报</description>
    <dependencies>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-user</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 6: 创建 demo-workflow/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.demoform</groupId>
        <artifactId>demo-form</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>demo-workflow</artifactId>
    <name>demo-workflow</name>
    <description>流程引擎模块：Camunda 7 审批流程</description>
    <dependencies>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-form-engine</artifactId>
        </dependency>
        <dependency>
            <groupId>org.camunda.bpm.springboot</groupId>
            <artifactId>camunda-bpm-spring-boot-starter</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 7: 创建 demo-web/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.demoform</groupId>
        <artifactId>demo-form</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>demo-web</artifactId>
    <name>demo-web</name>
    <description>主启动模块：Controller、安全配置、全局异常处理</description>
    <dependencies>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-user</artifactId>
        </dependency>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-form-engine</artifactId>
        </dependency>
        <dependency>
            <groupId>com.demoform</groupId>
            <artifactId>demo-workflow</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>false</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 8: 验证构建**

```bash
cd /Users/fan/gitspace/demoReact && mvn validate
```

Expected: BUILD SUCCESS

- [ ] **Step 9: 提交代码**

```bash
git add pom.xml demo-*/pom.xml
git commit -m "chore: 初始化 Maven 多模块项目结构

创建父 POM 及 5 个子模块：common、user、form-engine、workflow、web。"
```

---

### Task 2: Spring Boot 启动类与基础配置

**Files:**
- Create: `demo-web/src/main/java/com/demoform/web/FormApplication.java`
- Create: `demo-web/src/main/resources/application.yml`
- Create: `demo-web/src/main/resources/application-dev.yml`
- Create: `demo-web/src/main/resources/application-prod.yml`

- [ ] **Step 1: 创建启动类**

```java
package com.demoform.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 表单数据平台 - 应用启动入口
 */
@SpringBootApplication(scanBasePackages = "com.demoform")
public class FormApplication {
    public static void main(String[] args) {
        SpringApplication.run(FormApplication.class, args);
    }
}
```

- [ ] **Step 2: 创建 application.yml 主配置文件**

```yaml
# 默认激活开发环境
spring:
  profiles:
    active: dev
  application:
    name: demo-form
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: non_null

# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.demoform
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true

# 服务器端口
server:
  port: 8080

# JWT 配置
jwt:
  secret: YTJkM2Y0ZTUtYjY3OC00ZjVhLTk4MjEtZjNjM2Q1N2U4YTAx
  expiration: 86400000
```

- [ ] **Step 3: 创建 application-dev.yml 开发环境配置**

```yaml
# 开发环境 - H2 数据库
spring:
  datasource:
    url: jdbc:h2:mem:demoform;DB_CLOSE_DELAY=-1;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  sql:
    init:
      mode: always
      schema-locations: classpath:db/schema-h2.sql
      data-locations: classpath:db/data-h2.sql

# Camunda 配置（开发环境）
camunda:
  bpm:
    admin-user:
      id: admin
      password: admin
    database:
      type: h2
    auto-deployment-enabled: true
```

- [ ] **Step 4: 创建 application-prod.yml 生产环境配置**

```yaml
# 生产环境 - MySQL 数据库
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demoform?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=GMT%2B8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
  sql:
    init:
      mode: never

# Camunda 配置（生产环境）
camunda:
  bpm:
    admin-user:
      id: ${CAMUNDA_ADMIN:admin}
      password: ${CAMUNDA_PASSWORD:admin}
    database:
      type: mysql
```

- [ ] **Step 5: 创建数据库初始化 SQL**

`demo-web/src/main/resources/db/schema-h2.sql`:
```sql
-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（bcrypt加密）',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    password_expire_date DATE COMMENT '密码过期日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除'
);

-- 用户角色中间表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
);

-- 表单模板表
CREATE TABLE IF NOT EXISTS form_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '表单名称',
    description VARCHAR(500) COMMENT '表单描述',
    owner_id BIGINT NOT NULL COMMENT '创建者ID',
    schema_json CLOB COMMENT '表单字段定义JSON',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿 PUBLISHED-已发布 DISABLED-已停用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 表单填报数据表
CREATE TABLE IF NOT EXISTS form_submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL COMMENT '表单模板ID',
    submitter_id BIGINT NOT NULL COMMENT '提交者ID',
    data_json CLOB COMMENT '填报数据JSON',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审批 APPROVED-已通过 REJECTED-已驳回',
    approver_id BIGINT COMMENT '审批人ID',
    approved_at TIMESTAMP COMMENT '审批时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);
```

`demo-web/src/main/resources/db/data-h2.sql`:
```sql
-- 初始化角色
MERGE INTO sys_role (id, name, code) VALUES (1, '管理员', 'ROLE_ADMIN');
MERGE INTO sys_role (id, name, code) VALUES (2, '特权用户', 'ROLE_PRIVILEGED');
MERGE INTO sys_role (id, name, code) VALUES (3, '普通用户', 'ROLE_USER');

-- 初始化管理员用户（密码：admin，bcrypt 加密）
-- 注意：实际 bcrypt 值需通过 PasswordEncoder 生成，此处为占位
MERGE INTO sys_user (id, username, password, email, status, password_expire_date)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@demoform.com', 1, DATEADD('DAY', -1, CURRENT_DATE));

-- 分配管理员角色
MERGE INTO sys_user_role (user_id, role_id) VALUES (1, 1);
```

- [ ] **Step 6: 验证项目能启动**

```bash
cd /Users/fan/gitspace/demoReact && mvn clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add demo-web/src/main/java/com/demoform/web/FormApplication.java \
        demo-web/src/main/resources/application*.yml \
        demo-web/src/main/resources/db/
git commit -m "chore: 添加 Spring Boot 启动类和基础配置

包含开发(H2)和生产(MySQL)双环境配置、数据库初始化脚本。"
```

---

### Task 3: 公共模块（Common）

**Files:**
- Create: `demo-common/src/main/java/com/demoform/common/entity/BaseEntity.java`
- Create: `demo-common/src/main/java/com/demoform/common/dto/ApiResponse.java`
- Create: `demo-common/src/main/java/com/demoform/common/dto/PageResult.java`
- Create: `demo-common/src/main/java/com/demoform/common/dto/LoginRequest.java`
- Create: `demo-common/src/main/java/com/demoform/common/dto/LoginResponse.java`
- Create: `demo-common/src/main/java/com/demoform/common/dto/RegisterRequest.java`
- Create: `demo-common/src/main/java/com/demoform/common/enums/ResultCode.java`
- Create: `demo-common/src/main/java/com/demoform/common/enums/FormStatus.java`
- Create: `demo-common/src/main/java/com/demoform/common/enums/SubmissionStatus.java`
- Create: `demo-common/src/main/java/com/demoform/common/exception/BusinessException.java`

- [ ] **Step 1: 创建基础实体基类**

```java
package com.demoform.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实体基类 —— 提供通用字段（id、创建时间、更新时间、逻辑删除标记）
 */
@Data
public abstract class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: 创建统一响应 DTO**

```java
package com.demoform.common.dto;

import com.demoform.common.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    /** 操作成功 */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /** 操作失败 */
    public static <T> ApiResponse<T> fail(ResultCode resultCode) {
        return new ApiResponse<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

- [ ] **Step 3: 创建分页结果 DTO**

```java
package com.demoform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * 分页查询结果
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private long total;
    private int page;
    private int size;
    private List<T> records;

    public static <T> PageResult<T> of(long total, int page, int size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }
}
```

- [ ] **Step 4: 创建请求/响应 DTO**

```java
package com.demoform.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

```java
package com.demoform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 —— 返回 JWT Token 和用户基本信息
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private java.util.List<String> roles;
}
```

```java
package com.demoform.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度8-100位")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

- [ ] **Step 5: 创建枚举类**

```java
package com.demoform.common.enums;

import lombok.Getter;

/**
 * 统一返回码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_FAILED(422, "参数校验失败"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码
    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_WRONG(1003, "密码错误"),
    PASSWORD_EXPIRED(1004, "密码已过期，请修改密码"),
    ACCOUNT_LOCKED(1005, "账户已被锁定，请30分钟后重试"),
    ACCOUNT_DISABLED(1006, "账户已被禁用"),
    PASSWORD_WEAK(1007, "密码强度不足，至少8位含大小写字母和数字"),
    ROLE_NOT_FOUND(1008, "角色不存在"),

    FORM_NOT_FOUND(2001, "表单不存在"),
    FORM_ALREADY_PUBLISHED(2002, "表单已发布"),
    FORM_NOT_PUBLISHED(2003, "表单未发布"),
    SUBMISSION_NOT_FOUND(2004, "填报数据不存在"),
    ALREADY_APPROVED(2005, "数据已审批"),
    NOT_FORM_OWNER(2006, "非表单所有者"),

    APPROVAL_TASK_NOT_FOUND(3001, "审批任务不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

```java
package com.demoform.common.enums;

/**
 * 表单模板状态
 */
public enum FormStatus {
    /** 草稿 */
    DRAFT,
    /** 已发布 */
    PUBLISHED,
    /** 已停用 */
    DISABLED
}
```

```java
package com.demoform.common.enums;

/**
 * 填报数据审批状态
 */
public enum SubmissionStatus {
    /** 待审批 */
    PENDING,
    /** 已通过 */
    APPROVED,
    /** 已驳回 */
    REJECTED
}
```

- [ ] **Step 6: 创建业务异常类**

```java
package com.demoform.common.exception;

import com.demoform.common.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常 —— 统一由全局异常处理器捕获并转换为 ApiResponse
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **Step 7: 验证编译**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-common
```

Expected: BUILD SUCCESS

- [ ] **Step 8: 提交**

```bash
git add demo-common/
git commit -m "feat: 添加公共模块 —— 实体基类、DTO、枚举、业务异常"
```

---

## Phase 2: 用户与认证

### Task 4: 用户与角色实体类

**Files:**
- Create: `demo-user/src/main/java/com/demoform/user/entity/SysUser.java`
- Create: `demo-user/src/main/java/com/demoform/user/entity/SysRole.java`
- Create: `demo-user/src/main/java/com/demoform/user/entity/UserRole.java`
- Create: `demo-user/src/main/java/com/demoform/user/dto/UserVO.java`
- Create: `demo-user/src/main/java/com/demoform/user/dto/UserUpdateRequest.java`
- Create: `demo-user/src/main/java/com/demoform/user/dto/ChangePasswordRequest.java`
- Create: `demo-user/src/main/java/com/demoform/user/dto/RoleAssignRequest.java`

- [ ] **Step 1: 创建用户实体**

```java
package com.demoform.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.demoform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

/**
 * 系统用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 用户名 */
    private String username;

    /** 密码（bcrypt 加密存储） */
    private String password;

    /** 邮箱 */
    private String email;

    /** 状态：1-正常 0-禁用 */
    private Integer status;

    /** 密码过期日期 */
    private LocalDate passwordExpireDate;
}
```

- [ ] **Step 2: 创建角色实体**

```java
package com.demoform.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统角色实体
 */
@Data
@TableName("sys_role")
public class SysRole {

    private Long id;
    /** 角色名称 */
    private String name;
    /** 角色编码：ROLE_ADMIN / ROLE_PRIVILEGED / ROLE_USER */
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 创建用户角色中间表实体**

```java
package com.demoform.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-角色关联
 */
@Data
@TableName("sys_user_role")
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
}
```

- [ ] **Step 4: 创建 VO 和请求 DTO**

```java
package com.demoform.user.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象（脱敏返回，不含密码）
 */
@Data
@Builder
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private Integer status;
    private LocalDate passwordExpireDate;
    private List<String> roles;
    private LocalDateTime createdAt;
}
```

```java
package com.demoform.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员编辑用户请求
 */
@Data
public class UserUpdateRequest {
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;
    private String email;
    private Integer status;
}
```

```java
package com.demoform.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求 —— 用于首次登录强制改密和自主修改
 */
@Data
public class ChangePasswordRequest {
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度8-100位")
    private String newPassword;
}
```

```java
package com.demoform.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 角色分配请求
 */
@Data
public class RoleAssignRequest {
    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}
```

- [ ] **Step 5: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-user
```

- [ ] **Step 6: 提交**

```bash
git add demo-user/src/main/java/com/demoform/user/entity/ \
        demo-user/src/main/java/com/demoform/user/dto/
git commit -m "feat: 添加用户与角色实体类及 DTO"
```

---

### Task 5: 用户和角色 Mapper

**Files:**
- Create: `demo-user/src/main/java/com/demoform/user/mapper/UserMapper.java`
- Create: `demo-user/src/main/java/com/demoform/user/mapper/RoleMapper.java`
- Create: `demo-user/src/main/java/com/demoform/user/mapper/UserRoleMapper.java`
- Create: `demo-user/src/main/resources/mapper/UserMapper.xml`
- Create: `demo-user/src/main/resources/mapper/RoleMapper.xml`

- [ ] **Step 1: 创建 UserMapper 接口**

```java
package com.demoform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.user.dto.UserVO;
import com.demoform.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户（含已逻辑删除的记录）
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 分页查询用户列表（含角色信息）
     */
    IPage<UserVO> selectUserPage(Page<UserVO> page, @Param("username") String username);

    /**
     * 根据ID查询用户详情（含角色）
     */
    UserVO selectUserDetail(@Param("userId") Long userId);

    /**
     * 查询用户的角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 更新密码过期日期
     */
    int updatePasswordExpireDate(@Param("userId") Long userId,
                                  @Param("expireDate") java.time.LocalDate expireDate);
}
```

- [ ] **Step 2: 创建 RoleMapper 接口**

```java
package com.demoform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demoform.user.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据角色编码查询角色
     */
    SysRole selectByCode(@Param("code") String code);
}
```

- [ ] **Step 3: 创建 UserRoleMapper 接口**

```java
package com.demoform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demoform.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 批量插入用户角色关联
     */
    int insertBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 删除用户的所有角色
     */
    int deleteByUserId(@Param("userId") Long userId);
}
```

- [ ] **Step 4: 创建 UserMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.demoform.user.mapper.UserMapper">

    <!-- 根据用户名查询（忽略逻辑删除） -->
    <select id="selectByUsername" resultType="com.demoform.user.entity.SysUser">
        SELECT id, username, password, email, status, password_expire_date,
               created_at, updated_at, deleted
        FROM sys_user
        WHERE username = #{username}
    </select>

    <!-- 分页查询用户列表 -->
    <select id="selectUserPage" resultType="com.demoform.user.dto.UserVO">
        SELECT u.id, u.username, u.email, u.status, u.password_expire_date,
               u.created_at
        FROM sys_user u
        WHERE u.deleted = 0
        <if test="username != null and username != ''">
            AND u.username LIKE CONCAT('%', #{username}, '%')
        </if>
        ORDER BY u.created_at DESC
    </select>

    <!-- 查询用户详情 -->
    <select id="selectUserDetail" resultType="com.demoform.user.dto.UserVO">
        SELECT u.id, u.username, u.email, u.status, u.password_expire_date, u.created_at
        FROM sys_user u
        WHERE u.id = #{userId} AND u.deleted = 0
    </select>

    <!-- 查询用户角色编码列表 -->
    <select id="selectRoleCodesByUserId" resultType="java.lang.String">
        SELECT r.code
        FROM sys_user_role ur
        INNER JOIN sys_role r ON ur.role_id = r.id
        WHERE ur.user_id = #{userId}
    </select>

    <!-- 更新密码过期日期 -->
    <update id="updatePasswordExpireDate">
        UPDATE sys_user
        SET password_expire_date = #{expireDate}, updated_at = CURRENT_TIMESTAMP
        WHERE id = #{userId}
    </update>
</mapper>
```

- [ ] **Step 5: 创建 RoleMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.demoform.user.mapper.RoleMapper">

    <select id="selectByCode" resultType="com.demoform.user.entity.SysRole">
        SELECT id, name, code, created_at, updated_at
        FROM sys_role
        WHERE code = #{code}
    </select>
</mapper>
```

- [ ] **Step 6: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-user
```

- [ ] **Step 7: 提交**

```bash
git add demo-user/src/main/java/com/demoform/user/mapper/ \
        demo-user/src/main/resources/mapper/
git commit -m "feat: 添加用户和角色 MyBatis Mapper 接口及 XML"
```

---

### Task 6: UserService 实现

**Files:**
- Create: `demo-user/src/main/java/com/demoform/user/service/UserService.java`
- Create: `demo-user/src/main/java/com/demoform/user/service/impl/UserServiceImpl.java`
- Create: `demo-user/src/main/java/com/demoform/user/service/RoleService.java`
- Create: `demo-user/src/main/java/com/demoform/user/service/impl/RoleServiceImpl.java`

- [ ] **Step 1: 创建 UserService 接口**

```java
package com.demoform.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.demoform.common.dto.PageResult;
import com.demoform.user.dto.*;
import com.demoform.user.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /** 根据用户名查询用户 */
    SysUser findByUsername(String username);

    /** 根据ID查询用户 */
    SysUser findById(Long userId);

    /** 用户注册 */
    void register(RegisterRequest request);

    /** 分页查询用户列表 */
    PageResult<UserVO> listUsers(int page, int size, String username);

    /** 查询用户详情 */
    UserVO getUserDetail(Long userId);

    /** 编辑用户 */
    void updateUser(Long userId, UserUpdateRequest request);

    /** 删除用户（逻辑删除） */
    void deleteUser(Long userId);

    /** 修改密码 */
    void changePassword(Long userId, ChangePasswordRequest request);

    /** 分配角色 */
    void assignRoles(Long userId, RoleAssignRequest request);

    /** 查询用户角色编码 */
    List<String> getUserRoleCodes(Long userId);
}
```

- [ ] **Step 2: 创建 RoleService 接口**

```java
package com.demoform.user.service;

import com.demoform.user.entity.SysRole;

/**
 * 角色服务接口
 */
public interface RoleService {

    /** 根据角色编码查询角色 */
    SysRole findByCode(String code);
}
```

- [ ] **Step 3: 创建 UserServiceImpl**

```java
package com.demoform.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.common.dto.PageResult;
import com.demoform.common.dto.RegisterRequest;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.user.dto.*;
import com.demoform.user.entity.SysUser;
import com.demoform.user.entity.UserRole;
import com.demoform.user.mapper.UserMapper;
import com.demoform.user.mapper.UserRoleMapper;
import com.demoform.user.service.RoleService;
import com.demoform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public SysUser findById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        SysUser existing = userMapper.selectByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        // 校验密码强度
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new BusinessException(ResultCode.PASSWORD_WEAK);
        }
        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus(1);
        // 90 天密码过期
        user.setPasswordExpireDate(LocalDate.now().plusDays(90));
        userMapper.insert(user);
        // 分配普通用户角色
        UserRole ur = new UserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(roleService.findByCode("ROLE_USER").getId());
        userRoleMapper.insert(ur);
    }

    @Override
    public PageResult<UserVO> listUsers(int page, int size, String username) {
        Page<UserVO> pageParam = new Page<>(page, size);
        IPage<UserVO> result = userMapper.selectUserPage(pageParam, username);
        // 填充角色
        for (UserVO vo : result.getRecords()) {
            vo.setRoles(userMapper.selectRoleCodesByUserId(vo.getId()));
        }
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public UserVO getUserDetail(Long userId) {
        UserVO vo = userMapper.selectUserDetail(userId);
        if (vo == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        vo.setRoles(userMapper.selectRoleCodesByUserId(userId));
        return vo;
    }

    @Override
    @Transactional
    public void updateUser(Long userId, UserUpdateRequest request) {
        SysUser user = findById(userId);
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        findById(userId); // 确保存在
        userMapper.deleteById(userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = findById(userId);
        // 校验原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_WRONG);
        }
        // 校验新密码强度
        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new BusinessException(ResultCode.PASSWORD_WEAK);
        }
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordExpireDate(LocalDate.now().plusDays(90));
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, UserAssignRolesRequest request) {
        findById(userId);
        userRoleMapper.deleteByUserId(userId);
        userRoleMapper.insertBatch(userId, request.getRoleIds());
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }
}
```

- [ ] **Step 4: 创建 RoleServiceImpl**

```java
package com.demoform.user.service.impl;

import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.user.entity.SysRole;
import com.demoform.user.mapper.RoleMapper;
import com.demoform.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public SysRole findByCode(String code) {
        SysRole role = roleMapper.selectByCode(code);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        return role;
    }
}
```

- [ ] **Step 5: 修复 DTO 引用 —— 创建 RoleAssignRequest 别名**

```java
package com.demoform.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 角色分配请求 DTO —— 与 Controller 配合使用
 */
@Data
public class UserAssignRolesRequest {
    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}
```

等待 — Step 5 中 UserServiceImpl 引用了 `UserAssignRolesRequest`，但之前定义了 `RoleAssignRequest`。保持一致性：统一使用 `RoleAssignRequest` 并修正 Service 实现中的引用为 `RoleAssignRequest`。

- [ ] **Step 5（修正）: 更新 UserServiceImpl assignRoles 方法签名**

修改 `UserServiceImpl.java` 中 `assignRoles` 方法参数为 `RoleAssignRequest`：

```java
@Override
@Transactional
public void assignRoles(Long userId, RoleAssignRequest request) {
    findById(userId);
    userRoleMapper.deleteByUserId(userId);
    userRoleMapper.insertBatch(userId, request.getRoleIds());
}
```

- [ ] **Step 6: 创建 UserRoleMapper.xml（批量插入/删除）**

`demo-user/src/main/resources/mapper/UserRoleMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.demoform.user.mapper.UserRoleMapper">

    <insert id="insertBatch">
        INSERT INTO sys_user_role (user_id, role_id) VALUES
        <foreach collection="roleIds" item="roleId" separator=",">
            (#{userId}, #{roleId})
        </foreach>
    </insert>

    <delete id="deleteByUserId">
        DELETE FROM sys_user_role WHERE user_id = #{userId}
    </delete>
</mapper>
```

- [ ] **Step 7: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-user
```

- [ ] **Step 8: 提交**

```bash
git add demo-user/
git commit -m "feat: 实现用户和角色 Service

包含注册(密码强度校验/bcrypt加密/90天过期)、CRUD、分页查询、角色分配。"
```

---

### Task 7: JWT 工具类与认证过滤器

**Files:**
- Create: `demo-web/src/main/java/com/demoform/web/util/JwtUtil.java`
- Create: `demo-web/src/main/java/com/demoform/web/filter/JwtAuthFilter.java`
- Create: `demo-web/src/main/java/com/demoform/web/service/UserDetailsServiceImpl.java`

- [ ] **Step 1: 创建 JwtUtil**

```java
package com.demoform.web.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类 —— 生成和解析 Token
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    /** 生成 JWT Token */
    public String generateToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    /** 从 Token 中解析 claims */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /** 从 Token 中获取用户ID */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /** 从 Token 中获取用户名 */
    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }
}
```

- [ ] **Step 2: 创建 UserDetailsServiceImpl**

```java
package com.demoform.web.service;

import com.demoform.user.entity.SysUser;
import com.demoform.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetailsService 实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.selectByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(sysUser.getId());
        List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                sysUser.getStatus() == 1,
                true, true, true,
                authorities
        );
    }
}
```

- [ ] **Step 3: 创建 JwtAuthFilter**

```java
package com.demoform.web.filter;

import com.demoform.web.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器 —— 从请求头中解析 Token，设置 SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            @SuppressWarnings("unchecked")
            List<String> roles = jwtUtil.parseToken(token).get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, username, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-web
```

- [ ] **Step 5: 提交**

```bash
git add demo-web/src/main/java/com/demoform/web/util/ \
        demo-web/src/main/java/com/demoform/web/filter/ \
        demo-web/src/main/java/com/demoform/web/service/
git commit -m "feat: JWT 工具类与认证过滤器

实现 Token 生成/解析/验证、Spring Security UserDetailsService、JWT 请求过滤器。"
```

---

### Task 8: Spring Security 配置

**Files:**
- Create: `demo-web/src/main/java/com/demoform/web/config/SecurityConfig.java`
- Create: `demo-web/src/main/java/com/demoform/web/config/CorsConfig.java`
- Create: `demo-web/src/main/java/com/demoform/web/config/MyBatisPlusConfig.java`

- [ ] **Step 1: 创建 SecurityConfig**

```java
package com.demoform.web.config;

import com.demoform.web.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // H2 控制台（开发环境）
                .requestMatchers("/h2-console/**").permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            // H2 控制台需要允许 frame
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))
            // JWT 过滤器在 UsernamePasswordAuthenticationFilter 之前
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

- [ ] **Step 2: 创建 CorsConfig**

```java
package com.demoform.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域配置 —— 允许前端开发服务器访问
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 3: 创建 MyBatisPlusConfig（分页插件 + 自动填充）**

```java
package com.demoform.web.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置 —— 分页插件、自动填充
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-web
```

- [ ] **Step 5: 提交**

```bash
git add demo-web/src/main/java/com/demoform/web/config/
git commit -m "feat: Spring Security JWT 配置 + CORS + MyBatis-Plus 分页/自动填充"
```

---

### Task 9: AuthController 实现

**Files:**
- Create: `demo-web/src/main/java/com/demoform/web/controller/AuthController.java`

- [ ] **Step 1: 创建 AuthController**

```java
package com.demoform.web.controller;

import com.demoform.common.dto.*;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.user.entity.SysUser;
import com.demoform.user.service.UserService;
import com.demoform.web.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 认证控制器 —— 注册、登录、登出
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /** 用户注册 */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success();
    }

    /** 用户登录 */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Spring Security 认证
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        // 查询用户
        SysUser user = userService.findByUsername(request.getUsername());
        // 检查账户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        // 生成 Token
        List<String> roles = userService.getUserRoleCodes(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);
        // 检查密码是否过期
        boolean passwordExpired = user.getPasswordExpireDate() != null
                && user.getPasswordExpireDate().isBefore(LocalDate.now());
        LoginResponse resp = new LoginResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), roles);
        return ApiResponse.success(resp);
    }

    /** 获取当前用户信息 */
    @GetMapping("/me")
    public ApiResponse<?> me() {
        // 从 SecurityContext 中获取当前用户ID
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(userService.getUserDetail(userId));
    }
}
```

- [ ] **Step 2: 编译验证 + 修正编译错误**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-web
```

注意：如果 `LoginResponse` 构造参数不匹配，可能需要调整。当前 `LoginResponse` 为 `@AllArgsConstructor`，参数顺序：`token, userId, username, email, roles`。

- [ ] **Step 3: 提交**

```bash
git add demo-web/src/main/java/com/demoform/web/controller/AuthController.java
git commit -m "feat: 实现认证控制器 —— 注册、登录(返回JWT)、获取当前用户"
```

---

### Task 10: UserController 实现

**Files:**
- Create: `demo-web/src/main/java/com/demoform/web/controller/UserController.java`

- [ ] **Step 1: 创建 UserController**

```java
package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.dto.PageResult;
import com.demoform.user.dto.*;
import com.demoform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器 —— 管理员操作
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 用户列表（管理员） */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username) {
        return ApiResponse.success(userService.listUsers(page, size, username));
    }

    /** 用户详情 */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserVO> detail(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserDetail(id));
    }

    /** 编辑用户 */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id,
                                     @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success();
    }

    /** 删除用户 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    /** 分配角色 */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> assignRoles(@PathVariable Long id,
                                          @Valid @RequestBody RoleAssignRequest request) {
        userService.assignRoles(id, request);
        return ApiResponse.success();
    }

    /** 修改密码（当前登录用户） */
    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                             Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userService.changePassword(userId, request);
        return ApiResponse.success();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-web
```

- [ ] **Step 3: 提交**

```bash
git add demo-web/src/main/java/com/demoform/web/controller/UserController.java
git commit -m "feat: 实现用户管理控制器 —— 分页查询/详情/编辑/删除/角色分配/修改密码"
```

---

## Phase 3: 表单引擎

### Task 11: 表单实体与 Mapper

**Files:**
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/entity/FormTemplate.java`
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/entity/FormSubmission.java`
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/mapper/FormTemplateMapper.java`
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/mapper/FormSubmissionMapper.java`
- Create: `demo-form-engine/src/main/resources/mapper/FormTemplateMapper.xml`
- Create: `demo-form-engine/src/main/resources/mapper/FormSubmissionMapper.xml`

- [ ] **Step 1: 创建 FormTemplate 实体**

```java
package com.demoform.formengine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demoform.common.entity.BaseEntity;
import com.demoform.common.enums.FormStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表单模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_template")
public class FormTemplate extends BaseEntity {

    /** 表单名称 */
    private String name;

    /** 表单描述 */
    private String description;

    /** 创建者ID */
    private Long ownerId;

    /** 表单字段定义 JSON（schema_json 映射为 schemaJson） */
    @TableField("schema_json")
    private String schemaJson;

    /** 状态：DRAFT / PUBLISHED / DISABLED */
    private String status;
}
```

- [ ] **Step 2: 创建 FormSubmission 实体**

```java
package com.demoform.formengine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demoform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 表单填报数据实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_submission")
public class FormSubmission extends BaseEntity {

    /** 表单模板ID */
    private Long templateId;

    /** 提交者ID */
    private Long submitterId;

    /** 填报数据 JSON */
    @TableField("data_json")
    private String dataJson;

    /** 状态：PENDING / APPROVED / REJECTED */
    private String status;

    /** 审批人ID */
    private Long approverId;

    /** 审批时间 */
    private LocalDateTime approvedAt;
}
```

- [ ] **Step 3: 创建 FormTemplateMapper**

```java
package com.demoform.formengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.formengine.entity.FormTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 表单模板 Mapper
 */
@Mapper
public interface FormTemplateMapper extends BaseMapper<FormTemplate> {

    /** 分页查询我的表单 */
    IPage<FormTemplate> selectMyTemplates(Page<FormTemplate> page, @Param("ownerId") Long ownerId);

    /** 查询已发布的表单列表（供用户填报） */
    IPage<FormTemplate> selectPublishedTemplates(Page<FormTemplate> page);
}
```

- [ ] **Step 4: 创建 FormSubmissionMapper**

```java
package com.demoform.formengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.formengine.entity.FormSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 表单填报数据 Mapper
 */
@Mapper
public interface FormSubmissionMapper extends BaseMapper<FormSubmission> {

    /** 分页查询指定模板的填报数据 */
    IPage<FormSubmission> selectByTemplateId(Page<FormSubmission> page,
                                              @Param("templateId") Long templateId);

    /** 分页查询我的填报记录 */
    IPage<FormSubmission> selectMySubmissions(Page<FormSubmission> page,
                                               @Param("submitterId") Long submitterId);

    /** 查询指定模板的所有填报数据(不分页，用于导出) */
    List<FormSubmission> selectAllByTemplateId(@Param("templateId") Long templateId);
}
```

- [ ] **Step 5: 创建 MyBatis XML 映射**

`demo-form-engine/src/main/resources/mapper/FormTemplateMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.demoform.formengine.mapper.FormTemplateMapper">

    <select id="selectMyTemplates" resultType="com.demoform.formengine.entity.FormTemplate">
        SELECT id, name, description, owner_id, schema_json, status, created_at, updated_at
        FROM form_template
        WHERE owner_id = #{ownerId} AND deleted = 0
        ORDER BY updated_at DESC
    </select>

    <select id="selectPublishedTemplates" resultType="com.demoform.formengine.entity.FormTemplate">
        SELECT id, name, description, owner_id, schema_json, status, created_at, updated_at
        FROM form_template
        WHERE status = 'PUBLISHED' AND deleted = 0
        ORDER BY updated_at DESC
    </select>
</mapper>
```

`demo-form-engine/src/main/resources/mapper/FormSubmissionMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.demoform.formengine.mapper.FormSubmissionMapper">

    <select id="selectByTemplateId" resultType="com.demoform.formengine.entity.FormSubmission">
        SELECT id, template_id, submitter_id, data_json, status,
               approver_id, approved_at, created_at, updated_at
        FROM form_submission
        WHERE template_id = #{templateId} AND deleted = 0
        ORDER BY created_at DESC
    </select>

    <select id="selectMySubmissions" resultType="com.demoform.formengine.entity.FormSubmission">
        SELECT id, template_id, submitter_id, data_json, status,
               approver_id, approved_at, created_at, updated_at
        FROM form_submission
        WHERE submitter_id = #{submitterId} AND deleted = 0
        ORDER BY created_at DESC
    </select>

    <select id="selectAllByTemplateId" resultType="com.demoform.formengine.entity.FormSubmission">
        SELECT id, template_id, submitter_id, data_json, status,
               approver_id, approved_at, created_at, updated_at
        FROM form_submission
        WHERE template_id = #{templateId} AND deleted = 0
        ORDER BY created_at DESC
    </select>
</mapper>
```

- [ ] **Step 6: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-form-engine
```

- [ ] **Step 7: 提交**

```bash
git add demo-form-engine/
git commit -m "feat: 添加表单实体、Mapper 及 XML 映射"
```

---

### Task 12: FormTemplateService 实现

**Files:**
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/service/FormTemplateService.java`
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/service/impl/FormTemplateServiceImpl.java`
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/dto/TemplateCreateRequest.java`
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/dto/TemplateUpdateRequest.java`

- [ ] **Step 1: 创建 DTO**

```java
package com.demoform.formengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建表单模板请求
 */
@Data
public class TemplateCreateRequest {
    @NotBlank(message = "表单名称不能为空")
    private String name;
    private String description;
    private String schemaJson;
}
```

```java
package com.demoform.formengine.dto;

import lombok.Data;

/**
 * 更新表单模板请求
 */
@Data
public class TemplateUpdateRequest {
    private String name;
    private String description;
    private String schemaJson;
}
```

- [ ] **Step 2: 创建 FormTemplateService 接口**

```java
package com.demoform.formengine.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.demoform.common.dto.PageResult;
import com.demoform.formengine.dto.TemplateCreateRequest;
import com.demoform.formengine.dto.TemplateUpdateRequest;
import com.demoform.formengine.entity.FormTemplate;

import java.util.List;

/**
 * 表单模板服务接口
 */
public interface FormTemplateService {

    /** 创建表单模板 */
    FormTemplate create(Long ownerId, TemplateCreateRequest request);

    /** 更新表单模板 */
    FormTemplate update(Long templateId, Long userId, TemplateUpdateRequest request);

    /** 删除表单模板 */
    void delete(Long templateId, Long userId);

    /** 根据ID查询 */
    FormTemplate findById(Long templateId);

    /** 查询我的表单列表 */
    PageResult<FormTemplate> listMyTemplates(int page, int size, Long ownerId);

    /** 查询已发布的表单列表（供用户填报） */
    PageResult<FormTemplate> listPublishedTemplates(int page, int size);

    /** 发布表单 */
    void publish(Long templateId, Long userId);

    /** 停用表单 */
    void disable(Long templateId, Long userId);
}
```

- [ ] **Step 3: 创建 FormTemplateServiceImpl**

```java
package com.demoform.formengine.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.common.dto.PageResult;
import com.demoform.common.enums.FormStatus;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.dto.TemplateCreateRequest;
import com.demoform.formengine.dto.TemplateUpdateRequest;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormTemplateMapper;
import com.demoform.formengine.service.FormTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 表单模板服务实现
 */
@Service
@RequiredArgsConstructor
public class FormTemplateServiceImpl implements FormTemplateService {

    private final FormTemplateMapper formTemplateMapper;

    @Override
    @Transactional
    public FormTemplate create(Long ownerId, TemplateCreateRequest request) {
        FormTemplate template = new FormTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setOwnerId(ownerId);
        template.setSchemaJson(request.getSchemaJson());
        template.setStatus(FormStatus.DRAFT.name());
        formTemplateMapper.insert(template);
        return template;
    }

    @Override
    @Transactional
    public FormTemplate update(Long templateId, Long userId, TemplateUpdateRequest request) {
        FormTemplate template = findById(templateId);
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        if (request.getName() != null) template.setName(request.getName());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        if (request.getSchemaJson() != null) template.setSchemaJson(request.getSchemaJson());
        formTemplateMapper.updateById(template);
        return template;
    }

    @Override
    @Transactional
    public void delete(Long templateId, Long userId) {
        FormTemplate template = findById(templateId);
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        formTemplateMapper.deleteById(templateId);
    }

    @Override
    public FormTemplate findById(Long templateId) {
        FormTemplate template = formTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }
        return template;
    }

    @Override
    public PageResult<FormTemplate> listMyTemplates(int page, int size, Long ownerId) {
        Page<FormTemplate> pageParam = new Page<>(page, size);
        IPage<FormTemplate> result = formTemplateMapper.selectMyTemplates(pageParam, ownerId);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public PageResult<FormTemplate> listPublishedTemplates(int page, int size) {
        Page<FormTemplate> pageParam = new Page<>(page, size);
        IPage<FormTemplate> result = formTemplateMapper.selectPublishedTemplates(pageParam);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    @Transactional
    public void publish(Long templateId, Long userId) {
        FormTemplate template = findById(templateId);
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        if (FormStatus.PUBLISHED.name().equals(template.getStatus())) {
            throw new BusinessException(ResultCode.FORM_ALREADY_PUBLISHED);
        }
        template.setStatus(FormStatus.PUBLISHED.name());
        formTemplateMapper.updateById(template);
    }

    @Override
    @Transactional
    public void disable(Long templateId, Long userId) {
        FormTemplate template = findById(templateId);
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        template.setStatus(FormStatus.DISABLED.name());
        formTemplateMapper.updateById(template);
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-form-engine
```

- [ ] **Step 5: 提交**

```bash
git add demo-form-engine/src/main/java/com/demoform/formengine/service/ \
        demo-form-engine/src/main/java/com/demoform/formengine/dto/
git commit -m "feat: 实现表单模板 Service —— CRUD、发布/停用、分页查询"
```

---

### Task 13: FormSubmissionService 实现

**Files:**
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/service/FormSubmissionService.java`
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/service/impl/FormSubmissionServiceImpl.java`
- Create: `demo-form-engine/src/main/java/com/demoform/formengine/dto/SubmissionRequest.java`

- [ ] **Step 1: 创建 DTO**

```java
package com.demoform.formengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提报表单数据请求
 */
@Data
public class SubmissionRequest {
    @NotBlank(message = "填报数据不能为空")
    private String dataJson;
}
```

- [ ] **Step 2: 创建 FormSubmissionService 接口**

```java
package com.demoform.formengine.service;

import com.demoform.common.dto.PageResult;
import com.demoform.formengine.entity.FormSubmission;
import java.util.List;

/**
 * 表单填报服务接口
 */
public interface FormSubmissionService {

    /** 提报表单数据，返回填报记录并触发审批 */
    FormSubmission submit(Long templateId, Long submitterId, String dataJson);

    /** 查询指定模板的填报数据（表单所有者可见） */
    PageResult<FormSubmission> listByTemplate(int page, int size, Long templateId, Long userId);

    /** 查询我的填报记录 */
    PageResult<FormSubmission> listMySubmissions(int page, int size, Long submitterId);

    /** 导出模板填报数据（CSV） */
    List<FormSubmission> exportByTemplate(Long templateId, Long userId);

    /** 根据 ID 查询填报记录 */
    FormSubmission findById(Long submissionId);
}
```

- [ ] **Step 3: 创建 FormSubmissionServiceImpl**

```java
package com.demoform.formengine.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.common.dto.PageResult;
import com.demoform.common.enums.FormStatus;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.enums.SubmissionStatus;
import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import com.demoform.formengine.mapper.FormTemplateMapper;
import com.demoform.formengine.service.FormSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 表单填报服务实现
 */
@Service
@RequiredArgsConstructor
public class FormSubmissionServiceImpl implements FormSubmissionService {

    private final FormSubmissionMapper submissionMapper;
    private final FormTemplateMapper templateMapper;

    @Override
    @Transactional
    public FormSubmission submit(Long templateId, Long submitterId, String dataJson) {
        // 校验表单模板存在且已发布
        FormTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }
        if (!FormStatus.PUBLISHED.name().equals(template.getStatus())) {
            throw new BusinessException(ResultCode.FORM_NOT_PUBLISHED);
        }
        // 创建填报记录
        FormSubmission submission = new FormSubmission();
        submission.setTemplateId(templateId);
        submission.setSubmitterId(submitterId);
        submission.setDataJson(dataJson);
        submission.setStatus(SubmissionStatus.PENDING.name());
        submissionMapper.insert(submission);
        return submission;
    }

    @Override
    public PageResult<FormSubmission> listByTemplate(int page, int size,
                                                      Long templateId, Long userId) {
        // 校验表单所有权
        FormTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        Page<FormSubmission> pageParam = new Page<>(page, size);
        IPage<FormSubmission> result = submissionMapper.selectByTemplateId(pageParam, templateId);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public PageResult<FormSubmission> listMySubmissions(int page, int size, Long submitterId) {
        Page<FormSubmission> pageParam = new Page<>(page, size);
        IPage<FormSubmission> result = submissionMapper.selectMySubmissions(pageParam, submitterId);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public List<FormSubmission> exportByTemplate(Long templateId, Long userId) {
        // 校验所有权
        FormTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        return submissionMapper.selectAllByTemplateId(templateId);
    }

    @Override
    public FormSubmission findById(Long submissionId) {
        FormSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCode.SUBMISSION_NOT_FOUND);
        }
        return submission;
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-form-engine
```

- [ ] **Step 5: 提交**

```bash
git add demo-form-engine/src/main/java/com/demoform/formengine/service/ \
        demo-form-engine/src/main/java/com/demoform/formengine/dto/
git commit -m "feat: 实现表单填报 Service —— 提交数据、按模板/按用户查询、导出"
```

---

### Task 14: 表单控制器（FormTemplateController + FormSubmissionController）

**Files:**
- Create: `demo-web/src/main/java/com/demoform/web/controller/FormTemplateController.java`
- Create: `demo-web/src/main/java/com/demoform/web/controller/FormSubmissionController.java`

- [ ] **Step 1: 创建 FormTemplateController**

```java
package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.dto.PageResult;
import com.demoform.formengine.dto.TemplateCreateRequest;
import com.demoform.formengine.dto.TemplateUpdateRequest;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.service.FormTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 表单模板控制器
 */
@RestController
@RequestMapping("/api/forms/templates")
@RequiredArgsConstructor
public class FormTemplateController {

    private final FormTemplateService templateService;

    /** 创建表单模板 */
    @PostMapping
    public ApiResponse<FormTemplate> create(@Valid @RequestBody TemplateCreateRequest request,
                                              Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(templateService.create(userId, request));
    }

    /** 我的表单列表 */
    @GetMapping
    public ApiResponse<PageResult<FormTemplate>> listMine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(templateService.listMyTemplates(page, size, userId));
    }

    /** 已发布的表单列表（供用户填报入口） */
    @GetMapping("/published")
    public ApiResponse<PageResult<FormTemplate>> listPublished(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(templateService.listPublishedTemplates(page, size));
    }

    /** 表单模板详情 */
    @GetMapping("/{id}")
    public ApiResponse<FormTemplate> detail(@PathVariable Long id) {
        return ApiResponse.success(templateService.findById(id));
    }

    /** 编辑表单模板 */
    @PutMapping("/{id}")
    public ApiResponse<FormTemplate> update(@PathVariable Long id,
                                              @Valid @RequestBody TemplateUpdateRequest request,
                                              Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(templateService.update(id, userId, request));
    }

    /** 删除表单模板 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        templateService.delete(id, userId);
        return ApiResponse.success();
    }

    /** 发布表单 */
    @PutMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        templateService.publish(id, userId);
        return ApiResponse.success();
    }

    /** 停用表单 */
    @PutMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        templateService.disable(id, userId);
        return ApiResponse.success();
    }
}
```

- [ ] **Step 2: 创建 FormSubmissionController**

```java
package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.dto.PageResult;
import com.demoform.formengine.dto.SubmissionRequest;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.service.FormSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表单填报控制器
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormSubmissionController {

    private final FormSubmissionService submissionService;

    /** 提交表单数据 */
    @PostMapping("/submissions")
    public ApiResponse<FormSubmission> submit(@Valid @RequestBody SubmissionRequest request,
                                                @RequestParam Long templateId,
                                                Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        FormSubmission submission = submissionService.submit(templateId, userId, request.getDataJson());
        return ApiResponse.success(submission);
    }

    /** 我的填报记录 */
    @GetMapping("/submissions/my")
    public ApiResponse<PageResult<FormSubmission>> listMy(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(submissionService.listMySubmissions(page, size, userId));
    }

    /** 查看某模板的填报数据（表单所有者） */
    @GetMapping("/templates/{templateId}/submissions")
    public ApiResponse<PageResult<FormSubmission>> listByTemplate(
            @PathVariable Long templateId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(submissionService.listByTemplate(page, size, templateId, userId));
    }

    /** 导出填报数据为 CSV */
    @GetMapping("/templates/{templateId}/submissions/export")
    public ResponseEntity<byte[]> export(@PathVariable Long templateId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<FormSubmission> submissions = submissionService.exportByTemplate(templateId, userId);
        String csv = submissions.stream()
                .map(s -> s.getId() + "," + s.getSubmitterId() + "," + s.getDataJson() + ","
                        + s.getStatus() + "," + s.getCreatedAt())
                .collect(Collectors.joining("\n"));
        byte[] bytes = ("id,submitterId,data,status,createdAt\n" + csv)
                .getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.csv")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-web
```

- [ ] **Step 4: 提交**

```bash
git add demo-web/src/main/java/com/demoform/web/controller/FormTemplateController.java \
        demo-web/src/main/java/com/demoform/web/controller/FormSubmissionController.java
git commit -m "feat: 实现表单控制器 —— 模板 CRUD/发布/停用、数据提报/查询/CSV导出"
```

---

## Phase 4: 流程引擎

### Task 15: Camunda 7 配置与 BPMN 流程

**Files:**
- Create: `demo-workflow/src/main/resources/processes/approval.bpmn`
- Create: `demo-workflow/src/main/java/com/demoform/workflow/config/CamundaConfig.java`

- [ ] **Step 1: 创建 BPMN 审批流程**

`demo-workflow/src/main/resources/processes/approval.bpmn`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">

  <bpmn:process id="approval-process" name="表单数据审批流程" isExecutable="true">

    <bpmn:startEvent id="StartEvent" name="开始">
      <bpmn:outgoing>Flow_start_to_approve</bpmn:outgoing>
    </bpmn:startEvent>

    <bpmn:userTask id="ApproveTask" name="审批" camunda:assignee="${assignee}">
      <bpmn:incoming>Flow_start_to_approve</bpmn:incoming>
      <bpmn:outgoing>Flow_approve_to_gateway</bpmn:outgoing>
    </bpmn:userTask>

    <bpmn:exclusiveGateway id="Gateway" name="审批结果">
      <bpmn:incoming>Flow_approve_to_gateway</bpmn:incoming>
      <bpmn:outgoing>Flow_approved</bpmn:outgoing>
      <bpmn:outgoing>Flow_rejected</bpmn:outgoing>
    </bpmn:exclusiveGateway>

    <bpmn:sequenceFlow id="Flow_start_to_approve" sourceRef="StartEvent" targetRef="ApproveTask"/>

    <bpmn:sequenceFlow id="Flow_approve_to_gateway" sourceRef="ApproveTask" targetRef="Gateway"/>

    <bpmn:sequenceFlow id="Flow_approved" name="批准" sourceRef="Gateway" targetRef="ApprovedEnd">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">${approved}</bpmn:conditionExpression>
    </bpmn:sequenceFlow>

    <bpmn:sequenceFlow id="Flow_rejected" name="驳回" sourceRef="Gateway" targetRef="RejectedEnd">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">${!approved}</bpmn:conditionExpression>
    </bpmn:sequenceFlow>

    <bpmn:endEvent id="ApprovedEnd" name="已批准">
      <bpmn:incoming>Flow_approved</bpmn:incoming>
    </bpmn:endEvent>

    <bpmn:endEvent id="RejectedEnd" name="已驳回">
      <bpmn:incoming>Flow_rejected</bpmn:incoming>
    </bpmn:endEvent>

  </bpmn:process>
</bpmn:definitions>
```

- [ ] **Step 2: 创建 CamundaConfig（可选，默认配置已足够）**

```java
package com.demoform.workflow.config;

import org.springframework.context.annotation.Configuration;

/**
 * Camunda 7 配置 —— 使用默认自动配置，无需额外设置
 */
@Configuration
public class CamundaConfig {
    // Camunda Spring Boot Starter 自动配置引擎和数据库
    // BPMN 文件自动从 classpath:processes/ 部署
}
```

- [ ] **Step 3: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-workflow
```

- [ ] **Step 4: 提交**

```bash
git add demo-workflow/
git commit -m "feat: 添加 Camunda 7 审批流程 BPMN 定义"
```

---

### Task 16: ApprovalService 实现（含 Camunda Delegate）

**Files:**
- Create: `demo-workflow/src/main/java/com/demoform/workflow/service/ApprovalService.java`
- Create: `demo-workflow/src/main/java/com/demoform/workflow/service/impl/ApprovalServiceImpl.java`
- Create: `demo-workflow/src/main/java/com/demoform/workflow/delegate/ApprovalCompleteDelegate.java`
- Create: `demo-workflow/src/main/java/com/demoform/workflow/dto/TaskDto.java`
- Create: `demo-web/src/main/java/com/demoform/web/controller/ApprovalController.java`

- [ ] **Step 1: 创建 TaskDto**

```java
package com.demoform.workflow.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * Camunda 任务 DTO
 */
@Data
@Builder
public class TaskDto {
    private String taskId;
    private String processInstanceId;
    private String name;
    private Date createTime;
    private Map<String, Object> variables;
}
```

- [ ] **Step 2: 创建 ApprovalService 接口**

```java
package com.demoform.workflow.service;

import com.demoform.workflow.dto.TaskDto;
import java.util.List;

/**
 * 审批服务接口 —— 封装 Camunda 流程操作
 */
public interface ApprovalService {

    /** 启动审批流程 */
    void startApproval(Long submissionId);

    /** 批准 */
    void approve(String taskId, Long approverId);

    /** 驳回 */
    void reject(String taskId, Long approverId, String reason);

    /** 查询待审批任务列表 */
    List<TaskDto> getPendingTasks(Long userId);
}
```

- [ ] **Step 3: 创建 ApprovalCompleteDelegate**

```java
package com.demoform.workflow.delegate;

import com.demoform.common.enums.SubmissionStatus;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Camunda 审批完成回调 Delegate —— 流程结束时更新填报数据状态
 */
@Component
@RequiredArgsConstructor
public class ApprovalCompleteDelegate implements JavaDelegate {

    private final FormSubmissionMapper submissionMapper;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long submissionId = (Long) execution.getVariable("submissionId");
        Long approverId = (Long) execution.getVariable("approverId");
        Boolean approved = (Boolean) execution.getVariable("approved");

        FormSubmission submission = submissionMapper.selectById(submissionId);
        if (submission != null) {
            submission.setApproverId(approverId);
            submission.setApprovedAt(LocalDateTime.now());
            submission.setStatus(approved ? SubmissionStatus.APPROVED.name()
                    : SubmissionStatus.REJECTED.name());
            submissionMapper.updateById(submission);
        }
    }
}
```

- [ ] **Step 4: 创建 ApprovalServiceImpl**

```java
package com.demoform.workflow.service.impl;

import com.demoform.common.enums.ResultCode;
import com.demoform.common.enums.SubmissionStatus;
import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import com.demoform.workflow.dto.TaskDto;
import com.demoform.workflow.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审批服务实现 —— 基于 Camunda 7 流程引擎
 */
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final FormSubmissionMapper submissionMapper;

    @Override
    @Transactional
    public void startApproval(Long submissionId) {
        FormSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCode.SUBMISSION_NOT_FOUND);
        }
        if (!SubmissionStatus.PENDING.name().equals(submission.getStatus())) {
            throw new BusinessException(ResultCode.ALREADY_APPROVED);
        }
        // 启动审批流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("submissionId", submissionId);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                "approval-process", String.valueOf(submissionId), variables);
    }

    @Override
    @Transactional
    public void approve(String taskId, Long approverId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.APPROVAL_TASK_NOT_FOUND);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("approverId", approverId);
        taskService.complete(taskId, variables);
    }

    @Override
    @Transactional
    public void reject(String taskId, Long approverId, String reason) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.APPROVAL_TASK_NOT_FOUND);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);
        variables.put("approverId", approverId);
        if (reason != null) variables.put("rejectReason", reason);
        taskService.complete(taskId, variables);
    }

    @Override
    public List<TaskDto> getPendingTasks(Long userId) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateGroup("privileged")
                .initializeFormKeys()
                .list();
        return tasks.stream().map(task -> TaskDto.builder()
                .taskId(task.getId())
                .processInstanceId(task.getProcessInstanceId())
                .name(task.getName())
                .createTime(task.getCreateTime())
                .variables(taskService.getVariables(task.getId()))
                .build()).collect(Collectors.toList());
    }
}
```

- [ ] **Step 5: 修改 FormSubmissionServiceImpl.submit 方法，提交后触发审批**

在 `FormSubmissionServiceImpl.java` 的 `submit` 方法末尾，插入插入审批调用：

在 `submissionMapper.insert(submission);` 之后、`return submission;` 之前添加：
```java
// 触发审批流程（需注入 ApprovalService）
approvalService.startApproval(submission.getId());
```

同时更新 `FormSubmissionServiceImpl` 的字段和构造函数，注入 `ApprovalService`：
```java
private final ApprovalService approvalService;
```

- [ ] **Step 6: 创建 ApprovalController**

```java
package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.workflow.dto.TaskDto;
import com.demoform.workflow.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批控制器 —— 特权用户操作
 */
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PRIVILEGED')")
public class ApprovalController {

    private final ApprovalService approvalService;

    /** 待审批列表 */
    @GetMapping("/pending")
    public ApiResponse<List<TaskDto>> pending() {
        return ApiResponse.success(approvalService.getPendingTasks(null));
    }

    /** 批准 */
    @PutMapping("/{submissionId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long submissionId, Authentication auth) {
        Long approverId = (Long) auth.getPrincipal();
        // 查询 Camunda task (by businessKey = submissionId)
        approvalService.approve(findTaskByBusinessKey(String.valueOf(submissionId)), approverId);
        return ApiResponse.success();
    }

    /** 驳回 */
    @PutMapping("/{submissionId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long submissionId,
                                      @RequestParam(required = false) String reason,
                                      Authentication auth) {
        Long approverId = (Long) auth.getPrincipal();
        approvalService.reject(findTaskByBusinessKey(String.valueOf(submissionId)), approverId, reason);
        return ApiResponse.success();
    }

    private String findTaskByBusinessKey(String businessKey) {
        var task = org.camunda.bpm.engine.TaskService.class; // 实际实现需注入 TaskService
        // 简化实现：通过 businessKey 查找 task
        return null; // 待实现 — 需注入 TaskService 调用 taskQuery
    }
}
```

**注意：** ApprovalController 的 `findTaskByBusinessKey` 需要访问 Camunda `TaskService`。简化处理：在 `ApprovalService` 中添加 `findTaskBySubmissionId` 方法，返回 taskId。

在 `ApprovalService` 接口中添加：
```java
String findTaskBySubmissionId(Long submissionId);
```

在 `ApprovalServiceImpl` 中实现：
```java
@Override
public String findTaskBySubmissionId(Long submissionId) {
    Task task = taskService.createTaskQuery()
            .processInstanceBusinessKey(String.valueOf(submissionId))
            .singleResult();
    if (task == null) {
        throw new BusinessException(ResultCode.APPROVAL_TASK_NOT_FOUND);
    }
    return task.getId();
}
```

更新 ApprovalController 使用此方法：
```java
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PRIVILEGED')")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    public ApiResponse<List<TaskDto>> pending() {
        return ApiResponse.success(approvalService.getPendingTasks(null));
    }

    @PutMapping("/{submissionId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long submissionId, Authentication auth) {
        Long approverId = (Long) auth.getPrincipal();
        String taskId = approvalService.findTaskBySubmissionId(submissionId);
        approvalService.approve(taskId, approverId);
        return ApiResponse.success();
    }

    @PutMapping("/{submissionId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long submissionId,
                                      @RequestParam(required = false) String reason,
                                      Authentication auth) {
        Long approverId = (Long) auth.getPrincipal();
        String taskId = approvalService.findTaskBySubmissionId(submissionId);
        approvalService.reject(taskId, approverId, reason);
        return ApiResponse.success();
    }
}
```

- [ ] **Step 7: 编译验证 + 处理循环依赖**

```bash
cd /Users/fan/gitspace/demoReact && mvn compile -pl demo-workflow,demo-web
```

注意：`FormSubmissionServiceImpl` 中注入 `ApprovalService` 可能造成模块间的循环依赖。如果出现，使用 `@Lazy` 注解或考虑通过事件机制解耦。

- [ ] **Step 8: 提交**

```bash
git add demo-workflow/ demo-web/src/main/java/com/demoform/web/controller/ApprovalController.java
git commit -m "feat: 实现审批服务及控制器

基于 Camunda 7 的审批流程：启动/批准/驳回/待办查询，审批完成 Delegate 回调更新数据状态。"
```

---

### Task 17: 全局异常处理与项目启动验证

**Files:**
- Create: `demo-web/src/main/java/com/demoform/web/handler/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建全局异常处理器**

```java
package com.demoform.web.handler;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 —— 统一将异常转换为 ApiResponse
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", detail);
        return ApiResponse.fail(ResultCode.VALIDATION_FAILED.getCode(), detail);
    }

    /** 认证失败 */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuth(Exception e) {
        log.warn("认证失败: {}", e.getMessage());
        return ApiResponse.fail(ResultCode.UNAUTHORIZED);
    }

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleForbidden(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ApiResponse.fail(ResultCode.FORBIDDEN);
    }

    /** 未知异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknown(Exception e) {
        log.error("未知异常", e);
        return ApiResponse.fail(ResultCode.INTERNAL_ERROR);
    }
}
```

- [ ] **Step 2: 首次启动验证**

```bash
cd /Users/fan/gitspace/demoReact && mvn clean install -DskipTests
```

Expected: BUILD SUCCESS（所有模块编译通过，web 模块可启动）

- [ ] **Step 3: 启动应用并测试 H2 控制台**

```bash
cd demo-web && mvn spring-boot:run
# 另开终端测试：
curl http://localhost:8080/h2-console
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

- [ ] **Step 4: 提交**

```bash
git add demo-web/src/main/java/com/demoform/web/handler/
git commit -m "feat: 全局异常处理器 —— 业务异常/参数校验/认证/权限/未知异常统一响应"
```

---

### Task 18: 后端单元测试

**Files:**
- Create: `demo-user/src/test/java/com/demoform/user/service/UserServiceTest.java`
- Create: `demo-form-engine/src/test/java/com/demoform/formengine/service/FormTemplateServiceTest.java`
- Create: `demo-form-engine/src/test/java/com/demoform/formengine/service/FormSubmissionServiceTest.java`
- Create: `demo-web/src/test/java/com/demoform/web/controller/AuthControllerTest.java`

- [ ] **Step 1: 创建 UserServiceTest**

```java
package com.demoform.user.service;

import com.demoform.common.dto.RegisterRequest;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.user.entity.SysUser;
import com.demoform.user.mapper.UserMapper;
import com.demoform.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private RoleService roleService;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("Abc12345");
        request.setEmail("test@test.com");
    }

    @Test
    void shouldRegisterSuccessfully() {
        when(userMapper.selectByUsername("testuser")).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(roleService.findByCode("ROLE_USER")).thenReturn(
                new com.demoform.user.entity.SysRole() {{ setId(3L); setCode("ROLE_USER"); }});
        when(userMapper.insert(any())).thenReturn(1);
        when(userRoleMapper.insert(any())).thenReturn(1);

        userService.register(request);

        verify(userMapper).insert(any(SysUser.class));
        verify(userRoleMapper).insert(any());
    }

    @Test
    void shouldFailWhenUsernameExists() {
        when(userMapper.selectByUsername("testuser")).thenReturn(new SysUser());

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResultCode.USERNAME_EXISTS.getCode());
    }

    @Test
    void shouldFailWhenPasswordWeak() {
        request.setPassword("12345678"); // 不含大小写字母组合

        when(userMapper.selectByUsername("testuser")).thenReturn(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResultCode.PASSWORD_WEAK.getCode());
    }
}
```

- [ ] **Step 2: 创建 FormTemplateServiceTest**

```java
package com.demoform.formengine.service;

import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.dto.TemplateCreateRequest;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormTemplateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormTemplateServiceTest {

    @Mock private FormTemplateMapper formTemplateMapper;
    @InjectMocks private FormTemplateServiceImpl service;

    @Test
    void shouldCreateTemplate() {
        when(formTemplateMapper.insert(any())).thenReturn(1);
        TemplateCreateRequest req = new TemplateCreateRequest();
        req.setName("测试表单");
        req.setDescription("描述");
        req.setSchemaJson("[{\"name\":\"field1\",\"type\":\"text\"}]");

        FormTemplate result = service.create(1L, req);

        assertThat(result.getName()).isEqualTo("测试表单");
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        verify(formTemplateMapper).insert(any(FormTemplate.class));
    }

    @Test
    void shouldPublishTemplate() {
        FormTemplate template = new FormTemplate();
        template.setId(1L);
        template.setOwnerId(1L);
        template.setStatus("DRAFT");
        when(formTemplateMapper.selectById(1L)).thenReturn(template);
        when(formTemplateMapper.updateById(any())).thenReturn(1);

        service.publish(1L, 1L);

        assertThat(template.getStatus()).isEqualTo("PUBLISHED");
    }
}
```

- [ ] **Step 3: 创建 AuthControllerTest**

```java
package com.demoform.web.controller;

import com.demoform.common.dto.LoginRequest;
import com.demoform.common.dto.RegisterRequest;
import com.demoform.user.entity.SysUser;
import com.demoform.user.service.UserService;
import com.demoform.web.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AuthenticationManager authenticationManager;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setStatus(1);

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

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("Abc12345");
        request.setEmail("new@test.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

- [ ] **Step 4: 运行测试**

```bash
cd /Users/fan/gitspace/demoReact && mvn test
```

Expected: 所有测试通过

- [ ] **Step 5: 提交**

```bash
git add demo-user/src/test/ demo-form-engine/src/test/ demo-web/src/test/
git commit -m "test: 添加后端单元测试 —— 用户注册、表单模板、认证接口"
```

---

## Phase 5: 前端

### Task 19: React 项目初始化

**Files:**
- Create: `demo-frontend/package.json`
- Create: `demo-frontend/vite.config.ts`
- Create: `demo-frontend/tsconfig.json`
- Create: `demo-frontend/tsconfig.node.json`
- Create: `demo-frontend/index.html`
- Create: `demo-frontend/src/main.tsx`
- Create: `demo-frontend/src/App.tsx`
- Create: `demo-frontend/src/App.css`
- Create: `demo-frontend/src/vite-env.d.ts`

- [ ] **Step 1: 初始化前端项目目录**

```bash
mkdir -p /Users/fan/gitspace/demoReact/demo-frontend/src/{api,components,contexts,pages,types}
```

- [ ] **Step 2: 创建 package.json**

```json
{
  "name": "demo-frontend",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "test": "vitest run"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^6.23.1",
    "antd": "^5.17.0",
    "@ant-design/icons": "^5.3.7",
    "@dnd-kit/core": "^6.1.0",
    "@dnd-kit/sortable": "^8.0.0",
    "@dnd-kit/utilities": "^3.2.2",
    "axios": "^1.7.2"
  },
  "devDependencies": {
    "@types/react": "^18.3.3",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.0",
    "typescript": "^5.4.5",
    "vite": "^5.2.12",
    "vitest": "^1.6.0",
    "@testing-library/react": "^16.0.0",
    "@testing-library/jest-dom": "^6.4.5",
    "jsdom": "^24.1.0"
  }
}
```

- [ ] **Step 3: 创建 vite.config.ts**

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

- [ ] **Step 4: 创建 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 5: 创建 tsconfig.node.json**

```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}
```

- [ ] **Step 6: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>表单数据平台</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 7: 创建 main.tsx**

```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './App.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

- [ ] **Step 8: 创建 App.tsx（入口路由骨架）**

```tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<div>首页（后续替换）</div>} />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
```

- [ ] **Step 9: 创建基础样式 App.css**

```css
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

#root {
  min-height: 100vh;
}
```

- [ ] **Step 10: 安装依赖并验证**

```bash
cd /Users/fan/gitspace/demoReact/demo-frontend && npm install && npm run dev
```

Expected: 开发服务器启动在 `http://localhost:5173`

- [ ] **Step 11: 提交**

```bash
git add demo-frontend/
git commit -m "chore: 初始化 React 前端项目 —— Vite + TypeScript + Ant Design + React Router"
```

---

### Task 20: 前端 API 层与 Auth Context

**Files:**
- Create: `demo-frontend/src/api/request.ts`
- Create: `demo-frontend/src/api/auth.ts`
- Create: `demo-frontend/src/api/user.ts`
- Create: `demo-frontend/src/api/form.ts`
- Create: `demo-frontend/src/api/approval.ts`
- Create: `demo-frontend/src/contexts/AuthContext.tsx`

- [ ] **Step 1: 创建 Axios 请求封装**

```typescript
// src/api/request.ts
import axios from 'axios';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// 请求拦截器：自动附加 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data;
    if (code === 200) {
      return data;
    }
    return Promise.reject(new Error(message || '请求失败'));
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default request;
```

- [ ] **Step 2: 创建 Auth API**

```typescript
// src/api/auth.ts
import request from './request';

export interface LoginParams {
  username: string;
  password: string;
}

export interface RegisterParams {
  username: string;
  password: string;
  email?: string;
}

export interface LoginResult {
  token: string;
  userId: number;
  username: string;
  email: string;
  roles: string[];
}

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  status: number;
  passwordExpireDate: string;
  roles: string[];
}

export const authApi = {
  login: (params: LoginParams) =>
    request.post<any, LoginResult>('/auth/login', params),

  register: (params: RegisterParams) =>
    request.post('/auth/register', params),

  getMe: () => request.get<any, UserInfo>('/auth/me'),
};
```

- [ ] **Step 3: 创建 User API**

```typescript
// src/api/user.ts
import request from './request';

export interface UserVO {
  id: number;
  username: string;
  email: string;
  status: number;
  passwordExpireDate: string;
  roles: string[];
  createdAt: string;
}

export interface PageResult<T> {
  total: number;
  page: number;
  size: number;
  records: T[];
}

export const userApi = {
  list: (params: { page: number; size: number; username?: string }) =>
    request.get<any, PageResult<UserVO>>('/users', { params }),

  detail: (id: number) =>
    request.get<any, UserVO>(`/users/${id}`),

  update: (id: number, data: any) =>
    request.put(`/users/${id}`, data),

  delete: (id: number) =>
    request.delete(`/users/${id}`),

  assignRoles: (id: number, roleIds: number[]) =>
    request.put(`/users/${id}/roles`, { roleIds }),

  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    request.put('/users/change-password', data),
};
```

- [ ] **Step 4: 创建 Form API**

```typescript
// src/api/form.ts
import request from './request';

export interface FormTemplate {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  schemaJson: string;
  status: string;
  createdAt: string;
}

export interface FormSubmission {
  id: number;
  templateId: number;
  submitterId: number;
  dataJson: string;
  status: string;
  approverId?: number;
  approvedAt?: string;
  createdAt: string;
}

export interface PageResult<T> {
  total: number;
  page: number;
  size: number;
  records: T[];
}

export const formTemplateApi = {
  create: (data: { name: string; description?: string; schemaJson?: string }) =>
    request.post<any, FormTemplate>('/forms/templates', data),

  listMine: (params: { page: number; size: number }) =>
    request.get<any, PageResult<FormTemplate>>('/forms/templates', { params }),

  listPublished: (params: { page: number; size: number }) =>
    request.get<any, PageResult<FormTemplate>>('/forms/templates/published', { params }),

  detail: (id: number) =>
    request.get<any, FormTemplate>(`/forms/templates/${id}`),

  update: (id: number, data: any) =>
    request.put<any, FormTemplate>(`/forms/templates/${id}`, data),

  delete: (id: number) => request.delete(`/forms/templates/${id}`),

  publish: (id: number) => request.put(`/forms/templates/${id}/publish`),

  disable: (id: number) => request.put(`/forms/templates/${id}/disable`),
};

export const formSubmissionApi = {
  submit: (templateId: number, dataJson: string) =>
    request.post<any, FormSubmission>('/forms/submissions', { dataJson, params: { templateId } }),

  listByTemplate: (templateId: number, params: { page: number; size: number }) =>
    request.get<any, PageResult<FormSubmission>>(`/forms/templates/${templateId}/submissions`, { params }),

  listMy: (params: { page: number; size: number }) =>
    request.get<any, PageResult<FormSubmission>>('/forms/submissions/my', { params }),

  exportCsv: (templateId: number) =>
    request.get(`/forms/templates/${templateId}/submissions/export`, { responseType: 'blob' }),
};
```

- [ ] **Step 5: 创建 Approval API**

```typescript
// src/api/approval.ts
import request from './request';

export interface TaskDto {
  taskId: string;
  processInstanceId: string;
  name: string;
  createTime: string;
  variables: Record<string, any>;
}

export const approvalApi = {
  pending: () =>
    request.get<any, TaskDto[]>('/approvals/pending'),

  approve: (submissionId: number) =>
    request.put(`/approvals/${submissionId}/approve`),

  reject: (submissionId: number, reason?: string) =>
    request.put(`/approvals/${submissionId}/reject`, null, { params: { reason } }),
};
```

- [ ] **Step 6: 创建 AuthContext**

```typescript
// src/contexts/AuthContext.tsx
import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi, LoginParams, RegisterParams, UserInfo } from '../api/auth';

interface AuthState {
  user: UserInfo | null;
  token: string | null;
  loading: boolean;
  login: (params: LoginParams) => Promise<void>;
  register: (params: RegisterParams) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthState | null>(null);

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem('token');

  // 启动时恢复登录状态
  useEffect(() => {
    if (token) {
      authApi.getMe()
        .then(setUser)
        .catch(() => {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

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
    localStorage.setItem('user', JSON.stringify(userInfo));
    setUser(userInfo);
  }, []);

  const register = useCallback(async (params: RegisterParams) => {
    await authApi.register(params);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  const hasRole = useCallback((role: string) => {
    return user?.roles?.includes(role) ?? false;
  }, [user]);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
```

- [ ] **Step 7: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact/demo-frontend && npx tsc --noEmit
```

- [ ] **Step 8: 提交**

```bash
git add demo-frontend/src/api/ demo-frontend/src/contexts/
git commit -m "feat: 前端 API 层封装 + AuthContext

Axios 拦截器自动附加 Token、统一错误处理；AuthContext 管理登录/注册/登出/角色判断。"
```

---

### Task 21: 前端 ProtectedRoute + MainLayout + 路由

**Files:**
- Create: `demo-frontend/src/components/ProtectedRoute.tsx`
- Create: `demo-frontend/src/components/MainLayout.tsx`

- [ ] **Step 1: 创建 ProtectedRoute**

```tsx
// src/components/ProtectedRoute.tsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Spin } from 'antd';

interface Props {
  children: React.ReactNode;
  requiredRole?: string;
}

const ProtectedRoute: React.FC<Props> = ({ children, requiredRole }) => {
  const { isAuthenticated, loading, hasRole } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
```

- [ ] **Step 2: 创建 MainLayout**

```tsx
// src/components/MainLayout.tsx
import { Layout, Menu, Button, Dropdown, Avatar } from 'antd';
import {
  FormOutlined, FileTextOutlined, CheckCircleOutlined,
  UserOutlined, DashboardOutlined, LogoutOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const { Header, Sider, Content } = Layout;

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, hasRole } = useAuth();

  const menuItems = [
    { key: '/', icon: <DashboardOutlined />, label: '首页' },
    { key: '/forms/templates', icon: <FormOutlined />, label: '我的表单' },
    { key: '/forms/submit', icon: <FileTextOutlined />, label: '填报数据' },
    ...(hasRole('ROLE_PRIVILEGED')
      ? [{ key: '/approvals/pending', icon: <CheckCircleOutlined />, label: '待审批' }]
      : []),
    ...(hasRole('ROLE_ADMIN')
      ? [{ key: '/admin/users', icon: <UserOutlined />, label: '用户管理' }]
      : []),
  ];

  const userMenuItems = [
    { key: 'profile', label: `${user?.username}` },
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true },
  ];

  const handleMenuClick = ({ key }: { key: string }) => navigate(key);

  const handleUserMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') logout();
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <div style={{
          height: 64, display: 'flex', alignItems: 'center',
          justifyContent: 'center', color: '#fff', fontSize: 18, fontWeight: 'bold'
        }}>
          表单平台
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header style={{
          background: '#fff', padding: '0 24px', display: 'flex',
          justifyContent: 'flex-end', alignItems: 'center'
        }}>
          <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }}>
            <Button type="text" icon={<Avatar size="small" icon={<UserOutlined />} />}>
              {user?.username}
            </Button>
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, padding: 24, background: '#fff', borderRadius: 8 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
```

- [ ] **Step 3: 更新 App.tsx 完整路由**

```tsx
// src/App.tsx (覆写)
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './components/MainLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import FormTemplateListPage from './pages/FormTemplateListPage';
import FormDesignerPage from './pages/FormDesignerPage';
import FormSubmitListPage from './pages/FormSubmitListPage';
import FormSubmitPage from './pages/FormSubmitPage';
import MySubmissionsPage from './pages/MySubmissionsPage';
import SubmissionListPage from './pages/SubmissionListPage';
import ApprovalPage from './pages/ApprovalPage';
import UserManagementPage from './pages/UserManagementPage';
import './App.css';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* 公开路由 */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            {/* 需要认证的路由 */}
            <Route element={
              <ProtectedRoute><MainLayout /></ProtectedRoute>
            }>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/forms/templates" element={<FormTemplateListPage />} />
              <Route path="/forms/templates/new" element={<FormDesignerPage />} />
              <Route path="/forms/templates/:id" element={<FormDesignerPage />} />
              <Route path="/forms/templates/:id/submissions" element={<SubmissionListPage />} />
              <Route path="/forms/submit" element={<FormSubmitListPage />} />
              <Route path="/forms/submit/:id" element={<FormSubmitPage />} />
              <Route path="/forms/submissions/my" element={<MySubmissionsPage />} />
              <Route path="/approvals/pending" element={
                <ProtectedRoute requiredRole="ROLE_PRIVILEGED"><ApprovalPage /></ProtectedRoute>
              } />
              <Route path="/admin/users" element={
                <ProtectedRoute requiredRole="ROLE_ADMIN"><UserManagementPage /></ProtectedRoute>
              } />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
```

- [ ] **Step 4: 编译验证**

```bash
cd /Users/fan/gitspace/demoReact/demo-frontend && npx tsc --noEmit
```

注意：此步骤因为页面组件尚未创建会有编译错误。仅确认路由结构正确即可。

- [ ] **Step 5: 提交**

```bash
git add demo-frontend/src/components/ demo-frontend/src/App.tsx
git commit -m "feat: 前端路由框架 —— ProtectedRoute + MainLayout + 完整路由表"
```

---

### Task 22: 登录与注册页面

**Files:**
- Create: `demo-frontend/src/pages/LoginPage.tsx`
- Create: `demo-frontend/src/pages/RegisterPage.tsx`
- Create: `demo-frontend/src/pages/DashboardPage.tsx`

- [ ] **Step 1: 创建 LoginPage**

```tsx
// src/pages/LoginPage.tsx
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const onFinish = async (values: { username: string; password: string }) => {
    try {
      await login(values);
      message.success('登录成功');
      navigate('/');
    } catch (err: any) {
      message.error(err.response?.data?.message || err.message || '登录失败');
    }
  };

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', background: '#f0f2f5'
    }}>
      <Card title="表单数据平台 - 登录" style={{ width: 400 }}>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>登录</Button>
          </Form.Item>
          <div style={{ textAlign: 'center' }}>
            没有账号？ <Link to="/register">立即注册</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;
```

- [ ] **Step 2: 创建 RegisterPage**

```tsx
// src/pages/RegisterPage.tsx
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const onFinish = async (values: { username: string; password: string; email?: string }) => {
    try {
      await register(values);
      message.success('注册成功，请登录');
      navigate('/login');
    } catch (err: any) {
      message.error(err.response?.data?.message || err.message || '注册失败');
    }
  };

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', background: '#f0f2f5'
    }}>
      <Card title="用户注册" style={{ width: 400 }}>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="username"
            rules={[
              { required: true, message: '请输入用户名' },
              { min: 3, max: 50, message: '用户名长度3-50位' }
            ]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码至少8位' },
              { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, message: '需含大小写字母和数字' }
            ]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item name="email">
            <Input prefix={<MailOutlined />} placeholder="邮箱（选填）" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>注册</Button>
          </Form.Item>
          <div style={{ textAlign: 'center' }}>
            已有账号？ <Link to="/login">去登录</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default RegisterPage;
```

- [ ] **Step 3: 创建 DashboardPage**

```tsx
// src/pages/DashboardPage.tsx
import { Card, Row, Col, Statistic } from 'antd';
import { FormOutlined, FileTextOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';

const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div>
      <h2>欢迎，{user?.username}</h2>
      <p style={{ color: '#666', marginBottom: 24 }}>
        角色：{user?.roles?.join(', ')}
      </p>
      <Row gutter={16}>
        <Col span={8}>
          <Card>
            <Statistic title="我的表单模板" value={0} prefix={<FormOutlined />} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="我的填报记录" value={0} prefix={<FileTextOutlined />} />
          </Card>
        </Col>
        {user?.roles?.includes('ROLE_PRIVILEGED') && (
          <Col span={8}>
            <Card>
              <Statistic title="待审批" value={0} prefix={<CheckCircleOutlined />} />
            </Card>
          </Col>
        )}
      </Row>
    </div>
  );
};

export default DashboardPage;
```

- [ ] **Step 4: 验证前端编译**

```bash
cd /Users/fan/gitspace/demoReact/demo-frontend && npx tsc --noEmit
```

- [ ] **Step 5: 提交**

```bash
git add demo-frontend/src/pages/LoginPage.tsx \
        demo-frontend/src/pages/RegisterPage.tsx \
        demo-frontend/src/pages/DashboardPage.tsx
git commit -m "feat: 登录、注册、首页仪表盘页面"
```

---

### Task 23: 表单设计器页面（拖拽）

**Files:**
- Create: `demo-frontend/src/pages/FormDesignerPage.tsx`
- Create: `demo-frontend/src/pages/FormDesignerPage/FieldPalette.tsx`
- Create: `demo-frontend/src/pages/FormDesignerPage/DesignCanvas.tsx`
- Create: `demo-frontend/src/pages/FormDesignerPage/FieldConfigPanel.tsx`
- Create: `demo-frontend/src/types/form.ts`

- [ ] **Step 1: 创建表单类型定义**

```typescript
// src/types/form.ts
export interface FormField {
  id: string;
  type: 'text' | 'textarea' | 'number' | 'date' | 'select' | 'radio' | 'checkbox' | 'file';
  label: string;
  name: string;
  required: boolean;
  placeholder?: string;
  options?: string[]; // 用于 select/radio/checkbox
}

export const fieldTypeLabels: Record<string, string> = {
  text: '文本输入',
  textarea: '多行文本',
  number: '数字输入',
  date: '日期选择',
  select: '下拉选择',
  radio: '单选',
  checkbox: '多选',
  file: '文件上传',
};
```

- [ ] **Step 2: 创建 FieldPalette（左侧字段面板）**

```tsx
// src/pages/FormDesignerPage/FieldPalette.tsx
import { useDraggable } from '@dnd-kit/core';
import { Card } from 'antd';
import { fieldTypeLabels } from '../../types/form';

const fieldTypes: { type: string; icon: string }[] = [
  { type: 'text', icon: 'Aa' },
  { type: 'textarea', icon: '📝' },
  { type: 'number', icon: '#' },
  { type: 'date', icon: '📅' },
  { type: 'select', icon: '📋' },
  { type: 'radio', icon: '🔘' },
  { type: 'checkbox', icon: '☑' },
  { type: 'file', icon: '📎' },
];

const FieldPalette: React.FC = () => {
  return (
    <div>
      <h4 style={{ marginBottom: 12 }}>字段类型</h4>
      {fieldTypes.map((ft) => (
        <PaletteItem key={ft.type} type={ft.type} icon={ft.icon} />
      ))}
    </div>
  );
};

const PaletteItem: React.FC<{ type: string; icon: string }> = ({ type, icon }) => {
  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: `palette-${type}`,
    data: { type, isNew: true },
  });

  return (
    <div
      ref={setNodeRef}
      {...listeners}
      {...attributes}
      style={{
        padding: '8px 12px',
        marginBottom: 8,
        background: isDragging ? '#e6f7ff' : '#fafafa',
        border: '1px solid #d9d9d9',
        borderRadius: 6,
        cursor: 'grab',
        userSelect: 'none',
      }}
    >
      {icon} {(fieldTypeLabels as any)[type] || type}
    </div>
  );
};

export default FieldPalette;
```

- [ ] **Step 3: 创建 DesignCanvas（中间画布）**

```tsx
// src/pages/FormDesignerPage/DesignCanvas.tsx
import { useDroppable } from '@dnd-kit/core';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { FormField } from '../../types/form';
import { DeleteOutlined, HolderOutlined } from '@ant-design/icons';
import { Button } from 'antd';

interface Props {
  fields: FormField[];
  selectedId: string | null;
  onSelect: (id: string) => void;
  onRemove: (id: string) => void;
}

const DesignCanvas: React.FC<Props> = ({ fields, selectedId, onSelect, onRemove }) => {
  const { setNodeRef, isOver } = useDroppable({ id: 'canvas' });

  return (
    <div>
      <h4 style={{ marginBottom: 12 }}>表单预览</h4>
      <div
        ref={setNodeRef}
        style={{
          minHeight: 400,
          background: isOver ? '#f0f5ff' : '#fff',
          border: '2px dashed ' + (isOver ? '#1677ff' : '#d9d9d9'),
          borderRadius: 8,
          padding: 16,
        }}
      >
        {fields.length === 0 && (
          <div style={{ textAlign: 'center', color: '#999', paddingTop: 160 }}>
            从左侧拖入字段到此处
          </div>
        )}
        {fields.map((field) => (
          <SortableField
            key={field.id}
            field={field}
            isSelected={selectedId === field.id}
            onSelect={() => onSelect(field.id)}
            onRemove={() => onRemove(field.id)}
          />
        ))}
      </div>
    </div>
  );
};

const SortableField: React.FC<{
  field: FormField;
  isSelected: boolean;
  onSelect: () => void;
  onRemove: () => void;
}> = ({ field, isSelected, onSelect, onRemove }) => {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({
    id: field.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    padding: '8px 12px',
    marginBottom: 8,
    background: isSelected ? '#e6f7ff' : '#fafafa',
    border: `1px solid ${isSelected ? '#1677ff' : '#d9d9d9'}`,
    borderRadius: 6,
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    cursor: 'pointer',
  };

  return (
    <div ref={setNodeRef} style={style} onClick={onSelect}>
      <span {...attributes} {...listeners} style={{ cursor: 'grab' }}>
        <HolderOutlined />
      </span>
      <span style={{ flex: 1 }}>
        {field.label}
        {field.required && <span style={{ color: 'red', marginLeft: 4 }}>*</span>}
      </span>
      <span style={{ color: '#999', fontSize: 12 }}>{field.type}</span>
      <Button
        type="text" size="small" danger
        icon={<DeleteOutlined />}
        onClick={(e) => { e.stopPropagation(); onRemove(); }}
      />
    </div>
  );
};

export default DesignCanvas;
```

- [ ] **Step 4: 创建 FieldConfigPanel（右侧属性面板）**

```tsx
// src/pages/FormDesignerPage/FieldConfigPanel.tsx
import { Form, Input, Switch, Select, Button, Space } from 'antd';
import { FormField } from '../../types/form';

interface Props {
  field: FormField | null;
  onUpdate: (field: FormField) => void;
}

const FieldConfigPanel: React.FC<Props> = ({ field, onUpdate }) => {
  if (!field) {
    return (
      <div>
        <h4>属性配置</h4>
        <p style={{ color: '#999' }}>请选择一个字段</p>
      </div>
    );
  }

  const handleChange = (changed: Partial<FormField>) => {
    onUpdate({ ...field, ...changed });
  };

  return (
    <div>
      <h4 style={{ marginBottom: 12 }}>属性配置</h4>
      <Form layout="vertical" size="small">
        <Form.Item label="字段标签">
          <Input value={field.label}
            onChange={e => handleChange({ label: e.target.value })} />
        </Form.Item>
        <Form.Item label="字段名（key）">
          <Input value={field.name}
            onChange={e => handleChange({ name: e.target.value })} />
        </Form.Item>
        <Form.Item label="占位提示">
          <Input value={field.placeholder || ''}
            onChange={e => handleChange({ placeholder: e.target.value })} />
        </Form.Item>
        <Form.Item label="是否必填">
          <Switch checked={field.required}
            onChange={v => handleChange({ required: v })} />
        </Form.Item>
        {(field.type === 'select' || field.type === 'radio' || field.type === 'checkbox') && (
          <Form.Item label="选项（每行一个）">
            <Input.TextArea
              value={field.options?.join('\n') || ''}
              rows={4}
              onChange={e => handleChange({ options: e.target.value.split('\n').filter(s => s.trim()) })}
            />
          </Form.Item>
        )}
      </Form>
    </div>
  );
};

export default FieldConfigPanel;
```

- [ ] **Step 5: 创建 FormDesignerPage 主页面**

```tsx
// src/pages/FormDesignerPage.tsx
import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Row, Col, Input, Button, Space, message } from 'antd';
import { SaveOutlined, SendOutlined } from '@ant-design/icons';
import {
  DndContext, DragEndEvent, PointerSensor, useSensor, useSensors,
  closestCenter,
} from '@dnd-kit/core';
import {
  SortableContext, verticalListSortingStrategy, arrayMove,
} from '@dnd-kit/sortable';
import FieldPalette from './FormDesignerPage/FieldPalette';
import DesignCanvas from './FormDesignerPage/DesignCanvas';
import FieldConfigPanel from './FormDesignerPage/FieldConfigPanel';
import { FormField } from '../types/form';
import { formTemplateApi } from '../api/form';

let idCounter = 0;
const genId = () => `field_${Date.now()}_${idCounter++}`;

const FormDesignerPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [fields, setFields] = useState<FormField[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const sensors = useSensors(useSensor(PointerSensor, {
    activationConstraint: { distance: 5 },
  }));

  // 编辑时加载表单
  useEffect(() => {
    if (id) {
      formTemplateApi.detail(Number(id)).then((t) => {
        setName(t.name);
        setDescription(t.description || '');
        try {
          setFields(JSON.parse(t.schemaJson || '[]'));
        } catch { setFields([]); }
      }).catch(() => message.error('表单不存在'));
    }
  }, [id]);

  const handleDragEnd = useCallback((event: DragEndEvent) => {
    const { active, over } = event;
    if (!over) return;

    // 从控件面板拖入新字段
    if (active.data.current?.isNew) {
      const newField: FormField = {
        id: genId(),
        type: active.data.current.type,
        label: '新字段',
        name: `field_${fields.length + 1}`,
        required: false,
      };
      setFields(prev => [...prev, newField]);
      setSelectedId(newField.id);
      return;
    }

    // 画布内排序
    const oldIdx = fields.findIndex(f => f.id === active.id);
    const newIdx = fields.findIndex(f => f.id === over.id);
    if (oldIdx !== -1 && newIdx !== -1 && oldIdx !== newIdx) {
      setFields(prev => arrayMove(prev, oldIdx, newIdx));
    }
  }, [fields]);

  const selectedField = fields.find(f => f.id === selectedId) || null;

  const handleFieldUpdate = (updated: FormField) => {
    setFields(prev => prev.map(f => f.id === updated.id ? updated : f));
  };

  const handleSave = async () => {
    if (!name.trim()) { message.warning('请输入表单名称'); return; }
    const schema = JSON.stringify(fields);
    try {
      if (isEdit) {
        await formTemplateApi.update(Number(id), { name, description, schemaJson: schema });
        message.success('保存成功');
      } else {
        const created = await formTemplateApi.create({ name, description, schemaJson: schema });
        message.success('创建成功');
        navigate(`/forms/templates/${created.id}`, { replace: true });
      }
    } catch (err: any) {
      message.error(err.message || '保存失败');
    }
  };

  const handlePublish = async () => {
    if (!isEdit || !id) return;
    await handleSave();
    try {
      await formTemplateApi.publish(Number(id));
      message.success('发布成功');
      navigate('/forms/templates');
    } catch (err: any) {
      message.error(err.message || '发布失败');
    }
  };

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
        <Space>
          <Input placeholder="表单名称" value={name}
            onChange={e => setName(e.target.value)}
            style={{ width: 240 }} />
          <Input placeholder="表单描述" value={description}
            onChange={e => setDescription(e.target.value)}
            style={{ width: 240 }} />
        </Space>
        <Space>
          <Button icon={<SaveOutlined />} onClick={handleSave}>保存</Button>
          {isEdit && (
            <Button type="primary" icon={<SendOutlined />} onClick={handlePublish}>发布</Button>
          )}
        </Space>
      </Space>

      <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
        <SortableContext items={fields.map(f => f.id)} strategy={verticalListSortingStrategy}>
          <Row gutter={16}>
            <Col span={4}>
              <FieldPalette />
            </Col>
            <Col span={14}>
              <DesignCanvas
                fields={fields}
                selectedId={selectedId}
                onSelect={setSelectedId}
                onRemove={(fid) => {
                  setFields(prev => prev.filter(f => f.id !== fid));
                  if (selectedId === fid) setSelectedId(null);
                }}
              />
            </Col>
            <Col span={6}>
              <FieldConfigPanel field={selectedField} onUpdate={handleFieldUpdate} />
            </Col>
          </Row>
        </SortableContext>
      </DndContext>
    </div>
  );
};

export default FormDesignerPage;
```

- [ ] **Step 6: 验证编译**

```bash
cd /Users/fan/gitspace/demoReact/demo-frontend && npx tsc --noEmit
```

- [ ] **Step 7: 提交**

```bash
git add demo-frontend/src/pages/FormDesignerPage.tsx \
        demo-frontend/src/pages/FormDesignerPage/ \
        demo-frontend/src/types/
git commit -m "feat: 表单拖拽设计器 —— @dnd-kit 实现字段拖入/排序/属性编辑"
```

---

### Task 24: 表单列表与填报页面

**Files:**
- Create: `demo-frontend/src/pages/FormTemplateListPage.tsx`
- Create: `demo-frontend/src/pages/FormSubmitListPage.tsx`
- Create: `demo-frontend/src/pages/FormSubmitPage.tsx`
- Create: `demo-frontend/src/pages/MySubmissionsPage.tsx`
- Create: `demo-frontend/src/pages/SubmissionListPage.tsx`

- [ ] **Step 1: 创建 FormTemplateListPage**

```tsx
// src/pages/FormTemplateListPage.tsx
import { useState, useEffect } from 'react';
import { Table, Button, Space, Tag, Popconfirm, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, DownloadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { formTemplateApi, FormTemplate } from '../api/form';

const FormTemplateListPage: React.FC = () => {
  const [data, setData] = useState<FormTemplate[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const load = async () => {
    setLoading(true);
    try {
      const result = await formTemplateApi.listMine({ page: 1, size: 100 });
      setData(result.records);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async (id: number) => {
    await formTemplateApi.delete(id);
    message.success('已删除');
    load();
  };

  const handleExport = async (id: number) => {
    const blob = await formTemplateApi.exportCsv(id);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `data_${id}.csv`; a.click();
    URL.revokeObjectURL(url);
  };

  const statusColor: Record<string, string> = {
    DRAFT: 'default', PUBLISHED: 'green', DISABLED: 'red',
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '名称', dataIndex: 'name' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    {
      title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{s}</Tag>,
    },
    { title: '创建时间', dataIndex: 'createdAt', width: 180 },
    {
      title: '操作', width: 280, render: (_: any, record: FormTemplate) => (
        <Space>
          <Button size="small" icon={<EditOutlined />}
            onClick={() => navigate(`/forms/templates/${record.id}`)}>编辑</Button>
          <Button size="small" icon={<EyeOutlined />}
            onClick={() => navigate(`/forms/templates/${record.id}/submissions`)}>数据</Button>
          <Button size="small" icon={<DownloadOutlined />}
            onClick={() => handleExport(record.id)}>导出</Button>
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
        <h3>我的表单</h3>
        <Button type="primary" icon={<PlusOutlined />}
          onClick={() => navigate('/forms/templates/new')}>新建表单</Button>
      </Space>
      <Table columns={columns} dataSource={data} rowKey="id"
        loading={loading} pagination={false} />
    </div>
  );
};

export default FormTemplateListPage;
```

- [ ] **Step 2: 创建 FormSubmitListPage（可用表单列表）**

```tsx
// src/pages/FormSubmitListPage.tsx
import { useState, useEffect } from 'react';
import { Table, Button, Tag } from 'antd';
import { FileTextOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { formTemplateApi, FormTemplate } from '../api/form';

const FormSubmitListPage: React.FC = () => {
  const [data, setData] = useState<FormTemplate[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    formTemplateApi.listPublished({ page: 1, size: 100 })
      .then(r => setData(r.records))
      .finally(() => setLoading(false));
  }, []);

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '创建时间', dataIndex: 'createdAt', width: 180 },
    {
      title: '操作', width: 120, render: (_: any, record: FormTemplate) => (
        <Button type="primary" size="small" icon={<FileTextOutlined />}
          onClick={() => navigate(`/forms/submit/${record.id}`)}>填报</Button>
      ),
    },
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>可填报表单</h3>
      <Table columns={columns} dataSource={data} rowKey="id"
        loading={loading} pagination={false} />
    </div>
  );
};

export default FormSubmitListPage;
```

- [ ] **Step 3: 创建 FormSubmitPage（动态渲染表单填写）**

```tsx
// src/pages/FormSubmitPage.tsx
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Form, Input, InputNumber, DatePicker, Select, Radio, Checkbox, Upload, Button, message, Card } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { formTemplateApi, formSubmissionApi } from '../api/form';
import { FormField } from '../types/form';

const FormSubmitPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [template, setTemplate] = useState<any>(null);
  const [fields, setFields] = useState<FormField[]>([]);
  const [form] = Form.useForm();

  useEffect(() => {
    if (id) {
      formTemplateApi.detail(Number(id)).then(t => {
        setTemplate(t);
        try { setFields(JSON.parse(t.schemaJson || '[]')); }
        catch { setFields([]); }
      });
    }
  }, [id]);

  const onFinish = async (values: any) => {
    try {
      await formSubmissionApi.submit(Number(id), JSON.stringify(values));
      message.success('提交成功，待审批后生效');
      navigate('/forms/submit');
    } catch (err: any) {
      message.error(err.message || '提交失败');
    }
  };

  const renderField = (field: FormField) => {
    const rules = field.required ? [{ required: true, message: `请输入${field.label}` }] : [];
    switch (field.type) {
      case 'text': return <Input placeholder={field.placeholder} />;
      case 'textarea': return <Input.TextArea rows={3} placeholder={field.placeholder} />;
      case 'number': return <InputNumber style={{ width: '100%' }} placeholder={field.placeholder} />;
      case 'date': return <DatePicker style={{ width: '100%' }} />;
      case 'select': return <Select placeholder={field.placeholder} options={field.options?.map(o => ({ label: o, value: o }))} />;
      case 'radio': return <Radio.Group options={field.options?.map(o => ({ label: o, value: o }))} />;
      case 'checkbox': return <Checkbox.Group options={field.options?.map(o => ({ label: o, value: o }))} />;
      case 'file': return <Upload><Button icon={<UploadOutlined />}>上传文件</Button></Upload>;
      default: return <Input />;
    }
  };

  if (!template) return null;

  return (
    <Card title={`填报：${template.name}`} style={{ maxWidth: 720, margin: '0 auto' }}>
      <p style={{ color: '#666', marginBottom: 16 }}>{template.description}</p>
      <Form form={form} layout="vertical" onFinish={onFinish}>
        {fields.map(field => (
          <Form.Item key={field.id} name={field.name} label={field.label}
            rules={field.required ? [{ required: true, message: `${field.label}不能为空` }] : []}>
            {renderField(field)}
          </Form.Item>
        ))}
        <Form.Item>
          <Button type="primary" htmlType="submit">提交</Button>
          <Button style={{ marginLeft: 8 }} onClick={() => navigate(-1)}>返回</Button>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default FormSubmitPage;
```

- [ ] **Step 4: 创建 MySubmissionsPage**

```tsx
// src/pages/MySubmissionsPage.tsx
import { useState, useEffect } from 'react';
import { Table, Tag } from 'antd';
import { formSubmissionApi, FormSubmission } from '../api/form';

const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red',
};

const MySubmissionsPage: React.FC = () => {
  const [data, setData] = useState<FormSubmission[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    formSubmissionApi.listMy({ page: 1, size: 100 })
      .then(r => setData(r.records))
      .finally(() => setLoading(false));
  }, []);

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '表单模板ID', dataIndex: 'templateId', width: 100 },
    {
      title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{s}</Tag>,
    },
    { title: '提交时间', dataIndex: 'createdAt', width: 180 },
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>我的填报记录</h3>
      <Table columns={columns} dataSource={data} rowKey="id"
        loading={loading} pagination={false} />
    </div>
  );
};

export default MySubmissionsPage;
```

- [ ] **Step 5: 创建 SubmissionListPage（查看某表单的所有填报数据）**

```tsx
// src/pages/SubmissionListPage.tsx
import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Table, Tag, Button } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { formSubmissionApi, formTemplateApi, FormSubmission } from '../api/form';

const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red',
};

const SubmissionListPage: React.FC = () => {
  const { id } = useParams();
  const [data, setData] = useState<FormSubmission[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (id) {
      setLoading(true);
      formSubmissionApi.listByTemplate(Number(id), { page: 1, size: 100 })
        .then(r => setData(r.records))
        .finally(() => setLoading(false));
    }
  }, [id]);

  const handleExport = async () => {
    if (!id) return;
    const blob = await formSubmissionApi.exportCsv(Number(id));
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `submissions_${id}.csv`; a.click();
    URL.revokeObjectURL(url);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '提交者ID', dataIndex: 'submitterId', width: 100 },
    {
      title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{s}</Tag>,
    },
    { title: '审批人ID', dataIndex: 'approverId', width: 100 },
    { title: '审批时间', dataIndex: 'approvedAt', width: 180 },
    { title: '提交时间', dataIndex: 'createdAt', width: 180 },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h3>填报数据</h3>
        <Button icon={<DownloadOutlined />} onClick={handleExport}>导出 CSV</Button>
      </div>
      <Table columns={columns} dataSource={data} rowKey="id"
        loading={loading} pagination={false} />
    </div>
  );
};

export default SubmissionListPage;
```

- [ ] **Step 6: 验证编译**

```bash
cd /Users/fan/gitspace/demoReact/demo-frontend && npx tsc --noEmit
```

- [ ] **Step 7: 提交**

```bash
git add demo-frontend/src/pages/FormTemplateListPage.tsx \
        demo-frontend/src/pages/FormSubmitListPage.tsx \
        demo-frontend/src/pages/FormSubmitPage.tsx \
        demo-frontend/src/pages/MySubmissionsPage.tsx \
        demo-frontend/src/pages/SubmissionListPage.tsx
git commit -m "feat: 表单列表、填报、数据查看页面

包含表单CRUD列表、已发布表单填报入口、动态表单渲染、填报记录查看。"
```

---

### Task 25: 审批和用户管理页面

**Files:**
- Create: `demo-frontend/src/pages/ApprovalPage.tsx`
- Create: `demo-frontend/src/pages/UserManagementPage.tsx`

- [ ] **Step 1: 创建 ApprovalPage**

```tsx
// src/pages/ApprovalPage.tsx
import { useState, useEffect } from 'react';
import { Table, Button, Space, message, Modal, Input } from 'antd';
import { CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { approvalApi, TaskDto } from '../api/approval';

const ApprovalPage: React.FC = () => {
  const [data, setData] = useState<TaskDto[]>([]);
  const [loading, setLoading] = useState(false);

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
      load();
    } catch (err: any) { message.error(err.message || '操作失败'); }
  };

  const handleReject = async (submissionId: number) => {
    Modal.confirm({
      title: '驳回原因',
      content: <Input.TextArea id="reject-reason" rows={2} placeholder="可选填写驳回原因" />,
      onOk: async () => {
        const reason = (document.getElementById('reject-reason') as HTMLTextAreaElement)?.value;
        await approvalApi.reject(submissionId, reason);
        message.success('已驳回');
        load();
      },
    });
  };

  const columns = [
    { title: '业务编号', dataIndex: 'processInstanceId', width: 120 },
    { title: '任务名称', dataIndex: 'name' },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
    {
      title: '操作', width: 180, render: (_: any, record: TaskDto) => (
        <Space>
          <Button type="primary" size="small" icon={<CheckOutlined />}
            onClick={() => handleApprove(Number(record.processInstanceId))}>批准</Button>
          <Button danger size="small" icon={<CloseOutlined />}
            onClick={() => handleReject(Number(record.processInstanceId))}>驳回</Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>待审批列表</h3>
      <Table columns={columns} dataSource={data} rowKey="taskId"
        loading={loading} pagination={false} />
    </div>
  );
};

export default ApprovalPage;
```

- [ ] **Step 2: 创建 UserManagementPage**

```tsx
// src/pages/UserManagementPage.tsx
import { useState, useEffect } from 'react';
import { Table, Button, Tag, Space, Modal, Select, message, Popconfirm } from 'antd';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { userApi, UserVO } from '../api/user';

const UserManagementPage: React.FC = () => {
  const [data, setData] = useState<UserVO[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const result = await userApi.list({ page, size: 10 });
      setData(result.records);
      setTotal(result.total);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [page]);

  const handleDelete = async (id: number) => {
    await userApi.delete(id);
    message.success('已删除');
    load();
  };

  const handleAssignRoles = (user: UserVO) => {
    Modal.confirm({
      title: `分配角色 - ${user.username}`,
      content: (
        <Select mode="multiple" style={{ width: '100%' }} defaultValue={user.roles}
          options={[
            { label: '管理员', value: 'ROLE_ADMIN' },
            { label: '特权用户', value: 'ROLE_PRIVILEGED' },
            { label: '普通用户', value: 'ROLE_USER' },
          ]}
          onChange={(values: string[]) => {
            // 角色编码映射为 roleId (1=ADMIN, 2=PRIVILEGED, 3=USER)
            const roleMap: Record<string, number> = {
              ROLE_ADMIN: 1, ROLE_PRIVILEGED: 2, ROLE_USER: 3,
            };
            const roleIds = values.map(v => roleMap[v]).filter(Boolean);
            userApi.assignRoles(user.id, roleIds).then(() => load());
          }}
        />
      ),
      onOk: () => message.success('已更新'),
    });
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '用户名', dataIndex: 'username' },
    { title: '邮箱', dataIndex: 'email' },
    {
      title: '角色', dataIndex: 'roles', render: (roles: string[]) => (
        <>
          {roles?.map(r => <Tag key={r} color="blue">{r}</Tag>)}
        </>
      ),
    },
    {
      title: '状态', dataIndex: 'status', width: 80,
      render: (s: number) => s === 1 ? <Tag color="green">正常</Tag> : <Tag color="red">禁用</Tag>,
    },
    { title: '创建时间', dataIndex: 'createdAt', width: 180 },
    {
      title: '操作', width: 180, render: (_: any, record: UserVO) => (
        <Space>
          <Button size="small" onClick={() => handleAssignRoles(record)}>角色</Button>
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>用户管理</h3>
      <Table columns={columns} dataSource={data} rowKey="id"
        loading={loading}
        pagination={{ total, current: page, onChange: setPage }} />
    </div>
  );
};

export default UserManagementPage;
```

- [ ] **Step 3: 验证前端编译**

```bash
cd /Users/fan/gitspace/demoReact/demo-frontend && npx tsc --noEmit
```

- [ ] **Step 4: 提交**

```bash
git add demo-frontend/src/pages/ApprovalPage.tsx \
        demo-frontend/src/pages/UserManagementPage.tsx
git commit -m "feat: 审批页面和用户管理页面

特权用户可批准/驳回数据，管理员可查看/删除用户/分配角色。"
```

---

## Phase 6: 收尾

### Task 26: README.md 编写

**Files:**
- Create: `README.md`

- [ ] **Step 1: 创建 README.md**

写入完整设计文档内容（从 `docs/superpowers/specs/2026-05-06-form-platform-design.md` 提取摘要，含项目结构、技术栈、开发指南、API 概览）。

- [ ] **Step 2: 提交**

```bash
git add README.md
git commit -m "docs: 项目 README —— 架构说明、技术栈、开发指南"
```

---

### Task 27: E2E 测试脚本（Playwright）

**Files:**
- Create: `demo-frontend/e2e/login.spec.ts`

- [ ] **Step 1: 创建 E2E 测试**

```typescript
// e2e/login.spec.ts
import { test, expect } from '@playwright/test';

test.describe('表单数据平台 E2E', () => {
  test('管理员登录并创建表单', async ({ page }) => {
    await page.goto('http://localhost:5173/login');
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/');
    await expect(page.locator('text=欢迎，admin')).toBeVisible();
  });
});
```

- [ ] **Step 2: 提交**

```bash
git add demo-frontend/e2e/
git commit -m "test: 添加 E2E 测试脚本 —— 登录流程"
```

---

### Task 28: 最终验证与编译

- [ ] **Step 1: 全量后端编译**

```bash
cd /Users/fan/gitspace/demoReact && mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 运行后端测试**

```bash
cd /Users/fan/gitspace/demoReact && mvn test
```

Expected: 所有测试通过

- [ ] **Step 3: 前端类型检查**

```bash
cd /Users/fan/gitspace/demoReact/demo-frontend && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 4: 端到端启动验证**

```bash
# 终端 1：启动后端
cd /Users/fan/gitspace/demoReact/demo-web && mvn spring-boot:run

# 终端 2：启动前端
cd /Users/fan/gitspace/demoReact/demo-frontend && npm run dev

# 浏览器验证：
# 1. 访问 http://localhost:5173 → 跳转登录页
# 2. 输入 admin/admin 登录 → 进入首页
# 3. 创建表单 → 拖入字段 → 保存 → 发布
# 4. 使用普通用户登录 → 进入填报入口 → 填写表单 → 提交
# 5. 使用特权用户登录 → 审批待办 → 批准/驳回
```

- [ ] **Step 5: 最终提交**

```bash
git add -A
git commit -m "chore: 最终验证 —— 全量编译通过、前后端联调完成"
```

---

## 实施总结

| 阶段 | 任务数 | 说明 |
|------|--------|------|
| Phase 1: 脚手架 | 3 | Maven 多模块 + Spring Boot + Common |
| Phase 2: 用户与认证 | 7 | 实体/Mapper/Service/Controller + JWT + Security |
| Phase 3: 表单引擎 | 4 | 模板/填报实体 + Service + Controller |
| Phase 4: 流程引擎 | 3 | Camunda 7 + BPMN + 审批 Service |
| Phase 5: 前端 | 7 | React + Ant Design + 拖拽设计器 + 全页面 |
| Phase 6: 收尾 | 3 | README + E2E + 最终验证 |
| **合计** | **27** | 约 120 个步骤 |

