# 使用 JDK 21 的基础镜像
FROM eclipse-temurin:21-jdk-alpine

# 维护者信息（可选）
LABEL maintainer="yangkaihu@yeah.net"

# 设置工作目录
WORKDIR /app

# 复制 jar 包到容器内
COPY codebit-api-1.0-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8888

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
