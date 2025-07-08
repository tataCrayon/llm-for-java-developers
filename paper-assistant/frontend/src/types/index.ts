// API响应基础类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 用户认证相关类型
export interface AuthRequest {
  username: string
  password: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken?: string
  scope?: string
}

// 聊天相关类型
export interface ChatRequest {
  sessionId: string
  parentMsgId?: number
  userMessage: string
  stream: boolean
  options?: ChatReqOptions
}

export interface ChatReqOptions {
  temperature?: number
  maxTokens?: number
  topP?: number
}

export interface ChatResponse {
  sessionId: string
  msgId: number
  content: string
  timestamp: number
}

export interface StreamResponse {
  sessionId?: string
  msgId?: number
  delta?: string
  fullText?: string
  status: 'generating' | 'finished' | 'error'
  tokenUsed?: number
  error?: string
  features?: {
    canShare: boolean
    canFeedback: boolean
  }
}

// 论文相关类型
export interface PaperAnalysisRequest {
  title: string
  authors?: string[]
  abstractText?: string
  url?: string
  publishedDate?: string
  category?: string
  analysisAspects?: string[]
}

export interface PaperComparisonRequest {
  papers: PaperAnalysisRequest[]
  comparisonAspects?: string[]
  comparisonPurpose?: string
}

export interface LearningPlanRequest {
  userBackground: string
  learningGoals: string
  weeklyHours?: number
  targetWeeks?: number
  interestedTopics?: string[]
  learningPreferences?: string[]
}

// 用户会话类型
export interface ChatSession {
  sessionId: string
  title: string
  lastMessage: string
  timestamp: number
  messageCount: number
}

// 用户信息类型
export interface User {
  id: number
  username: string
  email?: string
  avatar?: string
  createdAt: string
} 