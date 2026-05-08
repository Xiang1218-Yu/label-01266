# 公租房管理系统 - 后端 Dockerfile
# 多阶段构建，支持 ARM64 和 AMD64

# ============ 构建阶段 ============
FROM --platform=$BUILDPLATFORM maven:3.8-eclipse-temurin-11 AS builder

WORKDIR /app

# 复制 pom.xml 先下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============ 运行阶段 ============
FROM eclipse-temurin:11-jre

LABEL maintainer="rental-admin"
LABEL description="公租房管理系统后端服务"

WORKDIR /app

# 创建非 root 用户
RUN groupadd -r rental && useradd -r -g rental rental

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 切换用户
USER rental

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
