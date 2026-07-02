# NineTwoTool — 多功能桌面工具箱

一个集成多种实用工具的应用，支持 **Web 网页端**和 **Windows 桌面端**（Electron），基于 Vue 3 + Spring Boot 构建。

- **Web 端**：浏览器直接访问，无需安装
- **桌面端**：Electron 封装，内嵌 JRE 和数据库，开箱即用

## 功能概览

| 功能 | 说明 |
|------|------|
| JSON 格式化 | Monaco Editor 编辑器，支持美化、递归重构、多窗口对比 |
| 图片压缩 | 自定义宽高、质量、格式（JPG/PNG/WEBP） |
| PDF 转 Word | 上传 PDF 输出 Word 文档 |
| Word 转 PDF | 上传 Word 输出 PDF |
| TXT 转 PDF | 文本内容直接转 PDF |
| 邮件发送 | SMTP 邮件发送工具 |

## 技术栈

- **前端**：Vue 3 + Element Plus + Monaco Editor
- **后端**：Spring Boot + MyBatis-Plus + PostgreSQL
- **桌面**：Electron + electron-builder（内嵌 JRE 21）

## 项目结构

```
nine-two-toolbox/
├── electron/                  # Electron 主进程
│   ├── main.js                # 主进程入口
│   ├── preload.js             # 预加载脚本
│   └── resources/             # 打包资源（JRE、配置文件）
├── web-app/                   # Vue 3 前端
│   └── src/App.vue            # 主界面
├── toolbox-server/            # Spring Boot 后端
│   ├── src/main/java/         # Java 源码
│   └── src/main/resources/    # 配置文件
└── package.json               # Electron 构建配置
```

## 快速开始

### 环境要求

- Node.js 16+
- JDK 21
- Maven 3.6+
- PostgreSQL（本地开发需要）

### 开发调试

```bash
# 启动前端开发服务器
npm run dev

# 启动 Electron 开发模式（需先启动前端）
npm run dev:electron
```

### 一键打包

```bash
# 完整打包（前端 + 后端 + Electron 安装包）
npm run build:all

# 跳过后端打包（后端未改动时）
npm run build:all:skip-backend
```

### 分步打包

```bash
# 1. 构建前端
npm run build:frontend

# 2. 构建后端
npm run build:backend

# 3. 打包 Electron 安装包
npm run build:electron
```

### 打包产物

```
release/
├── win-unpacked/                    # 免安装版
│   └── NineTwoTool.exe
└── NineTwoTool Setup 1.0.0.exe     # NSIS 安装包
```

## 配置说明

### 后端配置

配置文件位于 `toolbox-server/src/main/resources/application.yml`，主要配置项：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:postgresql://localhost:5433/your-db
          username: postgres
          password: your-db-password

  mail:
    host: smtp.example.com
    username: your-email@example.com
    password: your-email-auth-code

file-storage:
  path: /home/ninetwo/files/
```

### Electron 配置

桌面端独立配置位于 `electron/resources/application-electron.yml`，打包后随应用分发。

## 许可证

MIT License
