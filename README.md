# 公租房管理系统

## How to Run

```bash
# Docker Compose 一键启动（推荐）
docker compose up -d --build

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止服务
docker compose down
```

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 8081 | 管理后台页面 http://localhost:8081 |
| 后端 | 8080 | API 服务 http://localhost:8080/api |
| Redis | 6379 | 缓存服务 |
| 达梦数据库 | 5236 | DM8 数据库 |

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 系统管理员 |

## 题目内容

1、设计公租房管理系统，包括前端页面、后端代码以及创建数据库表的SQL脚本。2、前端界面使用VUE3，后端代码使用Java11，数据库使用达梦数据库，有需要使用缓存的地方使用redis。3、前端页面包括楼盘表页面和房源详情页面。页面整体采用蓝色调，风格简约、专业。4、楼盘表页面左侧展示小区及小区栋，用树形结构展示，支持搜索小区。底部增加房源导入和导出功能5、楼盘表页面右侧上方显示本小区和本栋统计信息（包括总户数、已出租、空置房、入住率）。下方按楼层展示所有户室卡片（卡片上显示房号和承租人名称），支持按状态和楼层筛选。6、点击户室卡片打开户室详情页面，显示房屋信息、承租人信息、家庭成员信息、操作记录。7、房屋信息包括：面积、租金（元/月）、状态（已出租、空置）、备注。默认不可编辑，点击修改按钮变成可编辑状态。8、承租人信息包括：姓名、身份证号、婚姻状况（选择框）、家庭人口、联系方式、收入状况（选择框）、所属社区、入住时间、是否残疾（选择框）、拆迁/轮候（选择框）、出售/置换（选择框）、备注。默认不可编辑，点击修改按钮变成可编辑状态、点击更换承租人按钮变成可编辑状态并清空所有信息。9、家庭成员信息包括：姓名、身份证号、与承租人关系，支持新增和删除。默认不可编辑状态，第7点中的编辑按钮和更换承租人按钮同时影响家庭成员信息。10、操作记录包括：操作人、操作时间、操作类型（新增、修改、更换承租人）、描述（编辑操作之后记录修改前后内容、更换承租人操作后记录原承租人详细信息）11、户室详情页面整体保存或取消。

---

## 本地开发环境

```bash
# 1. 启动 Redis
redis-server

# 2. 启动达梦数据库（数据库表会在应用启动时自动创建）

# 3. 启动后端
cd backend
mvn spring-boot:run

# 4. 启动前端
cd frontend-admin
npm install
npm run dev
```

本地开发访问地址：http://localhost:3000

## 数据库初始化

数据库表会在应用首次启动时**自动创建**，无需手动执行 SQL 脚本。

初始化脚本位置：`backend/src/main/resources/db/init.sql`

## 技术栈

**后端：** Java 11 / Spring Boot 2.7.18 / MyBatis Plus 3.5.3.1 / Spring Security + JWT / 达梦数据库 / Redis

**前端：** Vue 3.4 / Vite 5 / Element Plus 2.4 / Pinia / Vue Router 4 / Axios

## 项目结构

```
├── backend/                    # 后端项目
│   ├── src/main/java/com/rental/
│   │   ├── controller/         # 控制器
│   │   ├── service/            # 服务层
│   │   ├── entity/             # 实体类
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── dto/                # 数据传输对象
│   │   ├── security/           # JWT 安全相关
│   │   └── config/             # 配置类
│   └── src/main/resources/
│       ├── application.yml
│       └── db/init.sql
│
├── frontend-admin/             # 前端管理后台
│   └── src/
│       ├── api/                # API接口
│       ├── views/              # 页面组件
│       ├── layouts/            # 布局组件
│       ├── router/             # 路由配置
│       └── stores/             # Pinia状态管理
│
├── test-data/                  # 测试数据
│   └── 房源导入测试数据.xlsx
│
├── .gitignore
├── docker-compose.yml
└── README.md
```

## 功能说明

**楼盘表页面**
- 左侧：小区及楼栋树形结构，支持搜索
- 右侧上方：统计信息（总户数、已出租、空置房、入住率）
- 右侧下方：按楼层展示户室卡片，支持状态和楼层筛选
- 底部：房源导入/导出功能

**房屋详情页面**
- 房屋信息：面积、租金、状态、备注
- 承租人信息：姓名、身份证、婚姻状况、联系方式等
- 家庭成员：支持新增和删除
- 操作记录：记录所有修改历史

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/logout | POST | 退出登录 |
| /api/health | GET | 健康检查 |
| /api/community/tree | GET | 获取小区树 |
| /api/community/search | GET | 搜索小区 |
| /api/room/stats/building/{id} | GET | 楼栋统计 |
| /api/room/stats/community/{id} | GET | 小区统计 |
| /api/room/cards/{buildingId} | GET | 房屋卡片列表 |
| /api/room/floors/{buildingId} | GET | 楼栋楼层列表 |
| /api/room/detail/{roomId} | GET | 房屋详情 |
| /api/room/save | POST | 保存房屋信息 |
| /api/room/import | POST | 导入房源 |
| /api/room/export | GET | 导出房源 |
