<template>
  <div class="login">
    <div class="login__box">
      <!-- 左侧品牌区 -->
      <div class="login__brand">
        <div class="login__logo">
          <span class="login__logo-mark">A</span>
          <div class="login__logo-text">
            <b>AbcMall</b>
            <small>阿帕茨商城</small>
          </div>
        </div>
        <h2 class="login__slogan">正品好物，一站购齐</h2>
        <p class="login__sub">3C 数码 · 家用电器 · 美妆个护 · 食品生鲜 · 服饰运动</p>
        <ul class="login__points">
          <li><el-icon><CircleCheckFilled /></el-icon> 新人注册立得 300 积分</li>
          <li><el-icon><CircleCheckFilled /></el-icon> 每日签到攒积分，积分当钱花</li>
          <li><el-icon><CircleCheckFilled /></el-icon> 满 99 元包邮，7 天无理由退换</li>
          <li><el-icon><CircleCheckFilled /></el-icon> 会员等级越高，专享权益越多</li>
        </ul>
      </div>

      <!-- 右侧表单区 -->
      <div class="login__form">
        <h3 class="login__title">账号登录</h3>
        <p class="login__hint">使用商城账号登录，Session 会话自动保持登录状态</p>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码"
                      :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login__submit" :loading="loading" @click="submit">
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login__links">
          <span>还没有账号？
            <el-link type="primary" :underline="false" @click="go('/register')">立即注册</el-link>
          </span>
          <el-link :underline="false" @click="go('/feature/change-password')">忘记密码？</el-link>
        </div>

        <el-divider><span class="login__divider-text">其它登录方式</span></el-divider>

        <div class="login__third">
          <el-tooltip content="此为第三方登录功能" placement="top">
            <el-button circle :icon="ChatDotRound" @click="go('/feature/third-login')" />
          </el-tooltip>
          <el-tooltip content="此为第三方登录功能" placement="top">
            <el-button circle :icon="Platform" @click="go('/feature/third-login')" />
          </el-tooltip>
          <el-tooltip content="此为第三方登录功能" placement="top">
            <el-button circle :icon="Iphone" @click="go('/feature/third-login')" />
          </el-tooltip>
        </div>

        <div class="login__demo">
          <span>演示账号：</span>
          <el-tag size="small" class="login__demo-tag" @click="fill('zhangsan')">zhangsan / 123456</el-tag>
          <el-tag size="small" type="success" class="login__demo-tag" @click="fill('lisi')">lisi / 123456</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Iphone, Lock, Platform, User } from '@element-plus/icons-vue'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
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
    const data = await authApi.login({ username: form.username.trim(), password: form.password })
    userStore.setInfo(data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect ? String(route.query.redirect) : '/'
    router.replace(redirect)
  } catch (e) {
    /* 统一拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function go(path) {
  router.push(path)
}

function fill(username) {
  form.username = username
  form.password = '123456'
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  background: linear-gradient(135deg, #fff2ec 0%, #f5f6f8 60%, #eef4ff 100%);
}
.login__box {
  width: 100%;
  max-width: 940px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.14);
}
.login__brand {
  padding: 48px 40px;
  color: #fff;
  background: linear-gradient(150deg, #ff5000, #ff9500 60%, #ff6a00);
}
.login__logo {
  display: flex;
  align-items: center;
  gap: 12px;
}
.login__logo-mark {
  width: 46px;
  height: 46px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.22);
}
.login__logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.login__logo-text b {
  font-size: 21px;
  letter-spacing: 0.5px;
}
.login__logo-text small {
  font-size: 12px;
  opacity: 0.85;
}
.login__slogan {
  margin: 34px 0 8px;
  font-size: 27px;
  letter-spacing: 1px;
}
.login__sub {
  margin: 0 0 30px;
  font-size: 13px;
  opacity: 0.9;
}
.login__points {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 14px;
  font-size: 13.5px;
}
.login__points li {
  display: flex;
  align-items: center;
  gap: 8px;
}
.login__form {
  padding: 48px 44px;
}
.login__title {
  margin: 0;
  font-size: 23px;
}
.login__hint {
  margin: 6px 0 26px;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.login__submit {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 22px;
}
.login__links {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--abc-text-2);
}
.login__divider-text {
  font-size: 12px;
  color: var(--abc-text-3);
}
.login__third {
  display: flex;
  justify-content: center;
  gap: 18px;
}
.login__demo {
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.login__demo-tag {
  cursor: pointer;
}
@media (max-width: 760px) {
  .login__box {
    grid-template-columns: 1fr;
  }
  .login__brand {
    display: none;
  }
}
</style>
