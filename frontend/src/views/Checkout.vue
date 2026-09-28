<template>
  <div v-loading="loading" class="abc-container page">
    <div class="page__head">
      <h2>确认订单</h2>
      <span class="page__sub">请核对收货信息与商品明细后提交订单</span>
    </div>

    <!-- 收货地址 -->
    <div class="abc-card block">
      <div class="block__head">
        <span class="block__title"><el-icon><Location /></el-icon> 收货地址</span>
        <el-button size="small" :icon="Plus" @click="addressDialog = true">新增收货地址</el-button>
      </div>
      <div v-if="preview.address" class="addr-list">
        <div v-for="a in preview.addressList" :key="a.id" class="addr-item"
             :class="{ active: preview.address && preview.address.id === a.id }"
             @click="chooseAddress(a)">
          <div class="addr-item__top">
            <b>{{ a.receiver }}</b>
            <span>{{ a.phone }}</span>
            <el-tag v-if="a.isDefault === 1" size="small" type="warning" effect="plain">默认</el-tag>
            <el-tag v-if="a.tag" size="small" effect="plain">{{ a.tag }}</el-tag>
          </div>
          <div class="addr-item__detail">{{ fullAddress(a) }}</div>
        </div>
      </div>
      <EmptyBox v-else icon="Location" text="还没有收货地址，先添加一个吧"
                action-text="新增收货地址" @action="addressDialog = true" />
    </div>

    <!-- 配送方式 -->
    <div class="abc-card block">
      <div class="block__head">
        <span class="block__title"><el-icon><Van /></el-icon> 配送方式</span>
      </div>
      <div class="delivery">
        <div class="delivery__item" :class="{ active: deliveryType === 1 }" @click="setDelivery(1)">
          <b>快递配送</b>
          <span>满 99 元包邮，未满收 12 元运费</span>
        </div>
        <div class="delivery__item" :class="{ active: deliveryType === 2 }" @click="setDelivery(2)">
          <b>门店自提</b>
          <span>此为门店自提功能</span>
          <el-tag size="small" type="info">功能说明页</el-tag>
        </div>
        <div class="delivery__item" :class="{ active: deliveryType === 3 }" @click="setDelivery(3)">
          <b>同城配送</b>
          <span>此为同城配送功能</span>
          <el-tag size="small" type="info">功能说明页</el-tag>
        </div>
      </div>
      <div class="link-row">
        <el-link type="primary" :underline="false" @click="$router.push('/feature/pickup-point')">
          <el-icon><OfficeBuilding /></el-icon> 自提点
        </el-link>
        <el-link type="primary" :underline="false" @click="$router.push('/feature/store-pickup')">
          门店自提说明
        </el-link>
        <el-link type="primary" :underline="false" @click="$router.push('/feature/city-delivery')">
          同城配送说明
        </el-link>
      </div>
    </div>

    <!-- 商品明细 -->
    <div class="abc-card block">
      <div class="block__head">
        <span class="block__title"><el-icon><Goods /></el-icon> 商品明细</span>
        <span class="block__sub">共 {{ preview.items.length }} 种 / {{ preview.payAmount ? totalQuantity : 0 }} 件</span>
      </div>
      <div class="goods__head">
        <span class="g-col g-col--info">商品信息</span>
        <span class="g-col g-col--price">单价</span>
        <span class="g-col g-col--qty">数量</span>
        <span class="g-col g-col--total">小计</span>
      </div>
      <div v-for="item in preview.items" :key="item.id" class="goods__row">
        <div class="g-col g-col--info">
          <div class="goods__img abc-img-box"><img :src="item.image" :alt="item.productName" /></div>
          <div>
            <div class="goods__name abc-ellipsis-2">{{ item.productName }}</div>
            <div class="goods__sku">{{ item.skuDesc }}</div>
          </div>
        </div>
        <div class="g-col g-col--price"><MoneyText :value="item.price" size="small" /></div>
        <div class="g-col g-col--qty">× {{ item.quantity }}</div>
        <div class="g-col g-col--total"><MoneyText :value="Number(item.price) * item.quantity" size="small" /></div>
      </div>
    </div>

    <!-- 优惠与备注 -->
    <div class="abc-card block">
      <div class="row">
        <span class="row__label">优惠券</span>
        <el-select v-model="couponId" placeholder="不使用优惠券" clearable style="width: 320px" @change="onCouponChange">
          <el-option v-for="c in usableCoupons" :key="c.id" :value="c.id"
                     :label="couponLabel(c)" :disabled="c.usable === false" />
        </el-select>
        <el-link type="primary" :underline="false" class="ml12" @click="$router.push('/coupon')">
          去领券中心
        </el-link>
      </div>

      <div class="row">
        <span class="row__label">积分抵扣</span>
        <el-link type="warning" :underline="false" @click="$router.push('/feature/balance-deduct')">
          此为余额抵扣功能
        </el-link>
        <span class="row__hint">当前可用积分：{{ preview.userPoints || 0 }}</span>
      </div>

      <div class="row">
        <span class="row__label">订单备注</span>
        <el-input v-model="remark" placeholder="选填，如：工作日送货" maxlength="100" style="width: 420px" />
        <el-link type="info" :underline="false" class="ml12" @click="$router.push('/feature/order-remark')">
          此为订单备注功能
        </el-link>
      </div>

      <div class="row">
        <span class="row__label">发票信息</span>
        <el-link type="info" :underline="false" @click="$router.push('/feature/invoice')">
          此为发票信息功能
        </el-link>
      </div>
    </div>

    <!-- 金额汇总 -->
    <div class="abc-card block summary">
      <div class="summary__amounts">
        <div class="summary__line">
          <span>商品总额</span><MoneyText :value="preview.totalAmount" size="small" />
        </div>
        <div class="summary__line">
          <span>运费</span>
          <span v-if="Number(preview.freight) === 0" class="free">包邮</span>
          <MoneyText v-else :value="preview.freight" size="small" />
        </div>
        <div class="summary__line">
          <span>优惠抵扣</span>
          <span class="deduct">- <MoneyText :value="preview.discountAmount" size="small" /></span>
        </div>
        <div class="summary__line summary__line--total">
          <span>应付金额</span><MoneyText :value="preview.payAmount" size="large" />
        </div>
      </div>

      <div class="summary__submit">
        <div class="summary__addr">
          <template v-if="preview.address">
            寄送至：{{ fullAddress(preview.address) }}
            <br />
            收货人：{{ preview.address.receiver }} {{ preview.address.phone }}
          </template>
          <span v-else class="warn">请先选择收货地址</span>
        </div>
        <el-button type="primary" size="large" :loading="submitting" @click="submit">提交订单</el-button>
      </div>
    </div>

    <!-- 新增地址弹窗 -->
    <el-dialog v-model="addressDialog" title="新增收货地址" width="560px">
      <el-form :model="addressForm" label-width="90px">
        <el-form-item label="收货人" required>
          <el-input v-model="addressForm.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区">
          <div class="region">
            <el-input v-model="addressForm.province" placeholder="省" />
            <el-input v-model="addressForm.city" placeholder="市" />
            <el-input v-model="addressForm.district" placeholder="区/县" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item label="标签">
          <el-radio-group v-model="addressForm.tag">
            <el-radio-button label="家" />
            <el-radio-button label="公司" />
            <el-radio-button label="学校" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存并使用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Goods, Location, OfficeBuilding, Plus, Van } from '@element-plus/icons-vue'
