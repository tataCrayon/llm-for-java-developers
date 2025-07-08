<template>
  <div class="paper-search-container">
    <el-card>
      <el-form :inline="true" @submit.prevent="onSearch">
        <el-form-item label="关键词">
          <el-input v-model="query" placeholder="请输入论文关键词" style="width: 300px"/>
        </el-form-item>
        <el-form-item>
          <el-button :loading="loading" type="primary" @click="onSearch">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-divider/>
      <div v-if="result">
        <el-alert show-icon title="搜索结果" type="success"/>
        <div class="result-content" v-html="result"></div>
      </div>
      <div v-else-if="searched">
        <el-empty description="暂无结果"/>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import {papersApi} from '@/api/papers'

const query = ref('')
const result = ref('')
const loading = ref(false)
const searched = ref(false)

const onSearch = async () => {
  if (!query.value.trim()) return
  loading.value = true
  try {
    const res = await papersApi.searchPapers(query.value)
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
.paper-search-container {
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
