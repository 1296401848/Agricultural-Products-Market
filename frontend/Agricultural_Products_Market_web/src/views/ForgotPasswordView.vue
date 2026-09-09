<template>
  <div class="forgot-container">
    <div class="auth-wrapper">
      <!-- 品牌区域 -->
      <div class="brand-area">
        <div class="brand-icon">
          <el-icon :size="48"><Lock /></el-icon>
        </div>
        <h1 class="brand-name">找回密码</h1>
        <p class="brand-desc">请输入注册邮箱获取验证码</p>
      </div>

      <el-card class="auth-card" shadow="hover">
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" :hide-required-asterisk="true">
          <el-form-item label="注册邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入注册时使用的邮箱" prefix-icon="Message" size="large" />
          </el-form-item>

          <el-form-item label="验证码" prop="code">
            <el-input v-model="form.code" placeholder="请输入6位验证码" maxlength="6" size="large">
              <template #append>
                <el-button
                  type="primary"
                  @click="showCaptchaDialog"
                  :loading="sending"
                  :disabled="countdown > 0"
                  class="send-code-btn"
                >
                  {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="form.newPassword" type="password" placeholder="英文字符+数字，8-20位" prefix-icon="Lock" show-password size="large" />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" prefix-icon="Lock" show-password size="large" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleReset" :loading="resetting" size="large" class="primary-btn">
              重置密码
            </el-button>
          </el-form-item>

          <el-form-item>
            <div class="extra-links">
              <el-button type="primary" link @click="goToLogin">返回登录</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 图形验证码弹窗 -->
    <el-dialog v-model="captchaVisible" title="人机校验" width="360px" :close-on-click-modal="false" append-to-body>
      <div class="captcha-dialog-body">
        <p class="captcha-tip">请输入图片中的验证码</p>
        <div class="captcha-image-box" @click="refreshCaptcha">
          <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="captcha-img" />
          <el-icon v-else :size="32" class="captcha-loading"><Loading /></el-icon>
          <span class="captcha-hint">点击图片刷新</span>
        </div>
        <el-input v-model="captchaInput" placeholder="请输入验证码" maxlength="4" size="large" class="captcha-input" @keyup.enter="confirmCaptcha" />
      </div>
      <template #footer>
        <el-button @click="captchaVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCaptcha" :loading="captchaLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCaptcha, sendResetCode, resetPassword } from '@/apis/Auth.js'
import { ElMessage } from 'element-plus'
import { Lock, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref()

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const sending = ref(false)
const resetting = ref(false)
const countdown = ref(0)
let timer = null

// 图形验证码
const captchaVisible = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')
const captchaInput = ref('')
const captchaLoading = ref(false)

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{8,20}$/, message: '密码需包含英文字符和数字，长度8-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取图形验证码
const fetchCaptcha = async () => {
  try {
    const response = await getCaptcha()
    if (response.code === 200) {
      captchaKey.value = response.data.captchaKey
      captchaImage.value = response.data.captchaImage
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  captchaInput.value = ''
  fetchCaptcha()
}

// 显示图形验证码弹窗
const showCaptchaDialog = async () => {
  // 先校验邮箱
  if (!formRef.value) return
  await formRef.value.validateField('email', async (valid) => {
    if (!valid) return
    captchaInput.value = ''
    captchaVisible.value = true
    fetchCaptcha()
  })
}

// 确认图形验证码 → 发送邮箱验证码
const confirmCaptcha = async () => {
  if (!captchaInput.value) {
    ElMessage.warning('请输入验证码')
    return
  }
  captchaLoading.value = true
  try {
    await sendResetCode({
      email: form.email,
      captchaKey: captchaKey.value,
      captchaCode: captchaInput.value
    })
    ElMessage.success('验证码已发送，请查收邮件')
    captchaVisible.value = false
    startCountdown()
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error?.response?.data?.msg || '验证失败')
    refreshCaptcha()
  } finally {
    captchaLoading.value = false
  }
}

// 启动倒计时
const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

// 重置密码
const handleReset = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    resetting.value = true
    try {
      await resetPassword({
        email: form.email,
        code: form.code,
        newPassword: form.newPassword
      })
      ElMessage.success('密码重置成功，请使用新密码登录')
      router.push('/login')
    } catch (error) {
      console.error('密码重置失败:', error)
      ElMessage.error(error?.response?.data?.msg || '重置失败')
    } finally {
      resetting.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 1rem;
}

.auth-wrapper {
  width: 100%;
  max-width: 460px;
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
  background: linear-gradient(135deg, #e6a23c 0%, #cf9236 100%);
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

/* 验证码输入框内嵌按钮 */
.send-code-btn {
  height: 100%;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  white-space: nowrap;
  font-size: 0.9rem;
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

/* 图形验证码弹窗 */
.captcha-dialog-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.captcha-tip {
  color: #606266;
  margin: 0;
}

.captcha-image-box {
  position: relative;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 56px;
  background: #fafafa;
}

.captcha-img {
  display: block;
  height: 48px;
}

.captcha-hint {
  position: absolute;
  bottom: 2px;
  right: 6px;
  font-size: 0.7rem;
  color: #c0c4cc;
}

.captcha-loading {
  color: #c0c4cc;
}

.captcha-input {
  width: 100%;
}

/* ========== 响应式 ========== */
@media (max-width: 480px) {
  .forgot-container {
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

  .send-code-btn {
    font-size: 0.8rem;
    padding: 0 0.75rem;
  }
}
</style>
