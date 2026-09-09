<template>
  <div class="login-container">
    <div class="auth-wrapper">
      <!-- 品牌区域 -->
      <div class="brand-area">
        <div class="brand-icon">
          <el-icon :size="48"><Reading /></el-icon>
        </div>
        <h1 class="brand-name">农产品商城</h1>
        <p class="brand-desc">欢迎回来，请登录您的账号</p>
      </div>

      <el-card class="auth-card" shadow="hover">
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top" :hide-required-asterisk="true">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" :loading="loading" size="large" class="primary-btn">登 录</el-button>
          </el-form-item>
          <el-form-item>
            <div class="extra-links">
              <el-button type="primary" link @click="goToRegister">还没有账号？立即注册</el-button>
              <el-button type="primary" link @click="goToForgotPassword">忘记密码</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 联系弹窗 -->
    <el-dialog v-model="contactVisible" title="账号已被冻结" width="400px" append-to-body>
      <div class="contact-body">
        <p class="contact-text">您的账号已被冻结，如需解冻请联系管理员：</p>
        <div class="contact-email">1296401848@qq.com</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { useUserStore } from '@/stores/user.js'
import { login } from '@/apis/Auth.js'
import { Reading } from '@element-plus/icons-vue'

const router = useRouter()
const tokenStore = useTokenStore()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)
const contactVisible = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{8,20}$/, message: '密码需包含英文字符和数字，长度8-20位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await login(loginForm)
        if (response.code === 200) {
          // 保存token
          tokenStore.setToken(response.data.token)
          // 保存用户信息
          userStore.setUserInfo({
            id: response.data.id,
            username: response.data.username,
            role: response.data.role,
            email: response.data.email,
            status: response.data.status,
            createTime: response.data.createTime
          })
          router.push('/')
        }
      } catch (error) {
        console.error('登录失败:', error)
        // 账号被冻结时弹出联系弹窗
        const msg = error?.response?.data?.msg || error?.message || ''
        if (msg.includes('禁用') || msg.includes('冻结')) {
          contactVisible.value = true
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  router.push('/register')
}

const goToForgotPassword = () => {
  router.push('/forgot-password')
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

.auth-wrapper {
  width: 100%;
  max-width: 420px;
}

/* 品牌区域 */
.brand-area {
  text-align: center;
  margin-bottom: 2rem;
}

.brand-icon {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border-radius: 20px;
  color: #fff;
  margin-bottom: 1rem;
}

.brand-name {
  font-size: 1.8rem;
  font-weight: bold;
  color: #303133;
  margin: 0 0 0.5rem 0;
}

.brand-desc {
  font-size: 0.95rem;
  color: #909399;
  margin: 0;
}

/* 卡片 */
.auth-card {
  border-radius: 12px;
}

.auth-card :deep(.el-card__body) {
  padding: 2rem 1.5rem;
}

/* 表单 */
.primary-btn {
  width: 100%;
  letter-spacing: 4px;
  font-size: 1.05rem;
}

.extra-links {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

/* ========== 响应式 ========== */
/* 联系弹窗 */
.contact-body {
  text-align: center;
  padding: 1rem 0;
}

.contact-text {
  color: #606266;
  margin: 0 0 1rem 0;
}

.contact-email {
  font-size: 1.2rem;
  font-weight: bold;
  color: #409eff;
  padding: 0.75rem 1.5rem;
  background: #ecf5ff;
  border-radius: 8px;
  display: inline-block;
}

@media (max-width: 480px) {
  .login-container {
    padding: 1rem 0.75rem;
    align-items: flex-start;
    padding-top: 3rem;
  }

  .brand-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
  }

  .brand-name {
    font-size: 1.5rem;
  }

  .auth-card :deep(.el-card__body) {
    padding: 1.25rem 1rem;
  }

  .extra-links {
    flex-direction: column;
    align-items: center;
    gap: 0.25rem;
  }
}
</style>