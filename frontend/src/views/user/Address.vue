<template>
  <div v-loading="loading" class="addr">
    <div class="addr__head">
      <div>
        <h2>收货地址</h2>
        <p>最多可保存 20 个收货地址，结算时可快速选择</p>
      </div>
      <el-button type="primary" round :icon="Plus" @click="openDialog()">新增地址</el-button>
    </div>

    <div class="addr__list">
      <div v-for="a in list" :key="a.id" class="addr__item" :class="{ 'addr__item--default': a.isDefault === 1 }">
        <div class="addr__item-main">
          <div class="addr__item-top">
            <b>{{ a.receiver }}</b>
            <span class="addr__item-phone">{{ a.phone }}</span>
            <el-tag v-if="a.tag" size="small" type="info" round>{{ a.tag }}</el-tag>
            <el-tag v-if="a.isDefault === 1" size="small" type="warning" effect="dark" round>默认</el-tag>
          </div>
          <p class="addr__item-detail">
            {{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}
            <span v-if="a.zipCode" class="addr__zip">邮编 {{ a.zipCode }}</span>
          </p>
        </div>
        <div class="addr__item-ops">
          <el-link v-if="a.isDefault !== 1" :underline="false" @click="setDefault(a)">设为默认</el-link>
          <el-link :underline="false" type="primary" @click="openDialog(a)">编辑</el-link>
          <el-link :underline="false" type="danger" @click="remove(a)">删除</el-link>
        </div>
      </div>
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="Location" text="还没有收货地址，先添加一个吧"
              action-text="新增地址" @action="openDialog()" />

    <el-dialog v-model="dialog" :title="editId ? '编辑收货地址' : '新增收货地址'" width="560px" top="8vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入 11 位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" prop="province">
          <div class="addr__region">
            <el-input v-model="form.province" placeholder="省 / 直辖市" />
            <el-input v-model="form.city" placeholder="市" />
            <el-input v-model="form.district" placeholder="区 / 县" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="街道、门牌号、楼层等" />
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="form.zipCode" placeholder="选填" style="max-width: 200px" />
        </el-form-item>
        <el-form-item label="标签">
          <el-radio-group v-model="form.tag">
            <el-radio-button value="家">家</el-radio-button>
            <el-radio-button value="公司">公司</el-radio-button>
            <el-radio-button value="学校">学校</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="dialog = false">取消</el-button>
        <el-button type="primary" round :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { addressApi } from '@/api'

const loading = ref(true)
const saving = ref(false)
const list = ref([])
const dialog = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  zipCode: '',
  tag: '家',
  isDefault: 0
})

const rules = {
  receiver: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请填写所在地区', trigger: 'blur' }],
  detail: [{ required: true, message: '请填写详细地址', trigger: 'blur' }]
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    list.value = (await addressApi.list()) || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(a) {
  editId.value = a ? a.id : null
  Object.assign(form, {
    receiver: a ? a.receiver : '',
    phone: a ? a.phone : '',
    province: a ? a.province : '',
    city: a ? a.city : '',
    district: a ? a.district : '',
    detail: a ? a.detail : '',
    zipCode: a ? a.zipCode : '',
    tag: a && a.tag ? a.tag : '家',
    isDefault: a ? a.isDefault ?? 0 : list.value.length === 0 ? 1 : 0
  })
  dialog.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    if (editId.value) {
      await addressApi.update(editId.value, { ...form })
    } else {
      await addressApi.save({ ...form })
    }
    ElMessage.success(editId.value ? '地址已更新' : '地址已添加')
    dialog.value = false
    await load()
  } catch (e) {
    /* 已提示 */
  } finally {
    saving.value = false
  }
}

async function setDefault(a) {
  try {
    await addressApi.setDefault(a.id)
    ElMessage.success('已设为默认地址')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}

async function remove(a) {
  try {
    await ElMessageBox.confirm(`确定删除「${a.receiver}」的收货地址吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await addressApi.remove(a.id)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}
</script>

<style scoped>
.addr__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.addr__head h2 {
  margin: 0;
  font-size: 20px;
}
.addr__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.addr__list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.addr__item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px;
  border: 1px solid var(--abc-border);
  border-radius: 12px;
  transition: all 0.2s;
}
.addr__item:hover {
  border-color: var(--abc-primary);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}
.addr__item--default {
  border-color: rgba(255, 80, 0, 0.45);
  background: #fffaf7;
}
.addr__item-main {
  min-width: 0;
  flex: 1;
}
.addr__item-top {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.addr__item-top b {
  font-size: 15.5px;
}
.addr__item-phone {
  font-size: 13.5px;
  color: var(--abc-text-2);
}
.addr__item-detail {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--abc-text-2);
  line-height: 1.6;
}
.addr__zip {
  margin-left: 8px;
  color: var(--abc-text-3);
  font-size: 12px;
}
.addr__item-ops {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
  font-size: 13px;
  flex-shrink: 0;
}
.addr__region {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  width: 100%;
}
@media (max-width: 900px) {
  .addr__list {
    grid-template-columns: 1fr;
  }
}
</style>
