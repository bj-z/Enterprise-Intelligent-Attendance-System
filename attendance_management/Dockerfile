# ============================================
# 阶段1：Node 构建
# ============================================
FROM node:20-alpine AS builder

WORKDIR /build

# 利用缓存层
COPY package.json package-lock.json yarn.lock* ./
RUN npm ci --registry=https://registry.npmmirror.com

COPY . .
RUN npm run build

# ============================================
# 阶段2：Nginx 运行
# ============================================
FROM nginx:1.25-alpine

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
