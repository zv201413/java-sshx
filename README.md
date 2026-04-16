# 🚀 java-sshx

这是一个基于 Java (Spring Boot) 开发的一站式代理部署工具，集成了 **sing-box 多协议代理**、**SSHX 网页终端** 以及 **GitHub Gist / Telegram 自动化同步** 功能。

## ✨ 功能特性

- **多协议支持**：使用 sing-box 核心，一键部署 VLESS (WS/Argo), Reality, Hysteria2, TUIC, SOCKS5 等。
- **Argo 隧道**：内置 Cloudflare Argo Tunnel，支持固定域名和临时域名。
- **SSHX 终端**：自动启动 SSHX 网页终端，支持通过浏览器协作管理服务器。
- **自动化推送**：节点信息自动生成并同步至 GitHub Gist，同时发送至 Telegram 机器人。
- **自动保活**：支持定时访问指定 URL 以防止容器或服务器进入休眠。
- **智能分流**：内置 WARP 出站，支持对 Netflix/OpenAI 等服务自动分流。
- **零配置安装**：程序自动下载并识别系统架构（arm64/amd64），自动生成 Reality 密钥对。

---

## 快速开始

### 1. 下载程序
从 [Releases](https://github.com/zv201413/java-sshx/releases) 页面下载最新的 `java-sshx.jar`。

### 2. 准备配置文件
在 JAR 包同级目录下创建 `application.yml` 文件。你可以直接参考仓库根目录下的 [application.yml 示例模板](./application.yml)。

### 3. 运行项目
```bash
java -jar java-sshx.jar
```

---

## ⚙️ 核心配置项说明

| 配置项 | 说明 | 示例值 |
| :--- | :--- | :--- |
| `app.domain` | 服务器域名或 IP (用于直连节点) | `example.com` |
| `app.port` | 监听端口 (Reality 端口，Hy2/TUIC 会在此基础上递增) | `10008` |
| `app.uuid` | 用户身份 UUID | `自动生成` |
| `app.enable-sshx` | 是否开启 SSHX 网页终端 | `true` |
| `app.gist-id` | GitHub Gist ID | `你的 Gist ID` |
| `app.gh-token` | GitHub PAT (需要 Gist 权限) | `ghp_xxxx` |
| `app.telegram-bot-token` | Telegram Bot API Token | `12345:xxxx` |
| `app.telegram-chat-id` | 接收通知的 Chat ID | `12345678` |
| `app.project-url` | 本项目运行的 URL (用于保活) | `https://yourapp.onrender.com` |
| `app.auto-keepalive` | 是否开启定时保活任务 | `true` |
| `app.warp-mode` | WARP 模式 (auto/warp/direct) | `auto` |

---

## 📡 订阅方式

程序启动后会自动生成订阅链接并在控制台输出：
- **Base64 订阅**：`http://your-ip:port/sub`
- **明文节点列表**：`http://your-ip:port/list`
- **SSHX 链接**：`http://your-ip:port/sshx`

---

## 📢 免责声明
- 本项目仅供技术研究和学习，请勿用于违法用途。
- 作者不对因使用本项目导致的任何封禁或法律责任负责。
