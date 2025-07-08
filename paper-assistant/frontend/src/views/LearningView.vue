<template>
  <div class="learning-bg">
    <div class="learning-brand-bar"></div>
    <div class="learning-plan-container">
      <el-card class="learning-card">
        <el-form :model="form" label-width="110px" @submit.prevent="onGenerate">
          <el-form-item label="用户背景" required>
            <el-input v-model="form.userBackground" class="learning-input"
                      placeholder="如：有Python基础的机器学习初学者"/>
          </el-form-item>
          <el-form-item label="学习目标" required>
            <el-input v-model="form.learningGoals" class="learning-input"
                      placeholder="如：掌握Transformer架构"/>
          </el-form-item>
          <el-form-item label="每周学习时间">
            <el-input-number v-model="form.weeklyHours" :max="80" :min="1"/>
            小时
          </el-form-item>
          <el-form-item label="期望完成时间">
            <el-input-number v-model="form.targetWeeks" :max="52" :min="1"/>
            周
          </el-form-item>
          <el-form-item label="兴趣方向">
            <el-select v-model="form.interestedTopics" multiple placeholder="选择或输入兴趣方向">
              <el-option label="大模型原理" value="大模型原理"/>
              <el-option label="模型训练" value="模型训练"/>
              <el-option label="推理优化" value="推理优化"/>
              <el-option label="应用开发" value="应用开发"/>
            </el-select>
          </el-form-item>
          <el-form-item label="学习偏好">
            <el-select v-model="form.learningPreferences" multiple placeholder="选择或输入学习偏好">
              <el-option label="理论深入" value="理论深入"/>
              <el-option label="实践导向" value="实践导向"/>
              <el-option label="论文阅读" value="论文阅读"/>
              <el-option label="代码实现" value="代码实现"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :loading="loading" class="learning-btn" type="primary" @click="onGenerate">
              生成学习计划
            </el-button>
          </el-form-item>
        </el-form>
        <el-divider/>
        <div v-if="result">
          <el-alert show-icon title="个性化学习计划" type="success"/>
          <div class="result-content" v-html="result"></div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import {papersApi} from '@/api/papers'

const form = ref({
  userBackground: '',
  learningGoals: '',
  weeklyHours: 5,
  targetWeeks: 8,
  interestedTopics: [] as string[],
  learningPreferences: [] as string[]
})
const result = ref('')
const loading = ref(false)

const onGenerate = async () => {
  if (!form.value.userBackground.trim() || !form.value.learningGoals.trim()) return
  loading.value = true
  try {
    const res = await papersApi.generateLearningPlan(form.value)
    result.value = res.data
  } catch (e) {
    result.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.learning-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f3ff 0%, #f8fafc 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.learning-brand-bar {
  width: 100vw;
  height: 6px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  margin-bottom: 0;
}

.learning-plan-container {
  max-width: 700px;
  margin: 32px auto;
  padding: 0;
}

.learning-card {
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(64, 158, 255, 0.08);
  border: 1.5px solid #e0f3ff;
  padding: 24px;
  background: #fff;
}

.learning-input {
  border-radius: 10px;
  box-shadow: 0 1px 8px rgba(64, 158, 255, 0.08);
  font-size: 16px;
}

.learning-btn {
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

.learning-btn:hover {
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
  .learning-plan-container {
    max-width: 99vw;
    padding: 0 2px;
  }

  .learning-card {
    border-radius: 8px;
    padding: 8px;
  }
}
</style>
