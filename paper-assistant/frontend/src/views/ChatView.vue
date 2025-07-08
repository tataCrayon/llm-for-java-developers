<template>
  <div class="chat-gemini-root">
    <aside class="chat-gemini-sidebar">
      <div class="sidebar-header">
        <span class="sidebar-logo" style="cursor:pointer;" @click="goHome">LLM</span>
        <span class="sidebar-title">论文助手</span>
      </div>
      <el-button class="sidebar-new-btn" type="primary" @click="onCreateSession">新建对话
      </el-button>
      <div class="sidebar-session-list">
        <el-scrollbar height="calc(100vh - 120px)">
          <el-menu :default-active="chatStore.currentSessionId" class="sidebar-menu"
                   @select="onSwitchSession">
            <el-menu-item v-for="session in chatStore.sessions" :key="session.sessionId"
                          :index="session.sessionId">
              <div :title="session.title" class="session-title">{{ session.title }}</div>
              <div class="session-time">{{ formatTime(session.timestamp) }}</div>
            </el-menu-item>
          </el-menu>
        </el-scrollbar>
      </div>
    </aside>
    <main class="chat-gemini-main">
      <section ref="messagesRef" class="chat-gemini-messages">
        <transition-group name="fade" tag="div">
          <div v-for="msg in messages" :key="msg.id" :class="['chat-message', msg.role]">
            <div class="avatar-wrapper">
              <component :is="msg.role === 'user' ? UserAvatar : AiAvatar" class="avatar-svg"/>
            </div>
            <div :class="msg.role" class="chat-bubble">
              <span v-if="msg.loading" class="loading-dot">●</span>
              <span v-else>{{ msg.content }}</span>
            </div>
          </div>
        </transition-group>
      </section>
      <form class="chat-gemini-inputbar" @submit.prevent="onSend">
        <el-input v-model="input" class="chat-gemini-input" clearable
                  placeholder="请输入您的问题..."/>
        <el-button :loading="loading" class="chat-gemini-send" type="primary" @click="onSend">发送
        </el-button>
      </form>
    </main>
  </div>
</template>

<script lang="ts" setup>
import {ref, nextTick, watch, onMounted, computed} from 'vue'
import {useChatStore} from '@/stores/chat'
import {useRouter} from 'vue-router'
import dayjs from 'dayjs'

const AiAvatar = {
  template: `<svg width="36" height="36" viewBox="0 0 36 36" fill="none"><defs><radialGradient id="aiG" cx="50%" cy="50%" r="50%"><stop offset="0%" stop-color="#7fd7ff"/><stop offset="100%" stop-color="#409EFF"/></radialGradient></defs><circle cx="18" cy="18" r="18" fill="url(#aiG)"/><rect x="11" y="14" width="14" height="8" rx="4" fill="#fff"/><circle cx="15" cy="18" r="1.5" fill="#409EFF"/><circle cx="21" cy="18" r="1.5" fill="#409EFF"/></svg>`
}
const UserAvatar = {
  template: `<svg width="36" height="36" viewBox="0 0 36 36" fill="none"><circle cx="18" cy="18" r="18" fill="#e0f3ff"/><path d="M18 11a5 5 0 1 1 0 10 5 5 0 0 1 0-10Zm0 12c4.418 0 8 2.015 8 4.5V29H10v-1.5C10 25.015 13.582 23 18 23Z" fill="#409EFF"/></svg>`
}

const chatStore = useChatStore()
const router = useRouter()
const input = ref('')
const loading = ref(false)
const messagesRef = ref<HTMLElement | null>(null)

// 修复：强制 messages 响应式，避免直接赋值丢失响应
const messages = computed(() => chatStore.currentMessages)

const onSend = async () => {
  if (!input.value.trim()) return
  loading.value = true
  await chatStore.sendMessage(input.value)
  input.value = ''
  loading.value = false
}

const onCreateSession = async () => {
  await chatStore.createSession()
  input.value = ''
}

const onSwitchSession = (sessionId: string) => {
  chatStore.switchSession(sessionId)
  input.value = ''
}

const formatTime = (ts: number) => dayjs(ts).format('MM-DD HH:mm')

const goHome = () => {
  router.push('/dashboard')
}

onMounted(() => {
  chatStore.loadSessions()
})

watch(messages, async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
})
</script>

<style scoped>
:global(html), :global(body) {
  height: 100vh;
  background: #f8fafc;
  margin: 0;
  padding: 0;
}

.chat-gemini-root {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: #f8fafc;
  min-width: 0;
  min-height: 0;
}

.chat-gemini-sidebar {
  width: 260px;
  background: #f4f6fa;
  border-right: 1px solid #e0e7ef;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: 0 0 0 0;
  min-width: 180px;
  max-width: 320px;
  height: 100vh;
}

