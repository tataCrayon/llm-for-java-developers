<template>
  <div class="dashboard-bg">
    <div class="dashboard-brand-bar"></div>
    <div class="dashboard-center">
      <div class="dashboard-card">
        <div class="dashboard-header">
          <h2 class="welcome">
            欢迎使用 <span class="brand">LLM 论文助手</span>
          </h2>
          <el-button class="logout-btn" type="danger" @click="onLogout">退出登录</el-button>
        </div>
        <div class="quick-row">
          <div v-for="item in quicks" :key="item.label" class="quick-card"
               @click="$router.push(item.path)">
            <div class="icon-side">
              <el-icon :size="38">
                <component :is="item.icon"/>
              </el-icon>
            </div>
            <div class="text-side">
              <div class="quick-label">{{ item.label }}</div>
              <div class="quick-desc">{{ item.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {Search, Document, DataAnalysis, Guide, ChatDotRound, Reading} from '@element-plus/icons-vue'
import {useAuthStore} from '@/stores/auth'
import {useRouter} from 'vue-router'

const quicks = [
  {label: '论文检索', icon: Search, path: '/papers/search', desc: '智能检索最新/经典论文'},
  {label: '论文分析', icon: Document, path: '/papers/analyze', desc: 'AI深度解读论文内容'},
  {label: '论文对比', icon: DataAnalysis, path: '/papers/compare', desc: '多篇论文智能对比'},
  {label: '学习计划', icon: Guide, path: '/learning', desc: '定制个性化学习路径'},
  {label: 'AI对话', icon: ChatDotRound, path: '/chat', desc: '与AI助手实时交流'},
  {label: '学习资源', icon: Reading, path: '/resources', desc: '获取优质学习资料'}
]

const authStore = useAuthStore()
const router = useRouter()

const onLogout = () => {
  authStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
:global(html), :global(body) {
  overflow-x: hidden;
  width: 100vw;
  margin: 0;
  padding: 0;
  background: none;
}

.dashboard-bg {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(120deg, #e0e7ff 0%, #f5f6fa 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.dashboard-brand-bar {
  width: 100vw;
  height: 6px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  margin-bottom: 0;
}

.dashboard-center {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
}

.dashboard-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  box-shadow: 0 8px 48px rgba(80, 80, 120, 0.13);
  padding: 48px 4vw 32px 4vw;
  max-width: 1000px;
  width: 100%;
  margin: 48px auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 12px;
}

.logout-btn {
  border-radius: 8px;
  font-weight: 600;
  height: 36px;
  padding: 0 18px;
}

.welcome {
  text-align: center;
  margin-bottom: 40px;
  color: #222;
  font-size: 32px;
  font-weight: 800;
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

.quick-row {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  justify-content: center;
  width: 100%;
}

.quick-card {
  display: flex;
  align-items: center;
  width: 300px;
  height: 90px;
  background: linear-gradient(90deg, #f8fafc 60%, #fff 100%);
  border-radius: 14px;
  box-shadow: 0 2px 16px rgba(80, 80, 120, 0.10);
  cursor: pointer;
  transition: box-shadow 0.22s, transform 0.22s;
  margin-bottom: 10px;
  padding: 0 24px;
  position: relative;
  overflow: hidden;
}

.quick-card:hover {
  box-shadow: 0 8px 32px rgba(64, 158, 255, 0.18);
  transform: translateY(-2px) scale(1.02);
  color: #409eff;
}

.icon-side {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0e7ff 60%, #fff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);
  margin-right: 18px;
  font-size: 32px;
}

.text-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.quick-label {
  font-size: 20px;
  font-weight: 700;
  color: #222;
  margin-bottom: 2px;
}

.quick-desc {
  font-size: 14px;
  color: #888;
  font-weight: 400;
}

@media (max-width: 1100px) {
  .dashboard-card {
    padding: 32px 2vw 16px 2vw;
  }

  .quick-row {
    gap: 16px;
  }

  .quick-card {
    width: 48%;
    min-width: 180px;
    max-width: 100%;
    padding: 0 8px;
  }
}

@media (max-width: 700px) {
  .dashboard-card {
    padding: 10px 1vw 6px 1vw;
    border-radius: 10px;
  }

  .welcome {
    font-size: 18px;
    margin-bottom: 10px;
    text-align: center;
  }

  .quick-row {
    flex-direction: column;
    gap: 6px;
  }

  .quick-card {
    width: 100%;
    min-width: 0;
    max-width: 100vw;
    height: 64px;
    margin-bottom: 6px;
    padding: 0 3px;
  }

  .icon-side {
    width: 28px;
    height: 28px;
    margin-right: 8px;
    font-size: 16px;
  }

  .quick-label {
    font-size: 13px;
  }

  .quick-desc {
    font-size: 10px;
  }
}
</style>
