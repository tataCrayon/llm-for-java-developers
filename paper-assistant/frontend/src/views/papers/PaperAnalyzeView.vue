<template>
  <div class="paper-analyze-container">
    <el-card>
      <el-form :model="form" label-width="90px" @submit.prevent="onAnalyze">
        <el-form-item label="标题" prop="title" required>
          <el-input v-model="form.title" placeholder="请输入论文标题"/>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.authors" placeholder="作者名（逗号分隔）"/>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.abstractText" placeholder="论文摘要" rows="3" type="textarea"/>
        </el-form-item>
        <el-form-item label="链接">
          <el-input v-model="form.url" placeholder="论文链接"/>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="领域/分类"/>
        </el-form-item>
        <el-form-item label="分析重点">
          <el-select v-model="form.analysisAspects" multiple placeholder="选择或输入分析重点">
            <el-option label="技术创新点" value="技术创新点"/>
            <el-option label="实验方法" value="实验方法"/>
            <el-option label="应用场景" value="应用场景"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :loading="loading" type="primary" @click="onAnalyze">分析</el-button>
        </el-form-item>
      </el-form>
      <el-divider/>
      <div v-if="result">
        <el-alert show-icon title="分析结果" type="success"/>
        <div class="result-content" v-html="result"></div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import {papersApi} from '@/api/papers'

const form = ref({
  title: '',
  authors: '',
  abstractText: '',
  url: '',
  category: '',
  analysisAspects: [] as string[]
})
const result = ref('')
const loading = ref(false)

const onAnalyze = async () => {
  if (!form.value.title.trim()) return
  loading.value = true
  try {
    const req = {
      ...form.value,
      authors: form.value.authors.split(',').map((a: string) => a.trim()).filter(Boolean)
    }
    const res = await papersApi.analyzePaper(req)
    result.value = res.data
  } catch (e) {
    result.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.paper-analyze-container {
  max-width: 700px;
  margin: 40px auto;
  padding: 24px;
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
</style>
