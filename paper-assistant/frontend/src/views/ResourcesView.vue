<template>
  <div class="resources-bg">
    <div class="resources-brand-bar"></div>
    <div class="resources-container">
      <el-card class="resources-card">
        <el-form :inline="true" @submit.prevent="onGetResources">
          <el-form-item label="学习水平">
            <el-select v-model="level" placeholder="请选择">
              <el-option label="初级" value="beginner"/>
              <el-option label="中级" value="intermediate"/>
              <el-option label="高级" value="advanced"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :loading="loading" class="resources-btn" type="primary"
                       @click="onGetResources">获取资源
            </el-button>
          </el-form-item>
        </el-form>
        <el-divider/>
        <div v-if="result">
          <el-alert show-icon title="推荐学习资源" type="success"/>
          <div class="result-content" v-html="result"></div>
        </div>
        <div v-else-if="searched">
          <el-empty description="暂无推荐"/>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import {papersApi} from '@/api/papers'

const level = ref('beginner')
const result = ref('')
const loading = ref(false)
const searched = ref(false)

const onGetResources = async () => {
  loading.value = true
  try {
    const res = await papersApi.getLearningResources(level.value)
    result.value = res.data
    searched.value = true
  } catch (e) {
    result.value = ''
    searched.value = true
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.resources-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f3ff 0%, #f8fafc 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.resources-brand-bar {
  width: 100vw;
  height: 6px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  margin-bottom: 0;
}

.resources-container {
  max-width: 700px;
  margin: 32px auto;
  padding: 0;
}

.resources-card {
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(64, 158, 255, 0.08);
  border: 1.5px solid #e0f3ff;
  padding: 24px;
  background: #fff;
}

.resources-btn {
  border-radius: 10px;
  font-weight: 600;
  padding: 0 22px;
  height: 40px;
  font-size: 16px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  border: none;
  color: #fff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  transition: box-shadow 0.18s, transform 0.18s;
}

.resources-btn:hover {
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.18);
  transform: translateY(-2px) scale(1.03);
}

.result-content {
  margin-top: 16px;
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  min-height: 80px;
  font-size: 15px;
  color: #333;
}

@media (max-width: 700px) {
  .resources-container {
    max-width: 99vw;
    padding: 0 2px;
  }

  .resources-card {
    border-radius: 8px;
    padding: 8px;
  }
}
</style>
