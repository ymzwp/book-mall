# 卡券商城 (Kaquan Mall)

> **Vue3 + Spring Boot** 全栈项目 —— 优惠券/卡券权益在线商城  
> 支持商品浏览、下单支付、余额充值、会员体系、优惠券、签到、后台管理

---

## 项目结构

```
kaquan-mall/
├── kaquan/                        # 前端（Vue3 + Vite + Element Plus）
│   ├── src/
│   │   ├── router/index.js        # 路由配置 + 登录守卫
│   │   ├── utils/http.js          # Axios 封装（统一请求拦截）
│   │   ├── views/                 # 10个页面组件
│   │   │   ├── Index.vue          # 首页
│   │   │   ├── Login.vue          # 用户登录/注册
│   │   │   ├── Cate.vue           # 商品分类
│   │   │   ├── GoodsDetail.vue    # 商品详情
│   │   │   ├── ConfirmOrder.vue   # 确认订单
│   │   │   ├── Order.vue          # 我的订单（可退款）
│   │   │   ├── Mine.vue           # 个人中心（签到/会员/邀请）
│   │   │   ├── Recharge.vue       # 余额充值
│   │   │   └── admin/
│   │   │       ├── AdminLogin.vue # 后台登录
│   │   │       └── AdminIndex.vue # 后台管理面板
│   │   └── assets/                # 样式文件
│   ├── package.json               # 依赖声明
│   └── vite.config.js             # Vite 构建配置
│
├── kaquan-server/                 # 后端（Spring Boot + MyBatis + Redis）
│   ├── pom.xml                    # Maven 依赖（Spring Boot 4.0）
│   ├── mvnw / mvnw.cmd           # Maven Wrapper（无需手动安装 Maven）
│   └── src/main/
│       ├── java/cn/yunmeng/book/
│       │   ├── CouponBackApplication.java   # 启动入口
│       │   ├── controller/                  # 控制器层（9个，47个接口）
│       │   ├── service/                     # 服务层接口 + impl/
│       │   ├── mapper/                      # 数据访问层（10个 DAO）
│       │   ├── pojo/                        # 实体类（9个）
│       │   └── config/                      # 配置（安全/Redis/限流/CORS）
│       └── resources/
│           ├── application.yml              # 主配置文件
│           ├── schema.sql                   # 数据库建表脚本（11张表）
│           └── mapper/                      # MyBatis XML 映射文件
└── README.md
```

---

## 环境要求

### 必须安装

