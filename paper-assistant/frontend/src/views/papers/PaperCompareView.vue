<template>
  <div class="paper-compare-container">
    <el-card>
      <el-form label-width="90px" @submit.prevent="onCompare">
        <div v-for="(paper, idx) in papers" :key="idx" class="paper-item">
          <el-divider>论文{{ idx + 1 }}</el-divider>
          <el-form-item label="标题" required>
            <el-input v-model="paper.title" placeholder="请输入论文标题"/>
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="paper.authors" placeholder="作者名（逗号分隔）"/>
          </el-form-item>
          <el-form-item label="摘要">
            <el-input v-model="paper.abstractText" placeholder="论文摘要" rows="2" type="textarea"/>
          </el-form-item>
          <el-form-item label="链接">
            <el-input v-model="paper.url" placeholder="论文链接"/>
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="paper.category" placeholder="领域/分类"/>
          </el-form-item>
        </div>
        <el-form-item label="对比维度">
          <el-select v-model="comparisonAspects" multiple placeholder="选择或输入对比维度">
            <el-option label="方法创新性" value="方法创新性"/>
            <el-option label="实验效果" value="实验效果"/>
            <el-option label="计算复杂度" value="计算复杂度"/>
            <el-option label="应用前景" value="应用前景"/>
          </el-select>
        </el-form-item>
        <el-form-item label="对比目的">
          <el-input v-model="comparisonPurpose" placeholder="请输入对比目的"/>
        </el-form-item>
        <el-form-item>
          <el-button :loading="loading" type="primary" @click="onCompare">对比分析</el-button>
          <el-button style="margin-left:8px" @click="addPaper">添加论文</el-button>
          <el-button :disabled="papers.length <= 2" style="margin-left:8px" @click="removePaper">
            移除论文
          </el-button>
        </el-form-item>
      </el-form>
      <el-divider/>
      <div v-if="result">
        <el-alert show-icon title="对比结果" type="success"/>
        <div class="result-content" v-html="result"></div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import {papersApi} from '@/api/papers'

const papers = ref([
  {title: '', authors: '', abstractText: '', url: '', category: ''},
  {title: '', authors: '', abstractText: '', url: '', category: ''}
])
const comparisonAspects = ref<string[]>([])
const comparisonPurpose = ref('')
const result = ref('')
const loading = ref(false)

const addPaper = () => {
  papers.value.push({title: '', authors: '', abstractText: '', url: '', category: ''})
}
const removePaper = () => {
  if (papers.value.length > 2) papers.value.pop()
}

const onCompare = async () => {
  if (papers.value.some(p => !p.title.trim())) return
  loading.value = true
  try {
    const req = {
      papers: papers.value.map(p => ({
        ...p,
        authors: p.authors.split(',').map((a: string) => a.trim()).filter(Boolean)
      })),
      comparisonAspects: comparisonAspects.value,
      comparisonPurpose: comparisonPurpose.value
    }
    const res = await papersApi.comparePapers(req)
    result.value = res.data
  } catch (e) {
    result.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.paper-compare-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 24px;
}

.paper-item {
  margin-bottom: 12px;
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
