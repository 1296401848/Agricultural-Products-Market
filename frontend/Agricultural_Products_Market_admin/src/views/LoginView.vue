<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">农产品商城管理端</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/apis/auth.js'
import { useTokenStore } from '@/stores/token.js'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)
const tokenStore = useTokenStore()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{8,20}$/, message: '密码需包含英文字符和数字，长度8-20位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      login(loginForm)
        .then(res => {
          if (res.code === 200) {
            tokenStore.setToken(res.data.token)
            userStore.setUserInfo(res.data)
            ElMessage.success('登录成功')
            router.push('/user/list')
          } else {
            ElMessage.error(res.msg || '登录失败')
          }
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 1rem;
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 2.5rem 2rem;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 2rem;
  color: #303133;
}

.login-btn {
  width: 100%;
}

@media (max-width: 480px) {
  .login-box { padding: 1.5rem 1rem; }
  .login-title { font-size: 1.3rem; margin-bottom: 1.5rem; }
}
</style>
