# 学程智管（StudyPath Smart）

> 适用：项目立项文档、开发交付说明、产品对外简介、开发团队内部文档
> 业态适配：少儿体能、艺术培训、素质教育培训机构

## 目录

1. 项目简介
2. 系统定位与核心价值
3. 系统架构与终端组成
4. 角色体系与权限总览
5. 核心业务模块详细说明
6. 关键业务规则设计
7. 数据导入/导出能力
8. 安全与审计机制
9. 网络与域名部署说明
10. 后续扩展方向
11. 附录 A：全链路测试执行 Prompt 汇总

***

## 1. 项目简介

**系统名称**：学程智管
**英文名称**：StudyPath Smart
**产品Slogan**：智管课程进程，留存学员成长档案

学程智管是面向素质教育培训机构打造的**一体化教务管理微信小程序系统**，配套PC管理后台。
系统同时覆盖两大核心业务：
✅ **排课教务 + 课时台账管理**
✅ **学员体测/体检自定义模板 + 自动生成测评报告**

支持三类用户：**管理员（PC后台）、教师（微信小程序）、家长（微信小程序）**，实现家校数据互通、上课全流程追溯、学员成长数据长期存档。

> 典型场景：
> 老师线上排课、签到上课、自动消耗课时；家长随时查看子女课表、剩余课时与体测报告；机构批量导入学员、排课、体测原始数据，统一导出运营报表。

## 2. 系统定位与核心价值

### 定位

轻量化教务数字化平台，打通**排课管理、课时管控、学员成长测评**，以微信小程序为载体，无需家长、教师安装APP。

### 核心价值

1. **教务线上化**：线上排课、调课、签到、课时流水，告别纸质台账；
2. **课时精细化管控**：每一笔课时变动永久留痕，支持充值、消耗、人工调整、撤销；
3. **成长档案数字化**：支持自定义体测模板，导入数据自动生成报告，长期保存学员体测记录；
4. **家校透明互通**：家长实时查看课表、上课状态、课时余额、体测报告；
5. **批量数据处理**：Excel批量导入导出，大幅减少人工录入工作量。

## 3. 系统架构与终端组成

整套系统分为三端：

1. **PC管理后台（管理员入口）**
   面向运营管理员，负责账号管理、系统维护、大批量数据运维；
2. **微信小程序（教师端）**
   授课老师日常业务操作入口：排课、签到、课时管理、体测录入；
3. **微信小程序（家长端）**
   家长查询入口：仅查看绑定子女相关数据，无任何修改权限。

**技术架构：前后端分离**

- 后端：提供统一Restful API，按角色划分接口权限
- 前端：PC后台管理系统 + 微信小程序（教师/家长共用小程序，登录后区分角色）
- 数据库：关系型数据库存储学员、排课、课时、体测、报告数据

## 4. 角色体系与权限总览

系统内置三种固定角色，首期采用简易角色模型（暂不引入复杂RBAC）：
`ADMIN（管理员）` / `TEACHER（教师）` / `PARENT（家长）`

### 4.1 管理员（PC管理后台）

核心定位：**系统账号管理者 + 全局数据运维人员**

- 学员、教师、家长账号全量管理；家长与学员绑定关系维护
- 课程、班级、排课、课时、体测全业务操作权限
- Excel批量导入导出、报表导出
- 配置体测模板、管理体测报告
- 查询**全部用户操作日志**

> 限制：无限制业务操作，负责账号体系维护。

### 4.2 教师（微信小程序）

核心定位：**一线教学业务操作人员**

> ✅ 业务模块权限与管理员保持一致：课程/班级/排课/课时/体测/导入导出均可操作
> ❌ 无权限管理教师账号、家长账号、无法查看全局操作日志

主要能力：

- 管理学员基础信息、新增/修改学员；
- 新增、修改、调课、取消排课；课程签到、标记课程完成；登记请假、缺勤；
- 课时充值、扣减、手动调整（调整原因必填）；查看学员课时流水；
- 配置体测模板、录入体测数据、生成/重生成体测报告；
- Excel导入学员、排课、体测数据；导出各类业务报表；
- 仅可查看**本人操作日志**。

### 4.3 家长（微信小程序）

核心定位：**信息查询角色，仅访问绑定子女数据，无任何数据修改权限**