.sidebar-header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  font-size: 22px;
  font-weight: 900;
  color: #409eff;
  letter-spacing: 2px;
  border-bottom: 1px solid #e0e7ef;
}

.sidebar-logo {
  font-size: 26px;
  font-weight: 900;
  color: #409eff;
  margin-right: 8px;
}

.sidebar-title {
  font-size: 18px;
  color: #222;
  font-weight: 700;
}

.sidebar-new-btn {
  margin: 18px 24px 8px 24px;
  border-radius: 22px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  border: none;
  color: #fff;
  height: 44px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  transition: box-shadow 0.18s, transform 0.18s;
}

.sidebar-new-btn:hover {
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.18);
  transform: translateY(-2px) scale(1.03);
}

.sidebar-session-list {
  flex: 1;
  min-height: 0;
  padding: 0 0 12px 0;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.session-title {
  font-weight: 600;
  font-size: 15px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 12px;
  color: #aaa;
}

.chat-gemini-main {
  flex: 1 1 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
  min-width: 0;
  min-height: 0;
  position: relative;
}

.chat-gemini-messages {
  flex: 1 1 0;
  overflow-y: auto;
  padding: 48px 0 120px 0;
  min-width: 0;
  min-height: 0;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.chat-message {
  display: flex;
  align-items: flex-end;
  margin-bottom: 32px;
  gap: 16px;
  padding: 0 48px;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.avatar-wrapper {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-svg {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
}

.chat-bubble {
  background: #f4f6fa;
  border-radius: 22px 22px 22px 8px;
  padding: 18px 28px;
  max-width: 60vw;
  box-shadow: none;
  font-size: 17px;
  word-break: break-word;
  position: relative;
  min-height: 36px;
  display: flex;
  align-items: center;
}

.chat-bubble.user {
  background: linear-gradient(90deg, #e0f3ff 0%, #b3e0ff 100%);
  color: #222;
  border-radius: 22px 22px 8px 22px;
}

.chat-bubble.assistant {
  background: #f4f6fa;
  color: #222;
  border-radius: 22px 22px 22px 8px;
}

.loading-dot {
  color: #409eff;
  animation: blink 1s infinite alternate;
  font-size: 22px;
  margin-right: 4px;
}

@keyframes blink {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0.3;
  }
}

.chat-gemini-inputbar {
  display: flex;
  align-items: center;
  background: #f8fafc;
  border-radius: 28px;
  box-shadow: 0 2px 16px rgba(64, 158, 255, 0.08);
  padding: 18px 24px;
  position: absolute;
  left: 50%;
  bottom: 32px;
  transform: translateX(-50%);
  width: 60vw;
  min-width: 320px;
  max-width: 900px;
  z-index: 2;
}

.chat-gemini-input {
  flex: 1;
  border-radius: 18px;
  font-size: 17px;
  margin-right: 18px;
  background: #fff;
  box-shadow: 0 1px 8px rgba(64, 158, 255, 0.08);
}

.chat-gemini-send {
  border-radius: 18px;
  font-weight: 700;
  padding: 0 32px;
  height: 48px;
  font-size: 18px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  border: none;
  color: #fff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  transition: box-shadow 0.18s, transform 0.18s;
}

.chat-gemini-send:hover {
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.18);
  transform: translateY(-2px) scale(1.03);
}

@media (max-width: 900px) {
  .chat-gemini-sidebar {
    width: 60px;
    min-width: 60px;
    max-width: 60px;
    padding: 0;
  }

  .sidebar-header {
    padding: 0 8px;
    font-size: 18px;
  }

  .sidebar-title {
    display: none;
  }

  .sidebar-new-btn {
    margin: 12px 8px 8px 8px;
    font-size: 14px;
    height: 36px;
    padding: 0 8px;
  }

  .sidebar-session-list {
    padding: 0 0 4px 0;
  }

  .chat-gemini-main {
    width: calc(100vw - 60px);
  }

  .chat-gemini-messages {
    padding: 24px 0 80px 0;
  }

  .chat-message {
    padding: 0 12px;
    gap: 8px;
  }

  .chat-gemini-inputbar {
    width: 90vw;
    min-width: 0;
    max-width: 99vw;
    padding: 10px 6px;
    border-radius: 16px;
    bottom: 12px;
  }

  .chat-gemini-input {
    border-radius: 10px;
    font-size: 15px;
    margin-right: 8px;
  }

  .chat-gemini-send {
    border-radius: 10px;
    font-size: 15px;
    height: 36px;
    padding: 0 12px;
  }
}

@media (max-width: 600px) {
  .chat-gemini-sidebar {
    width: 0;
    min-width: 0;
    max-width: 0;
    display: none;
  }

  .chat-gemini-main {
    width: 100vw;
  }

  .chat-gemini-inputbar {
    width: 98vw;
    left: 1vw;
    transform: none;
    border-radius: 10px;
    padding: 6px 2px;
  }
}
</style>
