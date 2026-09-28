# AbcMall 阿帕茨商城

一个前后端分离的商城前台（用户端）示例项目。后端 **SpringBoot 3 + MyBatis-Plus**，前端 **Vue3 + ElementPlus + Axios**，使用 **Session** 做身份认证，跨域通过前端 **vite proxy** 解决。

> 需求中标注 `!` 的功能（微信支付、拼团、直播、客服介入等）**只做「功能说明页」**：点击后展示该功能的定位与说明，不包含真实的后端对接与业务实现。

---

## 一、技术栈

| 分层 | 技术 | 版本 |
| --- | --- | --- |
| 后端 | SpringBoot | 3.2.5 |
| 后端 | MyBatis-Plus（spring-boot3 starter） | 3.5.5 |
| 后端 | MySQL Connector / Lombok / Validation | — |
| 数据库 | MySQL | 8.x |
| 前端 | Vue | 3.5 |
| 前端 | Element Plus | 2.8 |
| 前端 | Vue Router / Pinia / Axios | 4 / 2 / 1.7 |
| 前端 | Vite | 5.4 |
| 运行时 | JDK 17+ / Node 18+ | — |

---

## 二、目录结构

```
AbcMall_v2/
├── db/
│   └── abcmall.sql                # 建库建表 + 种子数据（约 40 张表）
├── backend/                       # SpringBoot 后端（端口 8080）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/abcmall/
│       │   ├── AbcMallApplication.java
│       │   ├── common/            # Result / PageResult / 异常 / 拦截器 / UserContext
│       │   ├── config/            # WebMvc / MyBatis-Plus / 参数绑定
│       │   ├── entity/            # 39 个实体（与表一一对应）
│       │   ├── mapper/            # MyBatis-Plus Mapper
│       │   ├── dto/ vo/           # 入参 / 出参对象
│       │   ├── service/           # 业务逻辑（购物车、订单、支付、售后…）
│       │   └── controller/        # REST 接口，统一前缀 /api
│       └── resources/application.yml
└── frontend/                      # Vue3 前端（端口 5173）
    ├── vite.config.js             # proxy: /api → http://localhost:8080
    └── src/
        ├── api/                   # axios 实例 + 全部接口封装
        ├── config/features.js     # 「!」功能说明字典（FEATURES / FEATURE_GROUPS）
        ├── components/            # AppHeader / ProductCard / SkuPicker / CountDown …
        ├── layouts/DefaultLayout.vue
        ├── store/                 # Pinia：user / cart
        ├── router/index.js        # 全部路由（含 /feature/:key 功能说明页）
        └── views/                 # 51 个页面
```

---

## 三、环境要求

- JDK 17 及以上（项目按 Java 17 编译）
- Maven 3.8+（或使用 `backend` 目录下的 `mvnw` 思路，见「启动后端」）
- MySQL 8.x，账号建议 `root / root`（可在 `application.yml` 修改）
- Node.js 18 及以上（推荐 20/22）

---

## 四、快速开始

### 1. 初始化数据库

```bash
mysql -uroot -proot -e "source D:/Desktop/AbcMall_v2/db/abcmall.sql"
```

或在客户端里打开 `db/abcmall.sql` 直接执行。脚本会：

- 创建数据库 `abcmall`（utf8mb4）
- 建表约 40 张：商品 / SKU / 分类 / 品牌 / 购物车 / 订单 / 地址 / 优惠券 / 积分 / 消息 / 物流 / 售后 / 评价 等
- 写入种子数据：20 个 SPU、45 个 SKU、优惠券、积分商品、公告、活动、专题、物流轨迹、示例订单等

**图片说明**：所有商品/轮播图使用后端实时生成的 SVG 占位图 `/api/image/ph?t=文案&w=宽&h=高&c=色号`，无需任何外部图床。

### 2. 启动后端（端口 8080）

```bash
cd D:/Desktop/AbcMall_v2/backend
mvn spring-boot:run
# 或打包后运行
mvn clean package -DskipTests
java -jar target/abcmall-backend-1.0.0.jar --server.port=8080
```

启动成功后：

```bash
curl http://localhost:8080/api/home/index     # 首页聚合数据
curl http://localhost:8080/api/home/server-time
```

> 若本机 Maven 不可用，可直接用 JDK 运行已打包好的 jar：
> `java -jar D:/Desktop/AbcMall_v2/backend/target/abcmall-backend-1.0.0.jar --server.port=8080`

### 3. 启动前端（端口 5173）

```bash
cd D:/Desktop/AbcMall_v2/frontend
npm install
npm run dev
```

浏览器访问 **http://localhost:5173**

> 跨域：前端所有请求走 `/api`，由 `vite.config.js` 的 proxy 转发到 `http://localhost:8080`。
> Session 依赖 Cookie，**必须使用该代理方案**（前后端同源），不要改成后端 CORS + 独立域名。

---

## 五、演示账号

| 账号 | 密码 | 说明 |
| --- | --- | --- |
| `zhangsan` | `123456` | 金卡会员，有余额 1580.50、积分 860、示例订单 |
| `lisi` | `123456` | 银卡会员，有余额 200.00、积分 320 |