import MoneyText from '@/components/MoneyText.vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { addressApi, orderApi } from '@/api'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(true)
const submitting = ref(false)
const preview = reactive({
  address: null,
  addressList: [],
  items: [],
  totalAmount: 0,
  freight: 0,
  discountAmount: 0,
  payAmount: 0,
  coupons: [],
  userPoints: 0,
  userBalance: 0
})
const couponId = ref(null)
const deliveryType = ref(1)
const remark = ref('')

const addressDialog = ref(false)
const addressForm = reactive({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  tag: '家',
  isDefault: 1
})

const cartItemIds = ref([])

const totalQuantity = computed(() =>
  preview.items.reduce((sum, i) => sum + (i.quantity || 0), 0)
)

const usableCoupons = computed(() => preview.coupons || [])

onMounted(async () => {
  // 结算范围：优先取「立即购买」带过来的条目ID，否则取购物车已勾选商品
  if (route.query.ids) {
    cartItemIds.value = [Number(route.query.ids)]
  } else {
    await cartStore.load()
    cartItemIds.value = cartStore.selectedItems.map((i) => i.id)
  }
  if (!cartItemIds.value.length) {
    ElMessage.warning('请先在购物车中勾选要结算的商品')
    router.replace('/cart')
    return
  }
  await loadPreview()
})

async function loadPreview() {
  loading.value = true
  try {
    const res = await orderApi.preview({
      cartItemIds: cartItemIds.value,
      userCouponId: couponId.value,
      deliveryType: deliveryType.value
    })
    Object.assign(preview, res)
    if (!couponId.value && res.selectedCoupon) {
      couponId.value = res.selectedCoupon.id
    }
  } catch (e) {
    router.replace('/cart')
  } finally {
    loading.value = false
  }
}

function onCouponChange() {
  loadPreview()
}

async function setDelivery(type) {
  if (type !== 1) {
    router.push(type === 2 ? '/feature/store-pickup' : '/feature/city-delivery')
    return
  }
  deliveryType.value = type
  await loadPreview()
}

async function chooseAddress(a) {
  preview.address = a
  try {
    const res = await orderApi.preview({
      cartItemIds: cartItemIds.value,
      userCouponId: couponId.value,
      deliveryType: deliveryType.value
    })
    res.address = a
    Object.assign(preview, res)
    preview.address = a
  } catch (e) {
    /* 忽略 */
  }
}

