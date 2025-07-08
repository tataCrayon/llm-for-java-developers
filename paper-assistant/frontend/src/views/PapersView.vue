<template>
  <div class="papers-bg">
    <div class="papers-brand-bar"></div>
    <div class="papers-view-container">
      <el-tabs v-model="activeTab" class="papers-tabs" @tab-click="onTabClick">
        <el-tab-pane label="论文检索" name="paper-search"/>
        <el-tab-pane label="论文分析" name="paper-analyze"/>
        <el-tab-pane label="论文对比" name="paper-compare"/>
      </el-tabs>
      <router-view/>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.name as string)

watch(() => route.name, (name) => {
  activeTab.value = name as string
})

const onTabClick = (tab: any) => {
  router.push({name: tab.paneName})
}
</script>

<style scoped>
.papers-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f3ff 0%, #f8fafc 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.papers-brand-bar {
  width: 100vw;
  height: 6px;
  background: linear-gradient(90deg, #409eff 0%, #7fd7ff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.10);
  margin-bottom: 0;
}

.papers-view-container {
  max-width: 900px;
  margin: 32px auto;
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(64, 158, 255, 0.08);
  border: 1.5px solid #e0f3ff;
}

.papers-tabs {
  --el-tabs-header-bg-color: transparent;
  --el-tabs-active-color: #409eff;
  --el-tabs-header-border-bottom: 2px solid #e0f3ff;
  margin-bottom: 18px;
}

.papers-tabs .el-tabs__item.is-active {
  color: #409eff !important;
  font-weight: 700;
  background: linear-gradient(90deg, #e0f3ff 0%, #fff 100%);
  border-radius: 8px 8px 0 0;
}

@media (max-width: 900px) {
  .papers-view-container {
    max-width: 99vw;
    padding: 8px;
    border-radius: 8px;
  }
}
</style>
