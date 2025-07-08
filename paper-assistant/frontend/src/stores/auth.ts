import {defineStore} from 'pinia'
import {ref, computed} from 'vue'
import {authApi} from '@/api/auth'
import type {AuthRequest, User} from '@/types'
import {ElMessage} from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  // 登录
  const login = async (credentials: AuthRequest) => {
    loading.value = true
    try {
      const response = await authApi.login(credentials)
      // 适配后端只返回 accessToken
      const newToken = response.data.accessToken
      if (!newToken) throw new Error('未获取到 accessToken')
      token.value = newToken
      localStorage.setItem('token', newToken)
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      ElMessage.error('登录失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 创建会话
  const createSession = async () => {
    // ...如有需要可实现
    return null
  }

  // 登出
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    ElMessage.success('已退出登录')
  }

  // 初始化认证状态
  const initAuth = () => {
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
      token.value = savedToken
    }
  }

  return {
    token,
    user,
    loading,
    isAuthenticated,
    login,
    createSession,
    logout,
    initAuth
  }
})