也可在 `/register` 直接注册新账号（注册即送 300 积分）。

---

## 六、功能模块对照

### 已实现（可真实操作）

| 模块 | 说明 | 主要页面 |
| --- | --- | --- |
| 首页与导航 | 轮播图、广告位、公告、分类入口、推荐/热销/新品、活动专区、专题 | `/` |
| 分类 | 分类树、二级分类商品聚合 | `/category`、`/category/:id` |
| 商品 | 列表 / 筛选 / 排序 / 分页；详情含主图、SKU 选择、参数、图文详情、评价 | `/products`、`/product/:id` |
| 搜索 | 关键词搜索、联想、历史（localStorage）、热门词、筛选与排序 | `/search` |
| 购物车 | 加购、勾选、改数量、切规格、删除、清空、优惠试算、去结算 | `/cart` |
| 订单与结算 | 地址管理、下单预览（运费/券/积分/余额）、提交订单、订单列表/详情、取消、确认收货、再次购买 | `/checkout`、`/order/:id` |
| 收银台 | 支付方式列表 + 模拟支付打通完整链路 + 支付结果 | `/pay/:orderId`、`/pay/result/:orderNo` |
| 物流 | 物流单、轨迹时间线、快递公司 | `/order/:id/logistics` |
| 会员中心 | 注册/登录/退出、资料、改密、地址、订单、收藏、足迹、优惠券、积分、余额、消息、评价、售后 | `/user/*` |
| 营销 | 领券中心、积分商城、每日签到、活动专区、专题、公告中心 | `/coupon`、`/points-mall`、`/sign` … |
| 售后与评价 | 申请售后（退货/退款/换货/维修）、售后进度、发表评价、追评 | `/aftersale/apply`、`/comment/create` |

### 仅做「功能说明页」（需求中标注 `!`）

访问 `/features` 可查看全部说明项，共 9 组 50+ 项，例如：

- **购物车**：失效商品处理、移入收藏
- **订单结算**：自提点、余额抵扣、发票信息、订单备注
- **支付**：微信支付、支付宝、银联、余额支付、组合支付、支付结果回调、退款、部分退款、退款进度
- **会员中心**：第三方登录、手机绑定、密码修改、实名认证、会员等级、成长值
- **营销互动**：满减、折扣、满赠、秒杀、拼团、砍价、预售、套餐、赠品、抽奖、邀请有礼、分销、直播、短视频、种草社区
- **售后评价**：晒图、维修、投诉、工单、客服介入、仲裁
- **客服消息**：在线客服、智能客服、帮助中心、常见问题、APP 推送
- **物流配送**：门店自提、同城配送、预约配送、配送范围、运费模板

路由为 `/feature/:key`，页面会展示「此为 XXX 功能」+ 功能定位说明 + 相关业务入口。

---

## 七、接口约定

### 统一响应体

```json
{ "code": 200, "message": "操作成功", "data": {}, "success": true, "timestamp": 1789267238153 }
```

- `200` 成功；`401` 未登录（前端拦截器自动跳转登录页）；`400` 参数/业务校验失败；`500` 服务端异常
- 分页统一为 `{ list, total, page, size, pages }`

### 认证方式

- 登录成功后，服务端把 `userId` 写入 `HttpSession`
- 前端 axios `withCredentials: true`，浏览器自动携带 `JSESSIONID`
- `LoginInterceptor` 拦截需要登录的接口，未登录返回 `401`

### 接口分组（统一前缀 `/api`）

`/auth` `/home` `/category` `/brand` `/product` `/search` `/cart` `/address` `/order` `/pay` `/user` `/favorite` `/footprint` `/coupon` `/message` `/logistics` `/aftersale` `/comment` `/points` `/marketing` `/help` `/image`

---

## 八、常见问题

**Q：前端页面能打开，但数据全空、控制台报网络异常？**
A：后端没启动，或端口不是 8080。检查 `curl http://localhost:8080/api/home/server-time`。

**Q：登录成功但刷新后又变成未登录？**
A：说明请求没走 vite proxy（Cookie 跨域丢失）。确认通过 `http://localhost:5173` 访问，而不是直接双击 html 文件；确认 `vite.config.js` 中 proxy 生效。

**Q：图片出不来？**
A：图片由 `/api/image/ph` 实时生成 SVG，同样依赖代理转发到后端。

**Q：数据库连接失败？**
A：修改 `backend/src/main/resources/application.yml` 中的 `spring.datasource.username / password`，默认 `root / root`。

---

## 九、二次开发提示

- 新增页面：在 `src/views/` 建 `.vue`，到 `src/router/index.js` 注册路由即可（未登录访问带 `requiresAuth` 的路由会自动跳登录页）。
- 新增接口：在 `src/api/index.js` 对应模块中加一行，统一走 `request` 实例。
- 新增「!」功能说明项：在 `src/config/features.js` 的 `FEATURES` 里加一个 key，「功能一览」页面会自动按分组渲染。
111111