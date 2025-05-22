# 基于大模型的智能出行规划助手

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 项目背景
《"十四五"旅游业发展规划》指出要强化自主创新，加快推进智慧旅游建设。当前行程推荐软件普遍存在个性化不足、智能化程度低等问题。本项目基于大模型技术，构建智能出行规划系统，旨在提升游客的个性化游玩体验。

## 技术架构
### 系统组成
- **前端**：Vue3 + Vite + Element Plus
- **后端**：Spring Boot + Python Flask
- **数据库**：MySQL + Redis
- **算法层**：Transformer模型 + TSP优化算法
- **部署**：Docker容器化部署

### 功能特性
- 多模态行程需求理解
- 实时POI（兴趣点）推荐
- 动态路径优化算法
- 个性化偏好记忆
- 多语言交互支持

## 环境要求
- JDK 17+
- Python 3.9+
- Node.js 16+
- MySQL 8.0+
- Docker 20.10+

## 快速开始
### 数据库初始化
```bash
mysql -u root -p < docker/database/databaseinit.sql
```

### Docker部署
```bash
# 后端服务
docker build -f docker/backend_1/Dockerfile.backend -t travel-backend .

# 算法服务 
docker build -f docker/WorkSpace/Dockerfile.python -t travel-algo .

# 前端构建
cd docker/frontend/kqgis
npm install && npm run build
```

### 本地开发
**后端启动**
```bash
cd docker/backend_1
./mvnw spring-boot:run
```

**算法服务**
```bash
cd docker/WorkSpace
python map_recovery_api.py
```

**前端开发**
```bash
cd docker/frontend/kqgis
npm install
npm run dev
```


## 许可证
[MIT License](LICENSE)


        