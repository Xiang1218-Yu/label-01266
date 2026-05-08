# 公租房管理系统 - 前端 Dockerfile
# 多阶段构建，支持 ARM64 和 AMD64

# ============ 构建阶段 ============
FROM --platform=$BUILDPLATFORM node:18-alpine AS builder

WORKDIR /app

# 复制 package.json 先安装依赖（利用缓存）
COPY package.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源码并构建
COPY . .
RUN npm run build

# ============ 运行阶段 ============
FROM nginx:alpine

LABEL maintainer="rental-admin"
LABEL description="公租房管理系统前端服务"

# 删除默认配置
RUN rm -rf /usr/share/nginx/html/*

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