- 查看孩子课表、课程详情、签到与上课完成状态；
- 查看剩余课时、课时消费流水、历史上课记录；
- 查看学员全部历史体测数据；在线预览、保存、分享体测报告；
- 支持一名家长绑定多名学员；
- 不可录入、修改任何业务数据。

### 权限矩阵总表

| 功能          | 管理员          | 教师            | 家长       |
| ----------- | ------------ | ------------- | -------- |
| 学员管理        | 增删改查全部学员     | 新增/修改/查看学员    | 仅查看绑定子女  |
| 教师/家长账号管理   | 完整管理权限       | 仅查看信息，不可新增/删除 | 仅查看本人信息  |
| 课程/班级管理     | 增删改查         | 增删改查          | 查看子女所属信息 |
| 排课、调课、取消课程  | 支持全部操作       | 支持全部操作        | 仅查看子女课表  |
| 课程签到、上课状态登记 | 可操作全部课程      | 可操作课程         | 查看结果     |
| 课时充值、扣减、调整  | 允许操作         | 允许操作          | 不可操作     |
| 课时流水查询      | 查看全量流水       | 查看相关学员流水      | 仅查看子女流水  |
| 体测模板配置      | 新增/修改/启停     | 新增/修改/启停      | 无权限      |
| 体测数据录入与修改   | 支持单条/批量导入    | 支持单条/批量导入     | 不可操作     |
| 体测报告        | 生成、重生成、删除、查看 | 生成、重生成、删除、查看  | 查看、保存、分享 |
| Excel导入导出   | 全部支持         | 全部支持          | 不可操作     |
| 操作日志        | 查看系统全部日志     | 仅查看本人操作记录     | 无权限      |
| 系统账号维护      | ✅            | ❌             | ❌        |

## 5. 核心业务模块详细说明

### 模块一：基础数据管理

1. **学员管理**：学员档案维护，支持绑定家长、归属教师；
2. **教师管理**：教师账号维护；
3. **家长管理**：家长账号，支持一对多绑定学员；
4. **课程管理**：维护课程名称、时长、课程类型；
5. **班级管理**：班级信息、学员分班管理。

### 模块二：排课与上课管理

1. 新增排课、修改排课、调课、取消课程；
2. 课程签到、课程完成确认；
3. 请假、缺勤、课程取消状态登记；
4. 课表展示：支持日历/周视图；
5. 家长端自动展示子女个人课表。

### 模块三：课时台账管理（核心业务）

完整课时流水追溯机制，**禁止直接覆盖余额，所有变动生成流水记录**。
课时变动类型：

- `RECHARGE` 课时充值
- `CONSUME` 上课自动扣减
- `REFUND` 课时退回
- `ADJUST` 人工手动调整（强制填写调整原因）
- `ROLLBACK` 撤销上课扣减
- `IMPORT` 历史数据批量导入

业务规则：

- 课程标记【完成上课】自动生成消耗流水；
- 已完成课程禁止重复扣减课时；
- 取消已上完课程支持课时回滚；
- 教师/管理员手动调整课时，界面记录：调整前余额、变动数量、调整后余额、操作原因、操作人。

### 模块四：体测&体检报告管理

1. **自定义体测模板**
   后台/教师可创建测评模板，自由配置测评项目（身高、体重、BMI、立定跳远、肺活量等）、单位、参考标准区间；
2. **体测数据录入**
   支持小程序单学员录入、后台Excel批量导入；
3. **报告自动生成**
   基于选择的模板 + 学员体测数据，自动渲染体检/体测报告；支持重新生成，历史报告版本保留；
4. **报告预览与分发**
   家长小程序在线查看、保存图片至手机、微信转发。

### 模块五：数据导入导出

支持Excel模板标准化导入、业务报表导出：
**导入清单**
学员数据、课程数据、班级成员、排课计划、历史课时、体测数据
**导出清单**
学员名单、排课计划表、上课记录、课时流水、课时余额汇总、体测原始数据、体测汇总、报告列表

### 模块六：操作日志审计

记录关键操作：账号变更、课时调整、排课变更、体测数据修改、Excel导入记录；永久留存，用于业务追溯与责任核查。

## 6. 关键业务控制规则

