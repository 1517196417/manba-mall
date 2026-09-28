import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from './api/request'
import { useUserStore } from './store/user'
import './styles/global.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 全局注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

/**
 * 统一处理 401：清空登录态并跳转登录页
 * 说明：这里用「注入回调」的方式而不是直接 import router，
 *      是为了避免 request -> router -> store -> api -> request 的循环依赖。
 */
setUnauthorizedHandler(() => {
  const userStore = useUserStore()
  userStore.clear()
  const current = router.currentRoute.value
  if (current.meta && current.meta.requiresAuth) {
    router.push({ path: '/login', query: { redirect: current.fullPath } })
  }
})

// 启动时恢复登录态（Session 由浏览器 Cookie 维持）
const userStore = useUserStore()
userStore.fetchCurrent()

app.mount('#app')