function fullAddress(a) {
  if (!a) return ''
  return `${a.province || ''}${a.city || ''}${a.district || ''}${a.detail || ''}`
}

function couponLabel(c) {
  const min = Number(c.minAmount || 0)
  const base =
    c.type === 2
      ? `${Number(c.discount)} 折券`
      : `满 ${min} 减 ${Number(c.amount || 0)}`
  const usable = c.usable === false ? `（${c.unusableReason || '不可用'}）` : `（可抵 ¥${Number(c.deductAmount || 0).toFixed(2)}）`
  return `${c.name || base}${usable}`
}

async function saveAddress() {
  if (!addressForm.receiver || !addressForm.phone || !addressForm.detail) {
    ElMessage.warning('请填写收货人、联系电话与详细地址')
    return
  }
  try {
    const saved = await addressApi.save({ ...addressForm })
    addressDialog.value = false
    ElMessage.success('地址已保存')
    const res = await orderApi.preview({
      cartItemIds: cartItemIds.value,
      userCouponId: couponId.value,
      deliveryType: deliveryType.value
    })
    Object.assign(preview, res)
    preview.address = saved
  } catch (e) {
    /* 已提示 */
  }
}

async function submit() {
  if (!preview.address) {
    ElMessage.warning('请先选择收货地址')
    return
  }
  submitting.value = true
  try {
    const order = await orderApi.submit({
      addressId: preview.address.id,
      cartItemIds: cartItemIds.value,
      deliveryType: deliveryType.value,
      userCouponId: couponId.value,
      remark: remark.value
    })
    ElMessage.success('下单成功，请尽快完成支付')
    userStore.refreshBadges()
    router.replace(`/pay/${order.order.id}`)
  } catch (e) {
    /* 已提示 */
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.page__head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 14px;
}
.page__head h2 {
  margin: 0;
  font-size: 22px;
}
.page__sub {
  color: var(--abc-text-3);
  font-size: 13px;
}
.block {
  padding: 18px 20px;
  margin-bottom: 14px;
}
.block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.block__title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 700;
}
.block__sub {
  color: var(--abc-text-3);
  font-size: 13px;
}
.addr-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.addr-item {
  border: 1px solid var(--abc-border);
  border-radius: 10px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.addr-item:hover {
  border-color: var(--abc-primary);
}
.addr-item.active {
  border-color: var(--abc-primary);
  background: var(--abc-primary-light);
}
.addr-item__top {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.addr-item__detail {
  margin-top: 6px;
  font-size: 13px;
  color: var(--abc-text-2);
  line-height: 20px;
}
.delivery {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.delivery__item {
  border: 1px solid var(--abc-border);
  border-radius: 10px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.delivery__item b {
  font-size: 14px;
}
.delivery__item span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.delivery__item.active {
  border-color: var(--abc-primary);
  background: var(--abc-primary-light);
}
.link-row {
  margin-top: 12px;
  display: flex;
  gap: 18px;
  font-size: 13px;
}
.goods__head,
.goods__row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.goods__head {
  height: 38px;
  font-size: 13px;
  color: var(--abc-text-3);
  border-bottom: 1px solid var(--abc-border);
}
.goods__row {
  padding: 14px 0;
  border-bottom: 1px solid #f5f6f8;
}
.g-col--info {
  flex: 1;
  display: flex;
  gap: 12px;
  min-width: 0;
}
.g-col--price,
.g-col--qty,
.g-col--total {
  width: 110px;
  text-align: center;
}
.goods__img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  flex-shrink: 0;
}
.goods__name {
  font-size: 14px;
  line-height: 20px;
}
.goods__sku {
  margin-top: 4px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  font-size: 13px;
}
.row__label {
  width: 80px;
  color: var(--abc-text-3);
  flex-shrink: 0;
}
.row__hint {
  color: var(--abc-text-3);
  margin-left: 12px;
}
.ml12 {
  margin-left: 12px;
}
.summary {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}
.summary__amounts {
  flex: 1;
  min-width: 280px;
}
.summary__line {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
  color: var(--abc-text-2);
  max-width: 360px;
}
.summary__line--total {
  border-top: 1px dashed var(--abc-border);
  margin-top: 8px;
  padding-top: 12px;
  font-size: 15px;
  color: var(--abc-text);
}
.free {
  color: var(--abc-success);
}
.deduct {
  color: var(--abc-primary);
}
.summary__submit {
  display: flex;
  align-items: center;
  gap: 20px;
}
.summary__addr {
  text-align: right;
  font-size: 13px;
  color: var(--abc-text-2);
  line-height: 22px;
}
.warn {
  color: #f56c6c;
}
.region {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  width: 100%;
}
@media (max-width: 1000px) {
  .addr-list,
  .delivery {
    grid-template-columns: 1fr;
  }
}
</style>