1. 课时人工调整**调整原因必填**；
2. 删除课程、取消已完成课程弹出二次确认弹窗；
3. 已完成课程不能重复扣减课时；撤销上课自动生成反向课时流水；
4. Excel导入先执行数据预校验，展示冲突数据，确认后再入库；
5. 学员、课程、体测相关业务数据采用**软删除**，防止误删无法恢复；
6. 体测报告重新生成，保留历史报告记录，不直接覆盖旧报告；
7. 家长访问接口后端强制做数据权限过滤，**不能仅依靠前端隐藏页面**；
8. 教师只能查看自身操作日志；管理员可查看全系统日志。

***

## 附录 A：SP-Smart 全链路测试执行 Prompt 汇总

> 以下为可直接供 AI Agent 复现的标准化测试 Prompt，按优先级分级，覆盖 P0 Bug 修复 → 资金链路回归 → 并发/安全基线 → UI/性能 → 单元测试 共 7 大阶段，实际执行结果见"验证结果"。

---

### 🔴 P0 级：核心 Bug 修复与验证

#### A.1 修复 4 个 P0 级 Bug (代码修复 Prompt)

> **Prompt**: 
> 请修复 `ScheduleCompletionService`、`HourApplicationService`、`GlobalExceptionHandler` 中的 4 个 P0 级 Bug：
> 1. **Bug A（消课课时漏拷）**：在创建 `LessonStudent` 时，显式调用 `setConsumeHours(lesson.getConsumeHours())` 将课时从课程同步到学员排课记录，防止默认值 0 导致的"零消课但标记已消课"。
> 2. **Bug B（0 课时死锁）**：在 `completeLessonWithAutoConsume` 中增加 `if (consumeAmount.compareTo(BigDecimal.ZERO) > 0)` 判断，仅在课时 > 0 时执行扣减与 `shouldConsume=true` 设置。
> 3. **Bug C（事务 ID 关联错误）**：`consume` 返回 `HourTransaction` 对象，在 `completeLessonWithAutoConsume` 中将 `transaction.getId()` 赋给 `lessonStudent.setHourTransactionId`（不要使用账户 ID），确保退款链路能通过流水 ID 回溯。
> 4. **Bug D（异常码不合规）**：在 `GlobalExceptionHandler` 中新增 `@ExceptionHandler(BusinessException.class)`，根据 `ErrorCode.resolveHttpStatus()` 映射为 HTTP 400/401/403/409，禁止业务异常包装为 500。

**验证结果**：4 个 Bug 修复，T2 消课链路 25/25 PASS。

---

#### A.2 消课全链路 E2E 回归 (验证 Prompt)

> **Prompt**:
> 请编写 Node.js 脚本 `/tmp/t2_hours_consume_v2.js`，按"DB ↔ API 双查"闭环执行：
> 1. **准备数据**：admin 登录 → 为学员 `SID=42` 充值 10 课时，DB 直查 `student_hour_account.remaining_hours=10`。
> 2. **排课**：创建 ONE_TO_ONE 排课 (courseId=2, 2 课时)，DB 直查 `lesson_student.consume_hours=2.00`。
> 3. **签到→消课**：标记学员 SID=42 为 PRESENT，然后 `completeLessonWithAutoConsume`，期望：
>    - DB：`remaining_hours=8`，`lesson_student.should_consume=1`，`lesson_student.hour_transaction_id = hour_transaction.id`
>    - API：学员余额接口返回 8。
> 4. **退款**：`cancelLessonWithRefund` → 余额恢复为 10，生成 REFUND 流水。
> 5. **幂等性**：再次 `complete` 已取消课程 → HTTP 400/409 错误码。

**验证结果**：全链路 25/25 PASS，取消课程成功回滚课时。

---

### 🟠 P1 级：关键业务与安全验证

#### A.3 并发冲突与幂等性测试

> **Prompt**:
> 编写并发脚本 `/tmp/t3_concurrency_fixed.js` 或使用 `k6`，验证：
> 1. **并发考勤**：同一节课同一学员并发 3 次 PRESENT 请求，`lesson_student` 最终只写入 1 条（或只更新 1 次）。
> 2. **并发消课**：并发 2 次 `completeLessonWithAutoConsume`，`student_hour_account` 只扣减 1 次，`hour_transaction` 仅生成 1 条 CONSUME 流水（version 乐观锁生效）。
> 3. **异常捕获**：第二次请求因第一次已完成应抛出 `OPERATION_NOT_ALLOWED`，HTTP 状态码必须为 400/409（非 500）。

