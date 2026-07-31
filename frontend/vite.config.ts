/// <reference types="vitest/config" />
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

/**
 * 独立部署说明：
 * - 本仓库可不依赖 AI-SEP monorepo；在 frontend/ 根目录执行 pnpm install / build。
 * - VITE_API_BASE_URL：浏览器访问后端 API 的完整前缀（含 /api/v1）。
 * - VITE_BASE：静态资源 publicPath（部署在子路径时设置，如 /console/）。
 * - 开发态可选代理：未设置 VITE_API_BASE_URL 时，请求 /api → VITE_DEV_PROXY_TARGET。
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const base = env.VITE_BASE || '/'
  const proxyTarget = env.VITE_DEV_PROXY_TARGET || 'http://127.0.0.1:8080'

  return {
    base,
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      port: Number(env.VITE_DEV_PORT || 5173),
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
        },
      },
    },
    preview: {
      port: Number(env.VITE_PREVIEW_PORT || 4173),
    },
    build: {
      outDir: 'dist',
      sourcemap: mode !== 'production',
      emptyOutDir: true,
    },
    test: {
      environment: 'node',
      include: ['src/**/*.spec.ts'],
    },
  }
})
