# data-chain-static

接入端工作台（WSC / Workspace Console）**前端**独立仓库。

| 项 | 说明 |
|---|---|
| 远程 | `https://codeup.aliyun.com/5f4356276207a1a8b17f985c/SH-BIGDATA/data-chain/data-chain-static.git` |
| 技术栈 | Vue 3 + TypeScript + Vite 5 + Pinia + Vue Router 4 + Ant Design Vue 4 |
| Node | ≥ 20（见 `.nvmrc`） |
| 包管理 | pnpm 9 |

控制面（PRD / 规划 / Run）保留在本地 AI-SEP 仓，**不**随本仓库发布。

## 本地开发

```bash
pnpm install
cp .env.example .env.local   # 按需改 VITE_API_BASE_URL
pnpm dev                     # http://127.0.0.1:5173
```

需同时启动后端（`data-chain-backend`，默认 `8080`）。后端 CORS 须允许本机源（见后端 `WSC_CORS_ALLOWED_ORIGINS`）。

## 独立构建与部署

```bash
cp .env.production.example .env.production
# 编辑 VITE_API_BASE_URL=https://<后端域名>/api/v1
pnpm install --frozen-lockfile   # CI 推荐
pnpm typecheck
pnpm test
pnpm build                       # 产物：dist/
pnpm preview                     # 本地预览产物
```

部署 `dist/` 到任意静态托管（Nginx / OSS / CDN）。History 路由需回退到 `index.html`，例如 Nginx：

```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

跨域时：

1. 前端 `VITE_API_BASE_URL` 指向后端公网 API 前缀  
2. 后端配置 `WSC_CORS_ALLOWED_ORIGINS` 包含前端 Origin  
3. Cookie 会话跨站时需后端 Cookie `SameSite=None; Secure`（同主域反向代理可避免）

## 与 AI-SEP 控制面关系

| 路径 | 归属 |
|---|---|
| 本仓库源码 | 实现仓 `data-chain-static` |
| `product/` `planning/` `ai/runs/` | 本地 AI-SEP 控制面 |
| OpenAPI / 契约版本 | 以控制面 `contracts/` 为准；本仓 `src/api` 对齐消费 |

## 常用脚本

| 命令 | 作用 |
|---|---|
| `pnpm dev` | 开发服务器 |
| `pnpm build` | 类型检查 + 生产构建 |
| `pnpm typecheck` | 仅类型检查 |
| `pnpm test` | Vitest |
| `pnpm preview` | 预览 `dist/` |
