# 卡券商城 (Kaquan Mall)

> Vue3 前端 + Spring Boot 后端 — 优惠券/卡券权益在线商城

## 项目结构

```
kaquan-mall/
├── kaquan/                # 前端（Vue3 + Vite）
│   └── src/views/         # 页面：首页/分类/商品/订单/充值/后台
└── kaquan-server/         # 后端（Spring Boot + MyBatis）
    └── src/main/java/     # 分层：controller/service/mapper/pojo
```

---

## 环境要求

| 工具 | 版本要求 | 如何检查 |
|------|---------|---------|
| **Git** | 任意 | `git --version` |
| **Node.js** | ≥ 20.19 | `node -v` |
| **Java JDK** | 17 | `java -version` |
| **MySQL** | 8.0+ | `mysql --version` |
| **Redis** | 7.0+ | `redis-cli ping` |

> Redis 可选——项目配置了 Redis 缓存，没有也能跑，但签到等功能会报错。

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/ymzwp/kaquan-mall.git
cd kaquan-mall
```

### 2. 初始化数据库

打开 MySQL，执行建表脚本：

```sql
source kaquan-server/src/main/resources/schema.sql
```

这会自动创建 `coupon_mall` 数据库和全部 11 张表。

### 3. 配置数据库密码

编辑 `kaquan-server/src/main/resources/application.yml`，改成你的 MySQL 密码：

```yaml
spring:
  datasource:
    username: root        # 改成你的
    password: 1234        # 改成你的
```

### 4. 启动后端

```bash
cd kaquan-server

# Windows
mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```

> 首次运行会自动下载 Maven，需要几分钟。  
> 启动成功看到 `Started CouponBackApplication` → 后端运行在 **http://localhost:8088**

### 5. 启动前端

打开新终端：

```bash
cd kaquan
npm install              # 首次需安装依赖
npm run dev              # 启动开发服务器
```

浏览器打开 **http://localhost:3000**

---

## 功能模块

| 模块 | 前端页面 | 后端接口 |
|------|---------|---------|
| 首页/分类 | `Index.vue` `Cate.vue` | `UserController` `CateController` |
| 商品浏览 | `GoodsDetail.vue` | `GoodsController` |
| 下单支付 | `ConfirmOrder.vue` `Order.vue` | `OrderController` |
| 余额充值 | `Recharge.vue` | `RechargeController` |
| 优惠券 | — | `CouponController` |
| 签到 | `Mine.vue` | `SignService` |
| 用户中心 | `Login.vue` `Mine.vue` | `UserController` |
| 后台管理 | `admin/AdminIndex.vue` | `AdminController` |
| 数据统计 | — | `TrendController` |
| 系统通知 | — | `NotifyController` |

## 技术栈

**前端**：Vue 3 · Vue Router · Element Plus · ECharts · Axios · Vite

**后端**：Spring Boot 4.0 · MyBatis · Druid · Redis · BCrypt · MySQL

---

## 常见问题

**Q: 前端页面空白 / 接口报错？**  
确保后端已启动（`:8088`），刷新页面。

**Q: 数据库连接失败？**  
检查 `application.yml` 里的数据库地址、用户名、密码是否正确。

**Q: 提示"表不存在"？**  
可能是没执行建表脚本，在 MySQL 里运行 `source kaquan-server/src/main/resources/schema.sql`。

**Q: Redis 连接失败？**  
安装 Redis 并启动，或者暂时注释掉 `application.yml` 里的 Redis 配置。
