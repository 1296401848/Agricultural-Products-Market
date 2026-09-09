<template>
  <div class="register-container">
    <div class="auth-wrapper">
      <!-- 品牌区域 -->
      <div class="brand-area">
        <div class="brand-icon">
          <el-icon :size="48"><Reading /></el-icon>
        </div>
        <h1 class="brand-name">农产品商城</h1>
        <p class="brand-desc">创建账号，开启阅读之旅</p>
      </div>

      <el-card class="auth-card" shadow="hover">
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-position="top" :hide-required-asterisk="true">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" prefix-icon="Message" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password size="large" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="Lock" show-password size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleRegister" :loading="loading" size="large" class="primary-btn">注 册</el-button>
          </el-form-item>
          <el-form-item>
            <div class="extra-links">
              <el-button type="primary" link @click="goToLogin">已有账号？立即登录</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/apis/Auth.js'
import { ElMessage } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{8,20}$/, message: '密码需包含英文字符和数字，长度8-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...registerData } = registerForm
        const response = await register(registerData)
        if (response.code === 200) {
          ElMessage.success('注册成功')
          router.push('/login')
        }
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
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
  background: linear-gradient(135deg, #3EB135 0%, #328E2A 100%);
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
  justify-content: center;
  width: 100%;
}

/* ========== 响应式 ========== */
@media (max-width: 480px) {
  .register-container {
    padding: 1rem 0.75rem;
    align-items: flex-start;
    padding-top: 2rem;
  }

  .brand-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
  }

  .brand-name {
    font-size: 1.5rem;
  }

  .brand-area {
    margin-bottom: 1.25rem;
  }

  .auth-card :deep(.el-card__body) {
    padding: 1.25rem 1rem;
  }
}
</style>