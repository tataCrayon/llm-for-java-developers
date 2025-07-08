package com.crayon.paper.agent;

import com.crayon.paper.aiservice.LLMPaperService;
import com.crayon.paper.tools.web.PaperSearchTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


/**
 * LLM论文研究Agent服务实现类
 * 负责创建和管理Agent实例，提供论文搜索和分析服务
 */
@Slf4j
@Service
public class LLMPaperAgentService {


    private final ChatModel chatLanguageModel;
    private final PaperSearchTool paperSearchTool;

    private LLMPaperService paperAgent;

    public LLMPaperAgentService(@Qualifier("deepseek-chat") ChatModel chatLanguageModel, PaperSearchTool webSearchTool) {
        this.chatLanguageModel = chatLanguageModel;
        this.paperSearchTool = webSearchTool;
    }

    /**
     * 初始化Agent实例
     * 使用LangChain4j的AiServices构建具有工具调用能力的Agent
     */
    @PostConstruct
    public void initializeAgent() {
        log.info("正在初始化LLM论文研究Agent...");
        try {
            this.paperAgent = AiServices.builder(LLMPaperService.class)
                    .chatModel(chatLanguageModel)
                    .tools(paperSearchTool)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .build();
            log.info("LLM论文研究Agent初始化成功");
        } catch (Exception e) {
            log.error("Agent初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法初始化LLM论文研究Agent", e);
        }
    }

    /**
     * 搜索并分析LLM相关论文
     * 这是主要的对外服务接口
     *
     * @param query 用户查询，如"transformer注意力机制"、"GPT架构优化"等
     * @return 包含搜索结果和分析的详细回复
     */
    public String searchAndAnalyzePapers(String query) {
        log.info("接收到论文搜索请求，请求内容: {}", query);
        try {
            // 验证输入
            if (query == null || query.trim().isEmpty()) {
                log.warn("输入为null或者空字符");
                return "请提供有效的搜索查询词，例如：'Transformer架构'、'注意力机制'、'GPT模型优化'等。";
            }

            // 调用Agent进行搜索和分析
            String result = paperAgent.searchAndAnalyzePapers(query);

            log.info("论文搜索和分析完成，查询词: {}", query);
            return result;

        } catch (Exception e) {
            log.error("搜索和分析论文时发生错误: {}", e.getMessage(), e);
            return String.format("处理您的请求时发生错误: %s。请稍后重试或联系技术支持。", e.getMessage());
        }
    }

    /**
     * 深度分析特定论文
     *
     * @param paperInfo 论文信息，包括标题、摘要、作者等
     * @return 深度分析报告
     */
    public String analyzePaperInDepth(String paperInfo) {
        log.info("开始深度分析论文");

        try {
            if (paperInfo == null || paperInfo.trim().isEmpty()) {
                return "请提供论文的详细信息，包括标题、摘要、作者等，以便进行深度分析。";
            }

            String result = paperAgent.analyzePaperInDepth(paperInfo);

            log.info("论文深度分析完成");
            return result;

        } catch (Exception e) {
            log.error("深度分析论文时发生错误: {}", e.getMessage(), e);
            return String.format("分析过程中发生错误: %s", e.getMessage());
        }
    }

    /**
     * 比较多篇论文
     *
     * @param papers 多篇论文的信息
     * @return 对比分析结果
     */
    public String comparePapers(String papers) {
        log.info("开始比较多篇论文");

        try {
            if (papers == null || papers.trim().isEmpty()) {
                return "请提供要比较的论文信息，每篇论文应包含标题、主要方法和贡献等。";
            }

            String result = paperAgent.comparePapers(papers);

            log.info("论文对比分析完成");
            return result;

        } catch (Exception e) {
            log.error("比较论文时发生错误: {}", e.getMessage(), e);
            return String.format("对比分析过程中发生错误: %s", e.getMessage());
        }
    }

    /**
     * 生成个性化学习计划
     *
     * @param userBackground 用户背景，如"机器学习初学者"、"有深度学习基础的工程师"等
     * @param learningGoals  学习目标，如"理解Transformer架构"、"掌握LLM微调技术"等
     * @return 详细的学习计划
     */
    public String generatePersonalizedLearningPlan(String userBackground, String learningGoals) {
        log.info("生成个性化学习计划，背景: {}, 目标: {}", userBackground, learningGoals);

        try {
            if (userBackground == null || userBackground.trim().isEmpty()) {
                userBackground = "有一定编程基础的开发者";
            }

            if (learningGoals == null || learningGoals.trim().isEmpty()) {
                learningGoals = "系统学习大语言模型相关技术";
            }

            String result = paperAgent.generateLearningPlan(userBackground, learningGoals);

            log.info("个性化学习计划生成完成");
            return result;

        } catch (Exception e) {
            log.error("生成学习计划时发生错误: {}", e.getMessage(), e);
            return String.format("生成学习计划过程中发生错误: %s", e.getMessage());
        }
    }

