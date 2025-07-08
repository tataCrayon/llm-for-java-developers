package com.crayon.paper.tools.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 联网搜索工具类
 * 提供学术论文搜索功能，整合多个数据源
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaperSearchTool {

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    @Value("${app.search.academic.arxiv-base-url}")
    private String arxivBaseUrl;

    @Value("${app.search.academic.semantic-scholar-base-url}")
    private String semanticScholarBaseUrl;

    @Value("${app.search.academic.max-results:10}")
    private int maxResults;

    /**
     * 搜索LLM相关论文
     * 使用ArXiv和Semantic Scholar等学术数据库搜索相关论文
     *
     * @param query 搜索查询词，如"transformer attention mechanism"
     * @return 格式化的论文搜索结果，包含标题、作者、摘要、链接等信息
     */
    @Tool(name = "searchLLMPapers", value = """
            搜索LLM和机器学习相关的学术论文，返回论文的详细信息用于学习研究。
            输入要求：精确的搜索查询字符串。
            """)
    public String searchLLMPapers(String query) {
        log.info("开始搜索LLM论文，查询词: {}", query);
        try {
            List<PaperResult> results = new ArrayList<>();
            // 搜索ArXiv论文
            List<PaperResult> arxivResults = searchArXiv(query);
            results.addAll(arxivResults);

            // 如果已经找到足够的结果，停止搜索
            // 去重和排序
            results = deduplicateAndSort(results);

            // 限制结果数量
            if (results.size() > maxResults) {
                results = results.subList(0, maxResults);
            }
            String finalResult = formatSearchResults(results, query);
            log.info("searchLLMPapers finalResult: {}", finalResult);

            return finalResult;

        } catch (Exception e) {
            log.error("搜索论文时发生错误: {}", e.getMessage(), e);
            return "搜索过程中发生错误: " + e.getMessage();
        }
    }


    /**
     * 去重和排序
     */
    private List<PaperResult> deduplicateAndSort(List<PaperResult> results) {
        // 使用Set去重，基于标题
        Set<String> seenTitles = new HashSet<>();
        List<PaperResult> deduplicated = new ArrayList<>();

        for (PaperResult paper : results) {
            String normalizedTitle = paper.getTitle().toLowerCase().trim();
            if (!seenTitles.contains(normalizedTitle)) {
                seenTitles.add(normalizedTitle);
                deduplicated.add(paper);
            }
        }

        // 按发布日期排序（最新的在前）
        deduplicated.sort((a, b) -> {
            String dateA = a.getPublishedDate();
            String dateB = b.getPublishedDate();
            if (dateA == null) dateA = "";
            if (dateB == null) dateB = "";
            return dateB.compareTo(dateA);
        });

        return deduplicated;
    }

    /**
     * 在ArXiv中搜索论文
     *
     * @param query 搜索查询词
     * @return 论文结果列表
     */
    private List<PaperResult> searchArXiv(String query) {
        List<PaperResult> results = new ArrayList<>();

        try {
            // 构建ArXiv API查询URL
            String searchUrl = String.format("%s?search_query=all:%s&start=0&max_results=%d&sortBy=submittedDate&sortOrder=descending",
                    arxivBaseUrl, query.replace(" ", "+"), maxResults);

            log.debug("ArXiv搜索URL: {}", searchUrl);

            // 发起HTTP请求
            String response = webClient.get()
                    .uri(searchUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null && !response.isEmpty()) {
                log.debug("响应内容预览: {}", response.substring(0, Math.min(500, response.length())));
                results = parseArXivResponse(response);
                log.info("从ArXiv获取到 {} 篇论文", results.size());
            } else {
                log.warn("ArXiv响应为空或null");
            }

        } catch (Exception e) {
            log.error("搜索ArXiv时发生错误: {}", e.getMessage(), e);
            // TODO 超时备用方案
        }

        return results;
    }

    /**
     * 解析ArXiv API响应
     *
     * @param xmlResponse ArXiv返回的XML响应
     * @return 解析后的论文结果列表
     */
    private List<PaperResult> parseArXivResponse(String xmlResponse) {
        List<PaperResult> results = new ArrayList<>();
        log.info("开始解析从ArXiv获取的响应");
        try {
            // 使用正则表达式或简单字符串处理来解析XML
            // 首先检查是否包含entry标签
            if (!xmlResponse.contains("<entry>")) {
                log.warn("XML响应中未找到entry标签");
                return results;
            }

            // 分割每个entry
            String[] entries = xmlResponse.split("<entry>");
            log.debug("找到 {} 个entry块", entries.length - 1);

            for (int i = 1; i < entries.length && results.size() < maxResults; i++) {
                String entry = entries[i];

                try {
                    PaperResult paper = new PaperResult();

                    // 提取标题 - 处理可能的换行和空格
                    String title = extractXmlField(entry, "title");
                    if (title.isEmpty()) {
                        log.debug("跳过没有标题的entry");
                        continue;
                    }
                    paper.setTitle(cleanText(title));

                    // 提取作者
                    String authors = extractAuthorsImproved(entry);
                    paper.setAuthors(authors);

                    // 提取摘要
                    String summary = extractXmlField(entry, "summary");
                    paper.setSummary(cleanText(summary));

                    // 提取发布日期
                    String published = extractXmlField(entry, "published");
                    if (!published.isEmpty() && published.length() >= 10) {
                        paper.setPublishedDate(published.substring(0, 10));
                    } else {
                        paper.setPublishedDate("未知");
                    }

                    // 提取ArXiv ID和链接 - 链接提取
                    String id = extractXmlField(entry, "id");
                    paper.setUrl(id);
                    paper.setSource("ArXiv");

                    // 提取分类 - 分类提取
                    String category = extractCategoryImproved(entry);
                    paper.setCategory(category);

                    if (isValidPaper(paper)) {
                        results.add(paper);
                        log.debug("成功解析论文: {}", paper.getTitle());
                    } else {
                        log.debug("跳过无效论文记录");
                    }

                } catch (Exception e) {
                    log.warn("解析第 {} 个entry时出错: {}", i, e.getMessage());
                    continue;
                }
            }
            log.info("成功解析 {} 篇论文", results.size());

        } catch (Exception e) {
            log.error("解析ArXiv响应时发生错误: {}", e.getMessage(), e);
        }

        return results;
    }


    /**
     * 从XML中提取指定字段的值
     */
    private String extractXmlField(String xml, String fieldName) {
        try {
            // 处理自闭合标签的情况
            String pattern1 = "<" + fieldName + ">(.*?)</" + fieldName + ">";
            Pattern p1 = Pattern.compile(pattern1, Pattern.DOTALL);
            Matcher m1 = p1.matcher(xml);

            if (m1.find()) {
                return m1.group(1).trim();
            }

            // 如果没有找到，尝试处理可能的属性情况
            String pattern2 = "<" + fieldName + "[^>]*>(.*?)</" + fieldName + ">";
            Pattern p2 = Pattern.compile(pattern2, Pattern.DOTALL);
            Matcher m2 = p2.matcher(xml);

            if (m2.find()) {
                return m2.group(1).trim();
            }

        } catch (Exception e) {
            log.debug("提取字段 {} 时出错: {}", fieldName, e.getMessage());
        }

        return "";
    }

    /**
     * 作者信息提取
     */
    private String extractAuthorsImproved(String entry) {
        StringBuilder authors = new StringBuilder();

        try {
            // 使用正则表达式匹配所有author块
            Pattern authorPattern = Pattern.compile("<author>(.*?)</author>", Pattern.DOTALL);
            Matcher authorMatcher = authorPattern.matcher(entry);

            while (authorMatcher.find()) {
                String authorBlock = authorMatcher.group(1);
                String authorName = extractXmlField(authorBlock, "name");

                if (!authorName.isEmpty()) {
                    if (authors.length() > 0) {
                        authors.append(", ");
                    }
                    authors.append(authorName.trim());
                }
            }

            // 如果没有找到author标签，尝试其他可能的格式
            if (authors.length() == 0) {
                // 可能是简单的name标签
                Pattern namePattern = Pattern.compile("<name>(.*?)</name>", Pattern.DOTALL);
                Matcher nameMatcher = namePattern.matcher(entry);

                while (nameMatcher.find()) {
                    String name = nameMatcher.group(1).trim();
                    if (!name.isEmpty()) {
                        if (authors.length() > 0) {
                            authors.append(", ");
                        }
                        authors.append(name);
                    }
                }
            }

        } catch (Exception e) {
            log.debug("提取作者信息时出错: {}", e.getMessage());
        }

        return authors.toString();
    }

    /**
     * 论文分类提取
     */
    private String extractCategoryImproved(String entry) {
        try {
            // 查找primary_category或category标签
            Pattern primaryCategoryPattern = Pattern.compile("<arxiv:primary_category[^>]*term=\"([^\"]+)\"", Pattern.DOTALL);
            Matcher primaryMatcher = primaryCategoryPattern.matcher(entry);

            if (primaryMatcher.find()) {
                return primaryMatcher.group(1);
            }

            // 如果没有找到primary_category，查找第一个category
            Pattern categoryPattern = Pattern.compile("<category[^>]*term=\"([^\"]+)\"", Pattern.DOTALL);
            Matcher categoryMatcher = categoryPattern.matcher(entry);

            if (categoryMatcher.find()) {
                return categoryMatcher.group(1);
            }

        } catch (Exception e) {
            log.debug("提取分类信息时出错: {}", e.getMessage());
        }

        return "未分类";
    }

    /**
     * 文本清理
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text
                .replaceAll("\\s+", " ")  // 将多个空白字符替换为单个空格
                .replaceAll("\\n", " ")   // 将换行符替换为空格
                .replaceAll("\\r", " ")   // 将回车符替换为空格
                .replaceAll("\\t", " ")   // 将制表符替换为空格
                .trim();                  // 去除首尾空格
    }

    /**
     * 论文有效性验证
     */
    private boolean isValidPaper(PaperResult paper) {
        return paper.getTitle() != null && !paper.getTitle().trim().isEmpty() &&
                paper.getSummary() != null && !paper.getSummary().trim().isEmpty() &&
                paper.getAuthors() != null && !paper.getAuthors().trim().isEmpty();
    }

    /**
     * 格式化搜索结果为学习友好的格式
     */
    private String formatSearchResults(List<PaperResult> results, String query) {
        if (results.isEmpty()) {
            return String.format("未找到关于 '%s' 的相关论文。建议尝试其他关键词。", query);
        }

        StringBuilder formatted = new StringBuilder();
        formatted.append(String.format("=== 关于 '%s' 的论文搜索结果 ===\n\n", query));
        formatted.append(String.format("共找到 %d 篇相关论文：\n\n", results.size()));

        for (int i = 0; i < results.size(); i++) {
            PaperResult paper = results.get(i);
            formatted.append(String.format("【论文 %d】\n", i + 1));
            formatted.append(String.format("📄 标题: %s\n", paper.getTitle()));
            formatted.append(String.format("👥 作者: %s\n", paper.getAuthors()));
            formatted.append(String.format("📅 发布日期: %s\n", paper.getPublishedDate()));
            formatted.append(String.format("🏷️ 分类: %s\n", paper.getCategory()));
            formatted.append(String.format("🔗 链接: %s\n", paper.getUrl()));
            formatted.append(String.format("📝 摘要: %s\n",
                    paper.getSummary().length() > 300 ?
                            paper.getSummary().substring(0, 300) + "..." :
                            paper.getSummary()));
            formatted.append("\n" + "=".repeat(50) + "\n\n");
        }

        formatted.append("💡 学习建议：\n");
        formatted.append("1. 优先阅读最新发布的论文，了解领域前沿动态\n");
        formatted.append("2. 关注引用量高的经典论文，掌握基础理论\n");
        formatted.append("3. 比较不同方法的优缺点，形成系统性理解\n");
        formatted.append("4. 实践论文中的算法和实验，加深理解\n");

        return formatted.toString();
    }

    /**
     * 论文结果数据类
     */
    public static class PaperResult {
        private String title;
        private String authors;
        private String summary;
        private String publishedDate;
        private String url;
        private String source;
        private String category;

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthors() {
            return authors;
        }

        public void setAuthors(String authors) {
            this.authors = authors;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}