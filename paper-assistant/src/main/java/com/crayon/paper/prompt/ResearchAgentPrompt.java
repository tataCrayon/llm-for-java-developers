package com.crayon.paper.prompt;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * emmm，不是一个很好的方式。
 * TODO提示词管理
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Component
public class ResearchAgentPrompt {


    private static final String REACT_PROMPT = """
            # 学术研究助理指令
            你是一位AI学术研究专家，专门帮助用户查找领域内高质量论文和第一手资料。
            当前日期: {{current_date}}
            用户目标: {{user_query}}
            
            ## 你的能力
            1. 使用 search_paper 工具查找学术论文
            2. 使用 high_quality_internet_search 获取背景知识
            3. 分析论文的创新点和研究价值
            
            ## 工作流程
            {{#if history}}
            {{history}}
            {{/if}}
            
            ## 下一步操作
            请严格按格式回复：
            Thought: 分析当前研究状态和下一步需求
            {{#if should_use_tool}}
            Action: 选择工具 (search_paper 或 high_quality_internet_search)
            Action Input: 优化的搜索关键词
            {{else}}
            Final Answer: 按以下格式组织最终答案：
            ### 研究主题
            {{user_query}}
            
            ### 核心发现
            - 领域趋势
            - 关键突破
            
            ### 必读论文 (按重要性排序)
            1. [标题](链接)
               - **作者**: 主要作者
               - **年份**: 出版年份
               - **创新点**: 核心贡献
               - **研究价值**: 为什么值得阅读
            
            ...(3-5篇论文)...
            
            ### 补充资源
            - 相关数据集
            - 开源实现
            - 领域权威实验室
            
            ## 搜索策略说明
            - 使用关键词：{{keywords}}
            - 搜索次数：{{search_count}}
            {{/if}}
            """;

    public Prompt generateSimplePrompt(String userQuery) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("current_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        variables.put("user_query", userQuery);
        return PromptTemplate.from(REACT_PROMPT).apply(variables);
    }

    public Prompt generatePrompt(String userQuery, String history, boolean shouldUseTool) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("current_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        variables.put("user_query", userQuery);
        variables.put("history", history);
        variables.put("should_use_tool", shouldUseTool);
        return PromptTemplate.from(REACT_PROMPT).apply(variables);
    }

    public String formatHistory(List<ChatMessage> messages) {
        StringBuilder history = new StringBuilder();
        for (ChatMessage msg : messages) {
            if (msg instanceof UserMessage) {
                history.append("User: ").append(((UserMessage) msg).singleText()).append("\n");
            } else if (msg instanceof AiMessage) {
                history.append("AI: ").append(((AiMessage) msg).text()).append("\n");
            } else if (msg instanceof ToolExecutionResultMessage) {
                history.append("Tool: ").append(abbreviate(((ToolExecutionResultMessage) msg).text(), 300)).append("\n");
            }
        }
        return history.toString();
    }

    private String abbreviate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}