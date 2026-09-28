import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一 axios 实例
 *
 * - baseURL 使用 /api，由 vite.config.js 的 proxy 转发到后端 8080
 * - withCredentials: true 让浏览器携带 Session Cookie（JSESSIONID）
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 20000,
  withCredentials: true
})

/** 401 回调，由 main.js 注入（避免循环依赖） */
let unauthorizedHandler = null
let redirecting = false

export function setUnauthorizedHandler(fn) {
  unauthorizedHandler = fn
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const body = response.data

    // 非统一结构（例如直接返回字符串/SVG）原样返回
    if (!body || typeof body !== 'object' || !('code' in body)) {
      return body
    }

    if (body.code === 200) {
      return body.data
    }

    if (body.code === 401) {
      if (!redirecting) {
        redirecting = true
        setTimeout(() => {
          redirecting = false
        }, 1500)
        ElMessage.warning(body.message || '请先登录')
        if (unauthorizedHandler) {
          unauthorizedHandler()
        }
      }
      return Promise.reject(new Error(body.message || '未登录'))
    }

    ElMessage.error(body.message || '操作失败')
    return Promise.reject(new Error(body.message || '操作失败'))
  },
  (error) => {
    const status = error.response && error.response.status
    if (status === 404) {
      ElMessage.error('接口不存在：' + (error.config ? error.config.url : ''))
    } else if (status === 500) {
      ElMessage.error('服务器异常，请稍后再试')
    } else {
      ElMessage.error('网络异常，请检查后端服务是否已启动')
    }
    return Promise.reject(error)
  }
)

export default request