| 工具 | 版本 | 用途 | 安装方式 |
|------|------|------|---------|
| **Git** | 任意 | 克隆代码 | `winget install Git.Git` 或 [git-scm.com](https://git-scm.com) |
| **Node.js** | ≥20.19 | 运行前端 | `winget install OpenJS.NodeJS.LTS` 或 [nodejs.org](https://nodejs.org) |
| **Java JDK** | 17 | 运行后端 | `winget install EclipseAdoptium.Temurin.17.JDK` 或 [adoptium.net](https://adoptium.net) |
| **MySQL** | 8.0+ | 数据库 | `winget install Oracle.MySQL` 或 [mysql.com](https://dev.mysql.com/downloads) |
| **Maven** | 3.8+ | 后端构建 | 项目自带 `mvnw`，无需手动安装 |

### 可选安装

| 工具 | 版本 | 用途 | 说明 |
|------|------|------|------|
| **Redis** | 7.0+ | 缓存加速 | `winget install Redis.Redis`；没有也能跑，但登录限流等功能受影响 |

### 安装后验证

```bash
git --version      # 应输出版本号
node -v            # 应 ≥ v20.19
java -version      # 应显示 17.x
mysql --version    # 应显示 8.0.x
redis-cli ping     # 应返回 PONG（如安装了 Redis）
```

---

## 快速开始

### 第 1 步：克隆项目

```bash
git clone https://github.com/ymzwp/kaquan-mall.git
cd kaquan-mall
```

### 第 2 步：初始化数据库

启动 MySQL 服务，然后执行建表脚本：

```bash
mysql -u root -p < kaquan-server/src/main/resources/schema.sql
```

或在 MySQL 客户端中：

```sql
source kaquan-server/src/main/resources/schema.sql
```

这会自动创建 `coupon_mall` 数据库和下面 11 张表：

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `user` | 前台用户 | username, balance, points, member_level, invite_code |
| `category` | 商品分类 | cate_name, parent_id, level（三级层级） |
| `goods` | 卡券商品 | goods_name, price, stock, cate_id |
| `orders` | 订单 | user_id, goods_id, order_price, pay_method, status |
| `recharge_order` | 充值记录 | user_id, order_no, amount, balance_before/after |
| `user_sign` | 签到记录 | user_id, sign_date |
| `coupon` | 优惠券 | title, amount, min_amount, stock |
| `user_coupon` | 用户领券 | user_id, coupon_id, status |
| `admin` | 后台管理员 | username, password |
| `sys_notify` | 站内通知 | title, content, status |
| `invite_reward` | 邀请奖励 | inviter_id, invitee_id, reward |

### 第 3 步：修改数据库密码

编辑 `kaquan-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coupon_mall?useSSL=false&serverTimezone=Asia/Shanghai
    username: root          # ← 改成你的 MySQL 用户名
    password: 1234          # ← 改成你的 MySQL 密码
```

> 如果你的 Redis 密码不是空，也在同文件里搜 `redis:` 修改。

### 第 4 步：启动后端

```bash
cd kaquan-server

# Windows（PowerShell / CMD）
mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```

首次运行会自动下载 Maven 和所有依赖 jar 包（约 2~5 分钟）。  
看到以下日志表示启动成功：

```
Started CouponBackApplication in X.XXX seconds
```

后端运行在 **http://localhost:8088**

### 第 5 步：启动前端

打开**新终端窗口**：

```bash
cd kaquan
npm install                # 首次需安装依赖（约 1~2 分钟）
npm run dev                # 启动 Vite 开发服务器
```

浏览器打开 **http://localhost:3000**

### 第 6 步：注册管理员账号（可选）

```sql
-- 在 MySQL 中执行，创建管理员（密码用 BCrypt 加密）
-- 可以先通过注册页面注册普通用户，再手动改数据库：
INSERT INTO admin (username, password) VALUES ('admin', '<BCrypt哈希>');
```

后台管理入口：http://localhost:3000/adminLogin

---

## 核心配置说明

### 后端配置文件

`kaquan-server/src/main/resources/application.yml` 关键配置：

```yaml
server:
  port: 8088                              # 后端端口
  servlet:
    session:
      timeout: 30m                        # 会话 30 分钟过期
      cookie:
        http-only: true                   # 防 XSS 攻击
        same-site: strict                 # 防 CSRF 攻击
        name: CMSESSION                   # 自定义 Cookie 名

spring:
  datasource:
    druid:
      stat-view-servlet:                  # Druid 监控面板
        enabled: true                     # 访问 http://localhost:8088/druid
        login-username: druidAdmin        # 监控页登录账号
        login-password: Dr@2026Secure     # 监控页登录密码

logging:
  level:
    cn.yunmeng.book.mapper: debug         # SQL 日志级别
```

### 前端代理配置

前端请求通过 Axios 直连 `http://localhost:8088`（见 `src/utils/http.js`），如需代理可修改 `vite.config.js`。

---

## 完整 API 文档

> 基础路径：`http://localhost:8088`  
> 返回格式：`{ code: 200, msg: "success", data: ... }`  
> 管理员接口如未登录返回 `code: 403`

### 用户模块 — `/user`

| 方法 | 路径 | 参数 | 说明 | 登录 |
|------|------|------|------|------|
| POST | `/user/login` | `{ username, password }` | 用户登录（限流：1分钟5次，输错则1分钟内不允许再试） | 否 |
| POST | `/user/reg` | `{ username, password, inviteCode? }` | 注册（用户名≥2字符，密码≥6位，支持邀请码） | 否 |
| POST | `/user/getLogin` | — | 获取当前登录用户信息（实时查询DB） | 是 |
| POST | `/user/update` | `{ nick_name, avatar }` | 修改昵称和头像 | 是 |
| POST | `/user/logout` | — | 退出登录 | 是 |
| POST | `/user/sign` | — | 每日签到（+10云梦豆） | 是 |
| POST | `/user/sign/check` | — | 查询今日是否已签到 | 是 |
| POST | `/user/buyMember` | `{ password, price, level }` | 购买会员（扣余额，level: normal/bronze/silver/gold） | 是 |
| GET | `/user/invite` | — | 我的邀请码 + 已邀请用户列表 | 是 |

### 商品模块 — `/goods`

| 方法 | 路径 | 参数 | 说明 | 登录 |
|------|------|------|------|------|
| GET | `/goods/list` | `?cid=&page=1&pageSize=12` | 商品列表（支持分类筛选+分页） | 否 |
| GET | `/goods/get` | `?id=` | 单个商品详情 | 否 |

### 分类模块 — `/cate`

| 方法 | 路径 | 参数 | 说明 | 登录 |
|------|------|------|------|------|
| GET | `/cate/list` | — | 树形分类列表（三级层级） | 否 |

### 订单模块 — `/order`

| 方法 | 路径 | 参数 | 说明 | 登录 |
|------|------|------|------|------|
| POST | `/order/create` | `{ goodsId, accountNumber, payMethod?, discount? }` | 创建订单（支持余额支付） | 是 |
| POST | `/order/my` | — | 我的订单列表 | 是 |
| POST | `/order/refund` | `{ id, password }` | 退款（退还余额，需密码确认） | 是 |

### 充值模块 — `/recharge`

| 方法 | 路径 | 参数 | 说明 | 登录 |
|------|------|------|------|------|
| POST | `/recharge/create` | `{ amount(1~5000), payMethod, password }` | 创建充值订单（需密码确认） | 是 |
| POST | `/recharge/list` | — | 我的充值记录 | 是 |

### 优惠券模块 — `/coupon`

| 方法 | 路径 | 参数 | 说明 | 登录 |
|------|------|------|------|------|
| GET | `/coupon/list` | — | 可领取的优惠券 | 否 |
| POST | `/coupon/take` | `{ couponId }` | 领取优惠券（扣库存） | 是 |
| GET | `/coupon/my` | — | 我的优惠券 | 是 |
| POST | `/coupon/use` | `{ userCouponId }` | 使用优惠券（下单时核销） | 是 |

### 通知模块 — `/notify`

| 方法 | 路径 | 参数 | 说明 | 登录 |
|------|------|------|------|------|
| GET | `/notify/list` | — | 获取可见通知列表 | 否 |

### 数据趋势 — `/trend`

| 方法 | 路径 | 参数 | 说明 | 登录 |
|------|------|------|------|------|
| GET | `/trend/price` | `?days=30` | 各商品近N天价格走势 | 否 |

### 后台管理 — `/admin`（全部需要管理员登录）

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| POST | `/admin/login` | `{ username, password }` | 管理员登录（含限流） |
| POST | `/admin/check` | — | 校验登录状态（路由守卫用） |
| POST | `/admin/logout` | — | 退出登录 |
| | | | |
| GET | `/admin/goods/list` | — | 商品管理：查全部商品 |
| POST | `/admin/goods/add` | `{ goods_name, price, stock, ... }` | 添加商品 |
| POST | `/admin/goods/update` | `{ id, ... }` | 更新商品 |
| POST | `/admin/goods/del` | `?id=` | 删除商品 |
| | | | |
| GET | `/admin/user/list` | — | 用户管理：查全部用户 |
| POST | `/admin/user/update` | `{ id, ... }` | 更新用户信息 |
| POST | `/admin/user/del` | `?id=` | 删除用户 |
| | | | |
| GET | `/admin/order/all` | `?page=1&pageSize=15` | 订单管理：分页查全部订单 |
| | | | |
| GET | `/admin/hotRank` | — | 热销排行 TOP 3 |
| GET | `/admin/trend` | — | 近30天趋势（订单/交易额/注册数） |
| | | | |
| GET | `/cate/admin/list` | — | 分类管理列表 |
| POST | `/cate/admin/add` | `{ cate_name, parent_id?, level }` | 添加分类 |
| POST | `/cate/admin/update` | `{ id, cate_name, ... }` | 更新分类 |
| POST | `/cate/admin/del` | `?id=` | 删除分类 |
| | | | |
| GET | `/notify/admin/list` | — | 通知管理列表 |
| POST | `/notify/admin/add` | `{ title, content, status }` | 添加通知 |
| POST | `/notify/admin/update` | `{ id, ... }` | 更新通知 |
| POST | `/notify/admin/del` | `?id=` | 删除通知 |
| | | | |
| GET | `/coupon/admin/list` | — | 优惠券管理列表 |
| POST | `/coupon/admin/add` | `{ title, amount, min_amount, stock }` | 添加优惠券 |
| POST | `/coupon/admin/update` | `{ id, ... }` | 更新优惠券 |
| POST | `/coupon/admin/del` | `?id=` | 删除优惠券 |

---

## 前端路由与权限

| 路径 | 页面 | 权限 |
|------|------|------|
| `/index` | 首页 | 公开 |
| `/cate` | 分类浏览 | 公开 |
| `/login` | 登录/注册 | 公开 |
| `/goodsDetail/:id` | 商品详情 | 公开 |
| `/confirmOrder` | 确认下单 | **需用户登录** |
| `/order` | 我的订单 | **需用户登录** |
| `/mine` | 个人中心 | **需用户登录** |
| `/recharge` | 余额充值 | **需用户登录** |
| `/adminLogin` | 后台登录 | 公开 |
| `/admin` | 后台管理面板 | **需管理员登录** |

---

## 数据库 ER 图（简化）

```
                    ┌──────────┐
                    │  admin   │
                    └──────────┘

┌──────────┐     ┌──────────┐     ┌──────────┐
│ category │←──  │  goods   │──→  │  orders  │
└──────────┘     └──────────┘     └────┬─────┘
                                    ┌──────┘
┌──────────┐     ┌──────────────┐   │
│  coupon  │←──  │ user_coupon  │   │
└──────────┘     └──────┬───────┘   │
                     ┌──┘           │
              ┌──────▼──────┐       │
              │    user     │◄──────┘
              └──┬──┬──┬───┘
        ┌────────┘  │  └─────────┐
┌───────▼──────┐ ┌──▼──────┐ ┌──▼───────────┐
│  user_sign   │ │recharge │ │ invite_reward │
│              │ │ _order  │ │               │
└──────────────┘ └─────────┘ └───────────────┘

┌──────────────┐
│  sys_notify  │  (独立表，无外键)
└──────────────┘
```

---

## 技术栈

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | ^3.5 | 渐进式前端框架 |
| Vue Router | ^5.1 | 前端路由（含导航守卫） |
| Vite | ^8.0 | 开发服务器 + 构建工具 |
| Element Plus | ^2.14 | UI 组件库 |
| ECharts | ^6.1 | 数据可视化图表 |
| Axios | ^1.17 | HTTP 请求库 |
| @element-plus/icons-vue | ^2.3 | Element Plus 图标集 |

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.6 | 应用框架 |
| MyBatis | 4.0.1 | ORM 数据访问 |
| Druid | 1.2.20 | 数据库连接池 + SQL 监控 |
| MySQL Connector | 8.0+ | MySQL JDBC 驱动 |
| Redis (Lettuce) | — | 缓存 + 登录限流 |
| Spring Security Crypto | — | BCrypt 密码加密 |
| Lombok | 1.18 | 简化 Java 代码 |

---

## 常见问题

### Q: 前端页面空白 / 接口 404？
确保后端已启动在 `:8088`。打开 http://localhost:8088/druid 验证后端是否存活。

### Q: 数据库连接失败？
```bash
# 确认 MySQL 服务已启动
mysql -u root -p -e "SELECT 1"

# 检查 application.yml 中密码是否正确
```

### Q: 接口报 "表不存在"？
说明没执行建表脚本，运行：
```bash
mysql -u root -p < kaquan-server/src/main/resources/schema.sql
```

### Q: Redis 报错 `Unable to connect to localhost:6379`？
```bash
# 启动 Redis 服务
redis-server

# 或临时注释掉 application.yml 中的 redis 配置块
```

### Q: 端口 8088 被占用？
```bash
# Windows 查看端口占用
netstat -ano | findstr 8088

# 修改 application.yml 中 server.port 为其他端口
# 同时修改 kaquan/src/utils/http.js 中的 BASE 地址
```

### Q: npm install 很慢？
设置国内镜像：
```bash
npm config set registry https://registry.npmmirror.com
```

### Q: 如何部署到服务器？
```bash
# 前端打包
cd kaquan && npm run build    # 产出 dist/ 目录

# 后端打包
cd kaquan-server
./mvnw package -DskipTests    # 产出 target/*.jar

# 部署
java -jar target/kaquan-0.0.1-SNAPSHOT.jar
```
将 `dist/` 放到 Nginx 下，jar 包放到服务器上运行即可。
