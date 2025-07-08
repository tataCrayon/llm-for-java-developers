<template>
  <div class="login-bg">
    <div class="login-brand-bar"></div>
    <div class="login-container">
      <el-card class="login-card">
        <h2 class="title"><span class="brand">LLM 论文助手</span> 登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px"
                 @keyup.enter="onLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" class="login-input" placeholder="请输入用户名"
                      size="large"/>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" class="login-input" placeholder="请输入密码" size="large"
                      type="password"/>
          </el-form-item>
          <el-form-item>
            <el-button :loading="loading" class="login-btn" size="large" type="primary"
                       @click="onLogin">登录
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {useAuthStore} from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const formRef = ref()
const form = ref({
  username: '',
  password: ''
})
const rules = {
  username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}]
}

const onLogin = async () => {
  loading.value = true
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      const success = await authStore.login(form.value)
      if (success) {
        router.push('/dashboard')
      }
    }
    loading.value = false
  })
}
</script>

<style scoped>
.login-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #e0e7ff 0%, #f5f6fa 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.login-brand-bar {
  width: 100vw;
  height: 6px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  margin-bottom: 0;
}

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100vw;
}

.login-card {
  width: 420px;
  max-width: 96vw;
  padding: 48px 40px 36px 40px;
  box-shadow: 0 8px 32px rgba(80, 80, 120, 0.10);
  border-radius: 20px;
  background: #fff;
  transition: box-shadow 0.2s;
  border: 1.5px solid #e0f3ff;
}

.title {
  text-align: center;
  margin-bottom: 32px;
  color: #222;
  font-weight: 700;
  font-size: 28px;
  letter-spacing: 1px;
}

.brand {
  color: #409eff;
  font-weight: 900;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.login-input {
  border-radius: 10px;
  box-shadow: 0 1px 8px rgba(64, 158, 255, 0.08);
  font-size: 16px;
}

.login-btn {
  border-radius: 10px;
  font-weight: 600;
  padding: 0 22px;
  height: 44px;
  font-size: 18px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  border: none;
  color: #fff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  transition: box-shadow 0.18s, transform 0.18s;
}

.login-btn:hover {
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.18);
  transform: translateY(-2px) scale(1.03);
}

@media (max-width: 600px) {
  .login-card {
    width: 98vw;
    padding: 32px 8vw 24px 8vw;
    border-radius: 12px;
  }

  .title {
    font-size: 22px;
  }
}
</style>
