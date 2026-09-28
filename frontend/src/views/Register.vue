<template>
  <div class="reg">
    <div class="reg__box">
      <div class="reg__head">
        <span class="reg__logo-mark">A</span>
        <div>
          <h2>注册 AbcMall 阿帕茨商城</h2>
          <p>注册即送 300 积分，可到积分商城兑换好礼</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" label-width="0" class="reg__form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（4-20 位字母、数字、下划线）" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-20 位）" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" :prefix-icon="Postcard" clearable />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填，用于接收订单通知）" :prefix-icon="Iphone" clearable />
        </el-form-item>

        <el-form-item prop="agree">
          <el-checkbox v-model="form.agree">
            我已阅读并同意《用户服务协议》与《隐私政策》
          </el-checkbox>
        </el-form-item>

        <el-button type="primary" class="reg__submit" :loading="loading" @click="submit">
          注 册
        </el-button>

        <div class="reg__foot">
          已有账号？
          <el-link type="primary" :underline="false" @click="go('/login')">直接登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, Postcard, User } from '@element-plus/icons-vue'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  agree: true
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度需为 4-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需为 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        if (value !== form.password) {
          cb(new Error('两次输入的密码不一致'))
        } else {
          cb()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  agree: [
    {
      validator: (rule, value, cb) => {
        if (!value) {
          cb(new Error('请先阅读并同意用户协议'))
        } else {
          cb()
        }
      },
      trigger: 'change'
    }
  ]
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const data = await authApi.register({
      username: form.username.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword,
      nickname: form.nickname.trim(),
      phone: form.phone.trim()
    })
    userStore.setInfo(data)
    ElMessage.success('注册成功，欢迎加入阿帕茨商城')
    router.replace('/')
  } catch (e) {
    /* 已提示 */
  } finally {
    loading.value = false
  }
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.reg {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  background: linear-gradient(135deg, #fff2ec 0%, #f5f6f8 60%, #eef4ff 100%);
}
.reg__box {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 18px;
  padding: 40px 44px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.14);
}
.reg__head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}
.reg__logo-mark {
  width: 48px;
  height: 48px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  box-shadow: 0 8px 18px rgba(255, 80, 0, 0.3);
  flex-shrink: 0;
}
.reg__head h2 {
  margin: 0;
  font-size: 20px;
}
.reg__head p {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.reg__submit {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 22px;
  margin-top: 4px;
}
.reg__foot {
  margin-top: 18px;
  text-align: center;
  font-size: 13px;
  color: var(--abc-text-2);
}
</style>
