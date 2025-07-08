import api from './index'
import type {
  PaperAnalysisRequest,
  PaperComparisonRequest,
  LearningPlanRequest,
  ApiResponse
} from '@/types'

export const papersApi = {
  // 搜索论文
  searchPapers(query: string): Promise<ApiResponse<string>> {
    return api.get('/papers/search', {params: {query}})
  },

  // 分析论文
  analyzePaper(data: PaperAnalysisRequest): Promise<ApiResponse<string>> {
    return api.post('/papers/analyze', data)
  },

  // 比较论文
  comparePapers(data: PaperComparisonRequest): Promise<ApiResponse<string>> {
    return api.post('/papers/compare', data)
  },

  // 生成学习计划
  generateLearningPlan(data: LearningPlanRequest): Promise<ApiResponse<string>> {
    return api.post('/papers/plan/Learning/get', data)
  },

  // 获取热门话题
  getHotTopics(): Promise<ApiResponse<string>> {
    return api.get('/papers/hot/topics/get')
  },

  // 获取学习资源
  getLearningResources(level: string = 'beginner'): Promise<ApiResponse<string>> {
    return api.get('/papers/resources/get', {params: {level}})
  },

  // 健康检查
  healthCheck(): Promise<ApiResponse<string>> {
    return api.get('/papers/health')
  }
}
