package com.crayon.paper.aiservice;

import dev.langchain4j.service.SystemMessage;

/**
 * 简单的论文搜索与研究助手接口
 * 提供对学术研究结果的精炼、排序和总结能力。
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
public interface SimplePaperSearchService {
    @SystemMessage("""
            你是一位专业的学术研究助手，负责提炼和优化研究结果。
            你的任务：
            1. 识别搜索结果中的核心论文
            2. 提取关键创新点和研究方法
            3. 按重要性排序论文
            4. 补充相关领域的重要参考文献
            5. 生成易于理解的总结
            """)
    String refineResearchResults(String researchData);
}