    /**
     * 获取热门LLM研究方向
     * 提供当前热门的研究方向和推荐论文
     *
     * @return 热门研究方向介绍
     */
    public String getHotResearchTopics() {
        log.info("获取热门LLM研究方向");

        String hotTopics = """
                🔥 当前LLM领域热门研究方向：
                
                1. 📊 **多模态大模型 (Multimodal LLMs)**
                   - 图文理解和生成
                   - 视频分析和生成
                   - 音频处理集成
                   推荐搜索: "multimodal large language models"
                
                2. 🧠 **模型架构优化 (Architecture Optimization)**
                   - 高效注意力机制
                   - 稀疏激活和MoE
                   - 长序列处理
                   推荐搜索: "efficient transformer architecture"
                
                3. 🎯 **模型对齐和安全 (Alignment & Safety)**
                   - RLHF技术改进
                   - 有害内容检测
                   - 可控文本生成
                   推荐搜索: "RLHF alignment safety"
                
                4. ⚡ **推理加速 (Inference Acceleration)**
                   - 模型量化技术
                   - 推测解码
                   - 并行推理策略
                   推荐搜索: "LLM inference optimization"
                
                5. 🔧 **微调和适应 (Fine-tuning & Adaptation)**
                   - 参数高效微调
                   - 少样本学习
                   - 领域适应技术
                   推荐搜索: "parameter efficient fine tuning"
                
                💡 使用建议：直接输入推荐的搜索词，我会为您找到最新的相关论文！
                """;

        return hotTopics;
    }

    /**
     * 提供学习资源推荐
     *
     * @param level 学习水平：beginner, intermediate, advanced
     * @return 推荐的学习资源
     */
    public String recommendLearningResources(String level) {
        log.info("推荐学习资源，水平: {}", level);

        if (level == null) {
            level = "beginner";
        }

        switch (level.toLowerCase()) {
            case "beginner":
                return generateBeginnerResources();
            case "intermediate":
                return generateIntermediateResources();
            case "advanced":
                return generateAdvancedResources();
            default:
                return generateBeginnerResources();
        }
    }

    /**
     * 生成初学者资源推荐
     */
    private String generateBeginnerResources() {
        return """
                🌟 **LLM初学者学习资源推荐**
                
                📚 **基础理论论文**：
                1. "Attention Is All You Need" - Transformer基础
                2. "BERT: Pre-training of Deep Bidirectional Transformers" - 预训练基础
                3. "Language Models are Few-Shot Learners" - GPT-3原理
                
                🔍 **推荐搜索关键词**：
                - "transformer tutorial"
                - "BERT architecture explained"  
                - "GPT model basics"
                
                📖 **学习路径**：
                1. 先理解注意力机制和Transformer架构
                2. 学习预训练和微调概念
                3. 了解不同类型的语言模型
                4. 实践简单的文本分类任务
                
                💡 **实践建议**：从Hugging Face的教程开始，动手实现简单的文本处理任务。
                """;
    }

    /**
     * 生成进阶者资源推荐
     */
    private String generateIntermediateResources() {
        return """
                🚀 **LLM进阶者学习资源推荐**
                
                📚 **进阶技术论文**：
                1. "LoRA: Low-Rank Adaptation of Large Language Models" - 高效微调
                2. "InstructGPT: Training language models to follow instructions" - 指令调优
                3. "Chain-of-Thought Prompting Elicits Reasoning" - 推理能力
                
                🔍 **推荐搜索关键词**：
                - "parameter efficient fine tuning"
                - "instruction following LLM"
                - "chain of thought reasoning"
                
                📈 **技能提升方向**：
                1. 掌握高效微调技术（LoRA, AdaLoRA等）
                2. 理解指令遵循和对齐技术
                3. 学习提示工程和上下文学习
                4. 探索多任务学习和迁移学习
                
                🛠️ **项目实践**：构建特定领域的问答系统，实现自定义的文本生成应用。
                """;
    }

    /**
     * 生成高级者资源推荐
     */
    private String generateAdvancedResources() {
        return """
                🎓 **LLM高级研究者资源推荐**
                
                📚 **前沿研究论文**：
                1. "PaLM: Scaling Language Modeling with Pathways" - 大规模训练
                2. "Constitutional AI: Harmlessness from AI Feedback" - AI安全对齐
                3. "Toolformer: Language Models Can Teach Themselves to Use Tools" - 工具使用
                
                🔍 **前沿研究方向**：
                - "emergent abilities large language models"
                - "AI alignment constitutional AI"
                - "tool learning language models"
                
                🔬 **研究重点**：
                1. 涌现能力和规模效应研究
                2. 多模态融合和理解
                3. 强化学习和AI对齐
                4. 工具学习和代理能力
                
                🌟 **创新机会**：关注模型可解释性、效率优化、安全对齐等前沿问题。
                """;
    }
}