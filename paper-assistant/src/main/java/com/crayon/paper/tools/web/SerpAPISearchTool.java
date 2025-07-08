package com.crayon.paper.tools.web;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.web.search.WebSearchTool;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SerpAPI搜索工具<br>
 * 用于搜索论文等高质量搜索场景。<br>
 *
 *
 * <p>
 * 封装参考： <br>
 * https://docs.langchain4j.dev/integrations/web-search-engines/searchapi <br>
 * https://docs.langchain4j.dev/tutorials/tools
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/26
 */
@Slf4j
@Component
public class SerpAPISearchTool {

    /**
     * 创建serpapi工具后，LangChain给的示例是要封装进一个WebSearchTool
     * PaperSearchTool.from(searchEngine)
     * 没有深究
     */
    private final WebSearchTool defaultWebSearchTool;

    private final WebSearchTool paperSearchTool;

    /**
     * 通用参数
     */
    private final Map<String, Object> defaultOptionalParameters = Map.of(
            "gl", "us",
            "hl", "en",
            "google_domain", "google.com",
            "num", 5  // 增加结果数量控制
    );

    /**
     * 学术搜索专用参数
     */
    private final Map<String, Object> academicOptionalParameters = Map.of(
            "gl", "us",
            "hl", "en",
            "google_domain", "google.com",
            "num", 10,            // 获取更多结果用于筛选
            "tbm", "src",       // 学术搜索模式
            "as_ylo", Year.now().getValue() - 3,  // 近三年
            "as_vis", "0",      // 包含预印本论文
            "as_dt", "i"      // 仅返回计算机科学领域
    );

    public SerpAPISearchTool() {
        this.defaultWebSearchTool = buildDefaultWebSearchTool();
        this.paperSearchTool = buildWebSearchTool(academicOptionalParameters);
    }

    private WebSearchTool buildDefaultWebSearchTool() {
        return buildWebSearchTool(null);
    }

    private WebSearchTool buildWebSearchTool(Map<String, Object> optionalParameters) {
        if (optionalParameters == null) {
            optionalParameters = this.defaultOptionalParameters;
        }
        String serpapiApiKey = Optional.ofNullable(System.getenv("SERPAPI_API_KEY"))
                .orElseThrow(() -> new IllegalStateException("SERPAPI_API_KEY 环境变量未设置"));

        SearchApiWebSearchEngine searchEngine = SearchApiWebSearchEngine.builder()
                .apiKey(serpapiApiKey)
                .engine("google")
                .optionalParameters(optionalParameters)
                .build();
        WebSearchTool webSearchTool = WebSearchTool.from(searchEngine);
        return webSearchTool;
    }

    @Tool(name = "high_quality_internet_search", value = """
            当需要获取最新、高质量的网络信息时使用此工具。
            输入要求：精确的搜索查询字符串。
            适用场景：实时新闻、科技进展、市场数据等时效性强的信息。
            输出格式：返回包含关键信息的结构化摘要。
            """)
    public String search(@P("搜索关键词") String query) {
        try {
            log.info("🔍 执行SerpAPI网络搜索: {}", query);
            String result = defaultWebSearchTool.searchWeb(query);
            //return processSearchResults(result);
            return result;
        } catch (Exception e) {
            log.error("搜索 {} 失败,错误: {}", query, e.getMessage());
            return "无法获取搜索结果，请稍后重试";
        }
    }

    @Tool(name = "search_paper", value = """
            当需要获取最新学术论文资料时使用此工具。
            输入要求：精确的论文搜索关键词（示例："transformer model optimization 2023 site:arxiv.org"）
            专用参数：
            - 时间范围：近3年
            - 包含预印本
            - 领域过滤：计算机科学
            适用场景：
            1. 需要引用同行评审论文中的研究成果
            2. 查找特定领域的最新研究进展
            3. 验证技术方案的理论依据
            输出格式：
            【标题】论文标题
            【作者】作者列表
            【摘要】论文摘要（前200字符）
            【年份】出版年份
            【链接】论文访问链接
            ------------------
            （最多返回5篇最相关结果）
            """)
    public String searchPaper(@P("论文搜索关键词") String query) {
        try {
            log.info("📚 执行学术论文搜索: {}", query);
            String result = paperSearchTool.searchWeb(query);
            //return processAcademicResults(result);
            return result;
        } catch (Exception e) {
            log.error("论文搜索失败: {} | 错误: {}", query, e.getMessage());
            return "无法获取论文信息，请检查搜索关键词或稍后重试";
        }
    }

    /**
     * 处理学术搜索结果（ReAct提示词核心逻辑）
     */
    private String processAcademicResults(String rawResults) {
        return Arrays.stream(rawResults.split("\n\n"))
                .limit(5)
                .map(result -> {
                    // 实际应解析JSON响应，这里简化处理
                    String title = extractBetween(result, "\"title\":\"", "\"");
                    String authors = extractBetween(result, "\"authors\":\"", "\"");
                    String snippet = extractBetween(result, "\"snippet\":\"", "\"");
                    String year = extractBetween(result, "\"year\":\"", "\"");
                    String link = extractBetween(result, "\"link\":\"", "\"");

                    return String.format(
                            "【标题】%s\n【作者】%s\n【摘要】%.200s...\n【年份】%s\n【链接】%s\n--------------------",
                            title, authors, snippet, year, link
                    );
                })
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 辅助方法：提取JSON字段值
     */
    private String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end, startIndex);
        return endIndex > startIndex ? text.substring(startIndex, endIndex) : "未知信息";
    }


    /**
     * 处理搜索结果
     *
     * @param rawResults
     * @return
     */
    private String processSearchResults(String rawResults) {
        // 示例：提取前3条结果的核心信息
        // 实际应根据SerpAPI返回结构实现解析逻辑
        return Arrays.stream(rawResults.split("\n\n"))
                .limit(3)
                .map(result -> {
                    // 提取标题和摘要（伪代码）
                    String title = extractTitle(result);
                    String snippet = extractSnippet(result);
                    return String.format("【%s】%s", title, snippet);
                })
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 提取标题的示例方法（需根据实际API响应实现）
     */
    private String extractTitle(String result) {
        // 实际解析JSON或HTML逻辑
        return result.substring(0, Math.min(30, result.length())) + "...";
    }

    /**
     * 提取摘要的示例方法
     */
    private String extractSnippet(String result) {
        // 实际解析逻辑
        return result.length() > 100 ? result.substring(0, 100) + "..." : result;
    }
}
