import api from './index'
import type {AuthRequest, AuthResponse, ApiResponse, ChatSession} from '@/types'

export const authApi = {
  // 用户登录
  login(data: AuthRequest): Promise<ApiResponse<AuthResponse>> {
    return api.post('/user/auth', data)
  },

  // 创建会话
  createSession(): Promise<string> {
    return api.get('/session/create')
  },

  // 获取会话列表
  getSessionList(): Promise<ApiResponse<ChatSession[]>> {
    return api.get('/session/list')
  },

  // 保存/更新会话
  saveSession(session: ChatSession): Promise<ApiResponse<void>> {
    return api.post('/session/save', session)
  }
}
