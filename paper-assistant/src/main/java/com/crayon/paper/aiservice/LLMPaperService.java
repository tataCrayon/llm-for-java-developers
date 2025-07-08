package com.crayon.paper.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * LLM论文研究智能助手接口
 * 专门用于帮助开发者检索、分析和学习LLM相关论文
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
public interface LLMPaperService {

    /**
     * 搜索并分析LLM相关论文
     * 根据用户查询搜索相关论文，并提供学习导向的分析
     *
     * @param query 用户的查询请求
     * @return 包含论文搜索结果和学习建议的详细回复
     */
    //@SystemMessage(fromResource = "prompt/search_and_analyze_papers.txt")
    @SystemMessage(fromResource = "prompt/search_and_analyze_papers_without_tools.txt")
    String searchAndAnalyzePapers(@UserMessage String query);

    /**
     * 深度分析特定论文
     * 对单篇论文进行详细分析，包括技术细节、创新点、实现建议等
     *
     * @param paperInfo 论文信息（标题、摘要、链接等）
     * @return 深度分析报告
     */
    @SystemMessage(fromResource = "prompt/analyze_paper_in_depth.txt")
    String analyzePaperInDepth(@UserMessage String paperInfo);

    /**
     * 比较多篇论文
     * 对多篇相关论文进行对比分析，帮助理解技术演进和选择最佳方案
     *
     * @param papers 多篇论文信息
     * @return 对比分析结果
     */
    @SystemMessage(fromResource = "prompt/compare_papers.txt")
    String comparePapers(@UserMessage String papers);

    /**
     * 生成学习计划
     * 根据用户的背景和目标，生成个性化的LLM论文学习计划
     *
     * @param userBackground 用户背景信息
     * @param learningGoals  学习目标
     * @return 详细的学习计划
     */
    @SystemMessage(fromResource = "prompt/generate_learning_plan.txt")
    String generateLearningPlan(@UserMessage String userBackground, @UserMessage String learningGoals);
}