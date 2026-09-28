<template>
  <el-dialog v-model="visible" title="选择规格" width="720px" top="8vh" append-to-body>
    <div v-if="detail" class="sku-picker">
      <div class="sku-picker__head">
        <div class="sku-picker__img abc-img-box">
          <img :src="currentSku?.image || detail.product.mainImage" :alt="detail.product.name" />
        </div>
        <div class="sku-picker__info">
          <MoneyText :value="currentSku ? currentSku.price : detail.product.price" size="large" />
          <div class="sku-picker__stock">
            库存：{{ currentSku ? currentSku.stock : detail.product.stock }} 件
          </div>
          <div class="sku-picker__selected">
            已选：<b>{{ selectedText || '请选择规格' }}</b>
          </div>
        </div>
      </div>

      <div v-for="spec in detail.specs" :key="spec.id" class="sku-picker__spec">
        <div class="sku-picker__spec-name">{{ spec.name }}</div>
        <div class="sku-picker__spec-values">
          <span v-for="v in spec.values" :key="v.id"
                class="sku-value"
                :class="{
                  active: selectedMap[spec.name] === v.value,
                  disabled: !isValueAvailable(spec.name, v.value)
                }"
                @click="choose(spec.name, v.value)">
            {{ v.value }}
          </span>
        </div>
      </div>

      <div class="sku-picker__qty">
        <div class="sku-picker__spec-name">数量</div>
        <el-input-number v-model="quantity" :min="1" :max="maxQuantity" />
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :disabled="!currentSku || currentSku.stock <= 0" @click="confirm">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import MoneyText from './MoneyText.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  detail: { type: Object, default: null },
  confirmText: { type: String, default: '确定' }
})
const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const selectedMap = ref({})
const quantity = ref(1)

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      selectedMap.value = {}
      quantity.value = 1
      autoPick()
    }
  }
)

/** 默认选中第一个有库存的组合 */
function autoPick() {
  const detail = props.detail
  if (!detail || !detail.specs || !detail.specs.length) return
  detail.specs.forEach((spec) => {
    const first = spec.values.find((v) => isValueAvailable(spec.name, v.value))
    if (first) {
      selectedMap.value[spec.name] = first.value
    }
  })
}

const currentSku = computed(() => {
  const detail = props.detail
  if (!detail) return null
  const specs = detail.specs || []
  if (!specs.length) {
    return (detail.skus || [])[0] || null
  }
  const chosen = specs.every((s) => selectedMap.value[s.name])
  if (!chosen) return null
  return (
    (detail.skus || []).find((sku) => {
      let arr = []
      try {
        arr = JSON.parse(sku.specJson || '[]')
      } catch (e) {
        return false
      }
      return arr.every((item) => selectedMap.value[item.specName] === item.value)
    }) || null
  )
})

const selectedText = computed(() => {
  const specs = (props.detail && props.detail.specs) || []
  return specs.map((s) => selectedMap.value[s.name]).filter(Boolean).join(' ')
})

const maxQuantity = computed(() => {
  const stock = currentSku.value ? currentSku.value.stock : 1
  return Math.max(1, Math.min(stock || 1, 99))
})

/** 判断某规格值是否还有库存（其它已选规格固定的情况下） */
function isValueAvailable(specName, value) {
  const detail = props.detail
  if (!detail || !detail.skus) return true
  const specs = detail.specs || []
  return detail.skus.some((sku) => {
    if (!sku.stock || sku.stock <= 0) return false
    let arr = []
    try {
      arr = JSON.parse(sku.specJson || '[]')
    } catch (e) {
      return false
    }
    if (!arr.some((i) => i.specName === specName && i.value === value)) return false
    // 其它已选规格需要匹配
    return specs.every((s) => {
      if (s.name === specName) return true
      const picked = selectedMap.value[s.name]
      if (!picked) return true
      return arr.some((i) => i.specName === s.name && i.value === picked)
    })
  })
}

function choose(specName, value) {
  if (!isValueAvailable(specName, value)) {
    ElMessage.warning('该规格组合暂时缺货')
    return
  }
  selectedMap.value = { ...selectedMap.value, [specName]: value }
}

function confirm() {
  if (!currentSku.value) {
    ElMessage.warning('请先选择完整规格')
    return
  }
  emit('confirm', { sku: currentSku.value, quantity: quantity.value })
  visible.value = false
}
</script>

<style scoped>
.sku-picker__head {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed var(--abc-border);
}
.sku-picker__img {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  flex-shrink: 0;
}
.sku-picker__info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}
.sku-picker__stock {
  font-size: 13px;
  color: var(--abc-text-2);
}
.sku-picker__selected {
  font-size: 13px;
  color: var(--abc-text-2);
}
.sku-picker__selected b {
  color: var(--abc-primary);
}
.sku-picker__spec {
  margin-top: 18px;
}
.sku-picker__spec-name {
  font-size: 13px;
  color: var(--abc-text-2);
  margin-bottom: 8px;
}
.sku-picker__spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.sku-value {
  padding: 6px 16px;
  border: 1px solid var(--abc-border);
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.sku-value:hover {
  border-color: var(--abc-primary);
  color: var(--abc-primary);
}
.sku-value.active {
  border-color: var(--abc-primary);
  background: var(--abc-primary-light);
  color: var(--abc-primary);
  font-weight: 600;
}
.sku-value.disabled {
  color: #c6ccd6;
  border-style: dashed;
  cursor: not-allowed;
  background: #fafbfc;
}
.sku-picker__qty {
  margin-top: 18px;
}
</style>
