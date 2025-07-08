import axios from 'axios'
import type {AxiosInstance, InternalAxiosRequestConfig, AxiosResponse} from 'axios'
import {ElMessage} from 'element-plus'
import router from '@/router'

// 创建axios实例
const api: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 登录接口不需要token
    const isLoginApi = config.url?.includes('/user/auth')
    if (isLoginApi) {
      return config
    }
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    } else {
      if (router.currentRoute.value.path !== '/login') {
        ElMessage.warning('请先登录')
        router.replace('/login')
      }
      return Promise.reject(new Error('未登录，无token'))
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse) => {
    const {data} = response
    // 处理API响应格式
    if (data && typeof data === 'object') {
      if (data.code === 200) {
        return data
      } else {
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    }
    return data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default api
