import { defineStore } from 'pinia'
import { cartApi } from '@/api'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),

  getters: {
    /** 总条目数 */
    count: (state) => state.items.length,
    /** 有效商品 */
    validItems: (state) => state.items.filter((i) => i.valid),
    /** 失效商品 */
    invalidItems: (state) => state.items.filter((i) => !i.valid),
    /** 已勾选且有效 */
    selectedItems: (state) => state.items.filter((i) => i.valid && i.selected === 1),
    /** 已勾选件数 */
    selectedQuantity() {
      return this.selectedItems.reduce((sum, i) => sum + (i.quantity || 0), 0)
    },
    /** 已勾选金额 */
    selectedAmount() {
      return this.selectedItems.reduce((sum, i) => {
        const price = Number(i.price || 0)
        return sum + price * (i.quantity || 0)
      }, 0)
    },
    allSelected() {
      const valid = this.validItems
      return valid.length > 0 && valid.every((i) => i.selected === 1)
    }
  },

  actions: {
    async load() {
      this.loading = true
      try {
        const data = await cartApi.list()
        this.items = data || []
      } finally {
        this.loading = false
      }
      return this.items
    },

    async add(productId, skuId, quantity = 1) {
      await cartApi.add({ productId, skuId, quantity })
      await this.load()
    },

    async updateQuantity(id, quantity) {
      await cartApi.update({ id, quantity })
      const item = this.items.find((i) => i.id === id)
      if (item) {
        item.quantity = quantity
      }
    },

    async updateSelected(id, selected) {
      await cartApi.update({ id, selected })
      const item = this.items.find((i) => i.id === id)
      if (item) {
        item.selected = selected
      }
    },

    async selectAll(selected) {
      await cartApi.selectAll(selected)
      this.items.forEach((i) => {
        if (i.valid) {
          i.selected = selected
        }
      })
    },

    async changeSku(id, skuId) {
      await cartApi.changeSku(id, skuId)
      await this.load()
    },

    async remove(id) {
      await cartApi.remove(id)
      this.items = this.items.filter((i) => i.id !== id)
    },

    async clear() {
      await cartApi.clear()
      this.items = []
    },

    async cleanInvalid() {
      const count = await cartApi.cleanInvalid()
      await this.load()
      return count
    }
  }
})
