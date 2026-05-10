#!/bin/bash

# ===== 配置变量 =====
APP_NAME="codebit-api"
PORT=8888
JAR_FILE="codebit-api-1.0-SNAPSHOT.jar"
# ===================

DEPLOY_DIR="/root/deploy/jdk21-app"

cd ${DEPLOY_DIR}

echo "===== 开始部署 ${APP_NAME} ====="

# 停止并删除旧容器
echo "1. 停止旧容器..."
docker stop ${APP_NAME} 2>/dev/null
docker rm ${APP_NAME} 2>/dev/null

# 删除旧镜像
echo "2. 删除旧镜像..."
docker rmi ${APP_NAME}:latest 2>/dev/null

# 检查 JAR 文件是否存在
if [ ! -f "${JAR_FILE}" ]; then
    echo "错误: JAR 文件 ${JAR_FILE} 不存在！"
    exit 1
fi

# 构建新镜像
echo "3. 构建新镜像..."
docker build -t ${APP_NAME}:latest .

# 启动新容器
echo "4. 启动新容器..."
docker run -d \
  --name ${APP_NAME} \
  -p ${PORT}:${PORT} \
  -e TZ="Asia/Shanghai" \
  --restart=always \
  ${APP_NAME}:latest

# 检查容器启动状态
if [ $? -eq 0 ]; then
    echo "===== 部署完成 ====="
    echo "应用访问地址: http://192.168.100.101:${PORT}"
    echo "查看日志: docker logs ${APP_NAME} -f"
else
    echo "===== 部署失败 ====="
    exit 1
fi

# 显示最近日志
docker logs ${APP_NAME} --tail 10