**验证结果**：T3 并发 16/16 PASS，乐观锁生效。

---

#### A.4 T4 安全基线 (JWT + IDOR)

> **Prompt**:
> 编写脚本 `/tmp/t4_security_baseline.js` 覆盖 21 条检查：
> 1. **JWT 格式**：`Authorization: <token>`（无 Bearer 前缀）→ HTTP 401；
> 2. **Token 篡改**：Payload/Signature 换一个字符 → HTTP 401 `Token Invalid`；
> 3. **IDOR（越权）**：
>    - 家长访问非亲生子女 (`SID≠绑定SID`) 的考勤/体测写入 → HTTP 403；
>    - 教师访问 `/api/admin/**` 管理接口 → HTTP 403；
>    - 家长尝试修改非亲生子女课时 → HTTP 403；
>    - ADMIN 正确绕过 `ParentPermissionService.canAccessStudent` 校验。

**验证结果**：T4 21/21 PASS，`JwtAuthenticationFilter` 和 `ParentPermissionService` 生效。

---

### 🔵 P2 级：UI/UX 与性能测试

#### A.5 家长端/管理端 UI 数据真实性验证 (Browser E2E)

> **Prompt**:
> 使用 Browser Agent，按"DB ↔ API ↔ UI"三段式闭环验证：
> 1. **家长端 H5 小程序 (http://localhost:3030)**：
>    - 清理全部 Storage + 强刷后，登录 `parent01`；
>    - 进入「我的课时」页面，查看余额与流水条目，与接口返回、DB 直查三端相等；
>    - 确认页面**不是**纯前端 Mock（Network 面板出现 `/api/parent/**` 请求，响应码 200）。
> 2. **管理端 Web (http://localhost:3031)**：
>    - 学员档案详情页的课时余额，与 API + DB 对比；
>    - 排课管理页面展示的课程及学员信息与 DB 一致（含状态/课时字段）。

**验证结果**：T5 三端数据完全一致，排除 Mock/伪渲染。

---

#### A.6 T6 性能基准测试 (开学季场景)

> **Prompt**:
> 请使用 `k6` 执行性能基准测试，脚本存放到 `/tmp/perf_test.js`：
> 1. **场景**：开学季高并发；
> 2. **压测接口**：
>    - `POST /api/auth/login`（20 VU，多用户轮询）
>    - `GET /api/business/students`（50 VU，ADMIN/TEACHER 账户池）
>    - `POST /api/business/schedules`（10 VU，courseId 必须是数据库真实存在的 ID）
> 3. **并发修复优化**：
>    - 登录 `sys_user.last_login_at` 更新改为轻量 SQL（`@Update UPDATE sys_user SET last_login_at=NOW(), updated_at=NOW() WHERE id=?`），并用 try-catch 吞掉乐观锁冲突。
> 4. **目标**：`p95 latency < 500ms`，`http_req_failed rate < 0.1%`。
> 5. **阈值配置**：
>    - `http_req_duration p(95)<500`
>    - `http_req_failed rate<0.001`
>    - 三个场景各单独的自定义 Trend `p(95)<500`

**验证结果**：
| 指标 | 登录 | 学生列表 | 排课 | 阈值 |
|------|------|------|------|------|
| p95 Latency | 273ms | 146ms | 148ms | <500ms ✅ |
| Error Rate | 0.00% | 0.00% | 0.00% | <0.1% ✅ |

---

### 🟣 开发态：单元测试与 Jacoco 覆盖率

#### A.7 JUnit + Mockito + Jacoco 单测

> **Prompt**:
> 请为以下 3 个核心类编写 JUnit 5 + Mockito 单元测试，并在 `pom.xml` 配置 `jacoco-maven-plugin` 强制 BRANCH 覆盖率 ≥ 80%：
> 1. **`HourApplicationService`**：
>    - `consume`：余额充足扣减（EXPERIENCE→ACTIVITY→GIFT→PAID 优先级）；余额不足抛 `HOUR_INSUFFICIENT`；账户不存在抛 `STUDENT_NOT_FOUND`；`allowNegativeBalance=true` 负数允许；`MybatisPlusException` 乐观锁转换为 `OPERATION_NOT_ALLOWED`；零金额正常返回。
>    - `refund` / `refundByTransaction`：PAID/GIFT/EXPERIENCE/ACTIVITY/NULL 五种来源分别进入对应桶；流水不存在、非 CONSUME、已被退回等抛 `HOUR_TRANSACTION_FAILED`；乐观锁冲突转换异常。
>    - `recharge`：5 种 hourSource switch（PAID null 默认、EXPERIENCE、ACTIVITY、GIFT、UNKNOWN 兜底 PAID）；已有账户 vs 新账户；expireDate 在非 PAID 时置空；reason 为空走默认值。
>    - `listAccounts`：分页参数归一化（pageNum<1→1，pageSize>500→500，studentId=null 跳过，id=null fallback studentId）。
> 2. **`ScheduleCompletionService`**：
>    - `completeLessonWithAutoConsume`：PENDING/PRESENT 正常消课（lessonStudent consumeHours > lesson > ONE 三级 fallback）；0 课时跳过；shouldConsume=true 不重复消课；非 PRESENT 不处理；`isCompleted/isCancelled` 抛 OPERATION_NOT_ALLOWED；`MybatisPlusException`/`DataAccessException` 两种冲突异常各走分支；lesson 不存在抛 `LESSON_NOT_FOUND`。
>    - `cancelLessonWithRefund`：已消课学员触发退回，shouldConsume=false 不退回；已取消拦截；乐观锁/DAO 冲突异常。
> 3. **`AuthenticationController`**：
>    - `login`：成功（admin/teacher/parent 各角色字段正确返回）、密码错 (`AUTH_INVALID_CREDENTIALS`)、账号禁用 (`AUTH_ACCOUNT_DISABLED`)。
>    - `loginByWechat`、`refreshToken`（有效/无效）、`getCurrentUser`（有 user/null 抛 401）。
> 4. **Jacoco 配置**：
>    ```xml
>    <plugin>
>      <groupId>org.jacoco</groupId><artifactId>jacoco-maven-plugin</artifactId><version>0.8.11</version>
>      <executions>
>        <execution><id>prepare-agent</id><goals><goal>prepare-agent</goal></goals></execution>
>        <execution><id>report</id><phase>test</phase><goals><goal>report</goal></goals></execution>
>        <execution><id>jacoco-check</id><goals><goal>check</goal></goals>
>          <configuration><rules><rule><element>CLASS</element>
>            <includes>
>              <include>com.example.training.hour.application.service.HourApplicationService</include>
>              <include>com.example.training.schedule.application.service.ScheduleCompletionService</include>
>              <include>com.example.training.auth.controller.AuthenticationController</include>
>            </includes>
>            <limits><limit><counter>BRANCH</counter><value>COVEREDRATIO</value><minimum>0.80</minimum></limit></limits>
>          </rule></rules></configuration>
>        </execution>
>      </executions>
>    </plugin>
>    ```
> 5. **测试方法**：手动 `@Mock` + `@InjectMocks` + `@ExtendWith(MockitoExtension.class)`，用 `ReflectionTestUtils.setField` 设置 `@Value` 的 allowNegativeBalance 等字段，**避免启动 Spring 上下文**。

**验证结果**：59/59 用例通过，Jacoco check `All coverage checks have been met`：

| 类 | 指令覆盖 | 分支覆盖 |
|---|---|---|
| HourApplicationService | 99% | **91%** |
| ScheduleCompletionService | 100% | **95%** |
| AuthenticationController | 100% | **100%** |

---

## 10. 后续扩展方向

- **单测补充（P1，可选）**：`AuthenticationService`（密码校验 + 微信登录建用户）、`GlobalExceptionHandler`（HTTP 状态码映射）、`JwtAuthenticationFilter`（JWT 格式）、`ParentPermissionService`（IDOR 数据权限）。
- **集成测试（P2）**：用 `@SpringBootTest` + H2 + 手动 `ExecutorService` 验证乐观锁真实并发回滚行为、`@Valid` 参数校验返回的 HTTP 400 结构。
- **E2E 自动回归（P3）**：将 T2/T3/T4/T5/T6 的脚本模板固化为 CI 中可一键执行的命令，形成"资金链路 ↔ 安全基线 ↔ 性能阈值"三段式强制门禁。

