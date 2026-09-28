import { defineStore } from 'pinia'
import { authApi, cartApi, messageApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    /** 登录用户信息，null 表示未登录 */
    info: null,
    /** 是否已从服务端拉取过一次登录态 */
    loaded: false,
    /** 购物车件数（导航角标） */
    cartCount: 0,
    /** 未读消息数（导航角标） */
    unreadCount: 0
  }),

  getters: {
    isLogin: (state) => !!state.info,
    nickname: (state) => (state.info ? state.info.nickname || state.info.username : ''),
    avatar: (state) => (state.info ? state.info.avatar : ''),
    points: (state) => (state.info ? state.info.points : 0),
    balance: (state) => (state.info ? state.info.balance : 0),
    levelName: (state) => (state.info ? state.info.levelName : ''),
    userId: (state) => (state.info ? state.info.id : null)
  },

  actions: {
    /** 拉取当前登录用户（Session 由 Cookie 维持） */
    async fetchCurrent() {
      try {
        const data = await authApi.current()
        this.info = data || null
      } catch (e) {
        this.info = null
      } finally {
        this.loaded = true
      }
      if (this.info) {
        this.refreshBadges()
      }
      return this.info
    },

    /** 登录成功后写入本地状态 */
    setInfo(user) {
      this.info = user
      this.loaded = true
      this.refreshBadges()
    },

    async refreshBadges() {
      if (!this.info) {
        this.cartCount = 0
        this.unreadCount = 0
        return
      }
      try {
        const [cart, unread] = await Promise.all([
          cartApi.count().catch(() => 0),
          messageApi.unreadCount().catch(() => 0)
        ])
        this.cartCount = cart || 0
        this.unreadCount = unread || 0
      } catch (e) {
        /* 忽略角标异常 */
      }
    },

    /** 局部刷新用户信息（如积分、余额变动后） */
    async refreshInfo() {
      if (!this.info) return
      try {
        const data = await authApi.current()
        if (data) {
          this.info = data
        }
      } catch (e) {
        /* 忽略 */
      }
    },

    async logout() {
      try {
        await authApi.logout()
      } catch (e) {
        /* 忽略 */
      }
      this.clear()
    },

    clear() {
      this.info = null
      this.cartCount = 0
      this.unreadCount = 0
    }
  }
})
