<template>
  <div v-loading="loading" class="prof">
    <div class="prof__head">
      <h2>个人资料</h2>
      <p>完善资料可获得更精准的推荐与服务</p>
    </div>

    <div class="prof__avatar-row">
      <div class="prof__avatar abc-img-box">
        <img :src="form.avatar || fallbackAvatar" alt="头像" />
      </div>
      <div class="prof__avatar-info">
        <b>{{ form.nickname || form.username }}</b>
        <span>账号：{{ form.username }}</span>
        <div class="prof__avatar-tags">
          <el-tag size="small" effect="dark" type="warning" round>{{ levelName }}</el-tag>
          <el-tag size="small" :type="form.isVerified === 1 ? 'success' : 'info'" round>
            {{ form.isVerified === 1 ? '已实名认证' : '未实名认证' }}
          </el-tag>
        </div>
      </div>
      <el-button round @click="go('/feature/real-name')">去实名认证</el-button>
    </div>

    <el-form ref="formRef" :model="form" label-width="88px" class="prof__form">
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
          <el-radio :value="0">保密</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="出生日期">
        <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD"
                        placeholder="选择出生日期" style="width: 220px" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="用于接收订单与发票通知" style="max-width: 340px" />
      </el-form-item>
      <el-form-item label="手机号">
        <div class="prof__phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" style="max-width: 340px" />
          <el-button round @click="go('/feature/bind-phone')">更换绑定</el-button>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" round :loading="saving" @click="save">保存资料</el-button>
        <el-button round @click="load">重置</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <div class="prof__pwd">
      <div class="prof__pwd-head">
        <h3>修改密码</h3>
        <span class="prof__pwd-tip">修改成功后需要重新登录</span>
      </div>
      <el-form ref="pwdRef" :model="pwd" label-width="88px" style="max-width: 520px">
        <el-form-item label="原密码">
          <el-input v-model="pwd.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwd.newPassword" type="password" show-password placeholder="6-20 位新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwd.confirm" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round :loading="pwdSaving" @click="changePwd">确认修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { placeholderImage, userApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const fallbackAvatar = placeholderImage('头像', 120, 120, 3)

const loading = ref(true)
const saving = ref(false)
const pwdSaving = ref(false)
const formRef = ref(null)
const pwdRef = ref(null)

const form = reactive({
  username: '',
  nickname: '',
  avatar: '',
  gender: 0,
  birthday: '',
  email: '',
  phone: '',
  isVerified: 0
})

const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const levelName = computed(() => userStore.levelName || '普通会员')

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = (await userApi.profile()) || {}
    Object.assign(form, {
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar,
      gender: data.gender ?? 0,
      birthday: data.birthday ? String(data.birthday).slice(0, 10) : '',
      email: data.email,
      phone: data.phone,
      isVerified: data.isVerified ?? 0
    })
  } catch (e) {
    /* 已提示 */
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await userApi.updateProfile({
      nickname: form.nickname,
      avatar: form.avatar,
      gender: form.gender,
      birthday: form.birthday || null,
      email: form.email,
      phone: form.phone
    })
    ElMessage.success('资料已更新')
    await userStore.refreshInfo()
  } catch (e) {
    /* 已提示 */
  } finally {
    saving.value = false
  }
}

async function changePwd() {
  if (!pwd.oldPassword || !pwd.newPassword) {
    ElMessage.warning('请填写原密码与新密码')
    return
  }
  if (pwd.newPassword.length < 6 || pwd.newPassword.length > 20) {
    ElMessage.warning('新密码长度需为 6-20 位')
    return
  }
  if (pwd.newPassword !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  pwdSaving.value = true
  try {
    await userApi.changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    await userStore.logout()
    router.push('/login')
  } catch (e) {
    /* 已提示 */
  } finally {
    pwdSaving.value = false
  }
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.prof__head h2 {
  margin: 0;
  font-size: 20px;
}
.prof__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.prof__avatar-row {
  display: flex;
  align-items: center;
  gap: 18px;
  margin: 20px 0 26px;
  padding: 18px 20px;
  border-radius: 12px;
  background: #fafbfc;
  border: 1px solid var(--abc-border);
}
.prof__avatar {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
}
.prof__avatar-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.prof__avatar-info b {
  font-size: 16px;
}
.prof__avatar-info span {
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.prof__avatar-tags {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.prof__phone {
  display: flex;
  align-items: center;
  gap: 12px;
}
.prof__pwd-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;
}
.prof__pwd-head h3 {
  margin: 0;
  font-size: 17px;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
.prof__pwd-tip {
  font-size: 12.5px;
  color: var(--abc-text-3);
}
</style>
