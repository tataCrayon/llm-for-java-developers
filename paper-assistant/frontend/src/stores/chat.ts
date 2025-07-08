import {defineStore} from 'pinia'
import {ref, computed} from 'vue'
import {chatApi} from '@/api/chat'
import {authApi} from '@/api/auth'
import type {ChatRequest, StreamResponse, ChatSession} from '@/types'
import {ElMessage} from 'element-plus'

export interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  loading?: boolean
}

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<string>('')
  const messagesMap = ref<Record<string, ChatMessage[]>>({})
  const loading = ref(false)
  const streaming = ref(false)

  const currentMessages = computed(() => messagesMap.value[currentSessionId.value] || [])

  // 加载会话列表（从后端）
  const loadSessions = async () => {
    try {
      const res = await authApi.getSessionList()
      sessions.value = res.data || []
      // 自动切换到第一个会话
      if (sessions.value.length > 0) {
        currentSessionId.value = sessions.value[0].sessionId
      }
    } catch (e) {
      ElMessage.error('加载会话列表失败')
    }
  }

  // 发送消息
  const sendMessage = async (content: string, sessionId?: string) => {
    if (!content.trim()) return
    const targetSessionId = sessionId || currentSessionId.value
    if (!targetSessionId) {
      ElMessage.error('请先新建会话')
      return
    }

    // 取 parentMsgId：优先上一条有 id 的助手消息，否则上一条有 id 的用户消息，否则 undefined
    function getParentMsgId(msgList: ChatMessage[]): number | undefined {
      for (let i = msgList.length - 1; i >= 0; i--) {
        const m = msgList[i];
        if (m.role === 'assistant' && typeof m.id === 'number') return m.id;
      }
      for (let i = msgList.length - 1; i >= 0; i--) {
        const m = msgList[i];
        if (m.role === 'user' && typeof m.id === 'number') return m.id;
      }
      return undefined;
    }

    // 记录每个会话最近一次 AI 响应的 msgId
    const lastAssistantMsgIdMap = ref<Record<string, number>>({})
    // 生成唯一的用户消息 id（取当前会话最大 id+1，或1）
    const msgList = messagesMap.value[targetSessionId] || []
    const maxId = msgList.reduce((max, m) => typeof m.id === 'number' && m.id > max ? m.id : max, 0)
    const userMsgId = maxId + 1
    // 添加用户消息
    const userMessage: ChatMessage = {
      id: userMsgId,
      role: 'user',
      content,
      timestamp: Date.now()
    }
    if (!messagesMap.value[targetSessionId]) messagesMap.value[targetSessionId] = []
    messagesMap.value[targetSessionId].push(userMessage)
    // 添加助手消息占位
    const assistantMessage: ChatMessage = {
      id: undefined as any, // 后续用后端 msgId 赋值
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      loading: true
    }
    messagesMap.value[targetSessionId].push(assistantMessage)
    loading.value = true
    try {
      // parentMsgId 逻辑：新建对话时为 1，后续取最近一次 AI 响应的 msgId
      let parentMsgId: number | undefined = 1
      if (lastAssistantMsgIdMap.value[targetSessionId]) {
        parentMsgId = lastAssistantMsgIdMap.value[targetSessionId]
      }
      console.log('sendMessage 参数:', {targetSessionId, content, parentMsgId})
      const request: ChatRequest = {
        sessionId: targetSessionId,
        parentMsgId: parentMsgId,
        userMessage: content,
        stream: true
      }
      let fullContent = ''
      const eventSource = chatApi.streamChat(request)
      eventSource.onmessage = (event) => {
        try {
          let data: any = JSON.parse(event.data)
          // 兼容 OpenAI/DeepSeek 风格 SSE
          let delta = ''
          if (data.choices && data.choices[0] && data.choices[0].delta && typeof data.choices[0].delta.content === 'string') {
            delta = data.choices[0].delta.content
          } else if (data.status === 'generating' && data.delta) {
            delta = data.delta
          }
          if (delta) {
            fullContent += delta
            assistantMessage.content = fullContent
            assistantMessage.loading = true
            // 强制响应式刷新
            const idx = messagesMap.value[targetSessionId].indexOf(assistantMessage)
            if (idx !== -1) {
              messagesMap.value[targetSessionId].splice(idx, 1, {...assistantMessage})
            }
            console.log('delta', delta, assistantMessage)
          }
          if (data.status === 'finished' || (data.choices && data.choices[0] && data.choices[0].finish_reason)) {
            assistantMessage.content = data.fullText || fullContent
            assistantMessage.loading = false
            // 用后端返回的 msgId 记录到 lastAssistantMsgIdMap，保证下次 parentMsgId 连贯
            if (typeof data.msgId === 'number') {
              assistantMessage.id = data.msgId
              lastAssistantMsgIdMap.value[targetSessionId] = data.msgId
            }
            // 最后再强制刷新一次
            const idx = messagesMap.value[targetSessionId].indexOf(assistantMessage)
            if (idx !== -1) {
              messagesMap.value[targetSessionId].splice(idx, 1, {...assistantMessage})
            }
            eventSource.close()
            streaming.value = false
            saveCurrentSession()
          }
        } catch (error) {
          console.error('解析流式响应失败:', error)
        }
      }
      eventSource.onerror = (error) => {
        console.error('EventSource错误:', error)
        assistantMessage.loading = false
        ElMessage.error('连接失败')
        eventSource.close()
        streaming.value = false
      }
    } catch (error) {
      ElMessage.error('发送消息失败')
      assistantMessage.loading = false
      streaming.value = false
    } finally {
      loading.value = false
    }
  }

  // 新建会话（从后端获取sessionId并保存）
  const createSession = async (title: string = '新对话') => {
    try {
      const sessionId = await authApi.createSession()
      const session: ChatSession = {
        sessionId,
        title: title + ' ' + (sessions.value.length + 1),
        lastMessage: '',
        timestamp: Date.now(),
        messageCount: 0
      }
      // 保存到后端
      await authApi.saveSession(session)
      // 重新加载会话列表
      await loadSessions()
      currentSessionId.value = sessionId
      messagesMap.value[sessionId] = []
      return sessionId
    } catch (e) {
      ElMessage.error('创建会话失败')
      return null
    }
  }

  // 保存当前会话到后端
  const saveCurrentSession = async () => {
    const session = sessions.value.find(s => s.sessionId === currentSessionId.value)
    if (session) {
      // 可根据实际需求更新lastMessage、messageCount等
      session.lastMessage = (messagesMap.value[session.sessionId] || []).slice(-1)[0]?.content || ''
      session.messageCount = (messagesMap.value[session.sessionId] || []).length
      session.timestamp = Date.now()
      await authApi.saveSession(session)
    }
  }

  // 切换会话
  const switchSession = (sessionId: string) => {
    currentSessionId.value = sessionId
  }

  // 清空当前会话消息
  const clearMessages = () => {
    if (currentSessionId.value) messagesMap.value[currentSessionId.value] = []
  }

  return {
    sessions,
    currentSessionId,
    messagesMap,
    loading,
    streaming,
    currentMessages,
    sendMessage,
    createSession,
    switchSession,
    clearMessages,
    loadSessions
  }
})
