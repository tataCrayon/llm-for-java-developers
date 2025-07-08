import api from './index'
import type {ChatRequest, ChatResponse, StreamResponse, ApiResponse} from '@/types'

export const chatApi = {
  // 普通聊天
  chat(data: ChatRequest): Promise<ChatResponse> {
    return api.post('/ds/chat', data)
  },

  // 流式聊天（POST SSE，参数为ChatRequest，token从localStorage获取）
  streamChat(data: ChatRequest) {
    const url = api.defaults.baseURL + '/ds/stream/chat'
    const token = localStorage.getItem('token') || ''
    // 自定义事件对象，兼容 onmessage/onerror/close
    let closed = false
    const listeners: any = {}
    const eventSource = {
      onmessage: null as ((event: MessageEvent) => void) | null,
      onerror: null as ((err: any) => void) | null,
      close: () => {
        closed = true
      },
    }
    console.log('streamChat 请求体:', data)
    fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': token
      },
      body: JSON.stringify(data)
    }).then(async (res) => {
      if (!res.body) throw new Error('无响应体')
      const reader = res.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      while (!closed) {
        const {value, done} = await reader.read()
        if (done) break
        buffer += decoder.decode(value, {stream: true})
        let idx
        while ((idx = buffer.indexOf('\n')) !== -1) {
          const line = buffer.slice(0, idx).trim()
          buffer = buffer.slice(idx + 1)
          if (line) {
            try {
              // SSE: data:xxx\n
              const jsonStr = line.startsWith('data:') ? line.slice(5).trim() : line
              const event = {data: jsonStr}
              eventSource.onmessage && eventSource.onmessage(event as MessageEvent)
            } catch (e) {
              eventSource.onerror && eventSource.onerror(e)
            }
          }
        }
      }
      reader.releaseLock()
    }).catch(err => {
      eventSource.onerror && eventSource.onerror(err)
    })
    return eventSource
  }
}
