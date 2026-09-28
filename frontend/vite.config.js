import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

/**
 * AbcMall 前端构建配置
 *
 * 跨域方案（按需求）：在前端 vite.config.js 里配置 proxy，
 * 把 /api 开头的请求转发到后端 8080，从而在开发环境规避浏览器同源策略。
 * 需要注意的是：Session 认证依赖 Cookie，因此这里必须保持同源代理，
 * 不能改成后端 CORS + 独立域名的方式。
 */
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: false
      }
    }
  },
  build: {
    outDir: 'dist',
    chunkSizeWarningLimit: 1500
  }
})
