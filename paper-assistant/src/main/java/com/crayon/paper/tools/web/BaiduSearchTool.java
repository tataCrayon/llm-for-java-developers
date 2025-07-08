package com.crayon.paper.tools.web;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Baidu 搜索工具
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/26
 */
@Slf4j
@Component
public class BaiduSearchTool {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final int MAX_RETRIES = 2;
    private static final int TIMEOUT_SECONDS = 15;
    private static final int MAX_SUMMARY_LENGTH = 2000;
    private static final String[] RESULT_SELECTORS = {".result-op", ".c-container"};

    //CSS选择器
    private static final String[] SNIPPET_SELECTORS = {".c-abstract", ".content-right_8Zs40"};
    private static final String SEARCH_URL_TEMPLATE = "https://www.baidu.com/s?wd=%s";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newFixedThreadPool(5))
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Tool(name = "baidu_search", value = """
            当需要在线搜索最新信息时，默认使用此工具。
            输入格式：一个精确的搜索查询。
            适用场景：实时新闻、科技进展、市场数据等时效性强的信息。
            输出格式：返回包含关键信息的结构化摘要。
            """)
    public String search(String query) {
        log.info("🔍 执行 Baidu 网络搜索: {}", query);
        int retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                HttpRequest request = createSearchRequest(query);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Baidu response:{}", response.body());
                if (response.statusCode() == 200) {
                    return parseBaiduHtml(response.body());
                }

                return handleErrorResponse(response.statusCode(), retries);
            } catch (IOException | InterruptedException e) {
                return handleNetworkError(e, retries);
            }
        }
        return "搜索失败，达到最大重试次数";
    }

    /**
     * 创建HTTP请求
     *
     * @param query
     * @return
     * @throws IOException
     */
    private HttpRequest createSearchRequest(String query) throws IOException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format(SEARCH_URL_TEMPLATE, encodedQuery)))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    /**
     * 解析HTML内容
     *
     * @param html
     * @return
     */
    private String parseBaiduHtml(String html) {
        Document doc = Jsoup.parse(html);
        Elements results = findElementsWithFallback(doc, RESULT_SELECTORS);

        StringBuilder summary = new StringBuilder();
        for (Element result : results) {
            Element title = result.selectFirst("h3");
            Element snippet = findElementWithFallback(result, SNIPPET_SELECTORS);
            if (title != null && snippet != null) {
                summary.append(title.text()).append("\n")
                        .append(snippet.text()).append("\n\n");
            }
        }
        return formatSummary(summary.toString());
    }

    /**
     * 选择器的元素查找
     *
     * @param doc
     * @param selectors
     * @return
     */
    private Elements findElementsWithFallback(Document doc, String[] selectors) {
        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                return elements;
            }
        }
        return new Elements();
    }

    // 新增方法：带备用选择器的单个元素查找
    private Element findElementWithFallback(Element element, String[] selectors) {
        for (String selector : selectors) {
            Element found = element.selectFirst(selector);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 格式化最终结果
     *
     * @param summaryText
     * @return
     */
    private String formatSummary(String summaryText) {
        if (summaryText.isEmpty()) {
            return "未找到相关摘要信息";
        }
        // 优先截断到最后一个完整句子
        int lastSentenceEnd = Math.min(summaryText.length(), MAX_SUMMARY_LENGTH);
        lastSentenceEnd = Math.max(summaryText.lastIndexOf('。', lastSentenceEnd),
                summaryText.lastIndexOf('.', lastSentenceEnd));
        return summaryText.substring(0, lastSentenceEnd > 0 ? lastSentenceEnd + 1 : MAX_SUMMARY_LENGTH);
    }

    /**
     * 统一处理错误响应
     *
     * @param statusCode
     * @param retries
     * @return
     */
    private String handleErrorResponse(int statusCode, int retries) {
        log.warn("搜索请求失败，状态码: {}", statusCode);
        if (shouldRetry(statusCode, retries)) {
            return attemptRetry(retries);
        }
        return "搜索失败，HTTP 状态码: " + statusCode;
    }

    /**
     * 统一处理网络错误
     *
     * @param e
     * @param retries
     * @return
     */
    private String handleNetworkError(Exception e, int retries) {
        Thread.currentThread().interrupt();
        log.error("网络错误: {}", e.getMessage());
        log.error("网络请求异常类型: {}", e.getClass().getName());
        log.error("完整异常堆栈:", e);
        if (retries < MAX_RETRIES - 1) {
            return attemptRetry(retries);
        }
        return "搜索时发生错误: " + e.getMessage();
    }

    /**
     * 判断是否需要重试
     *
     * @param statusCode
     * @param retries
     * @return
     */
    private boolean shouldRetry(int statusCode, int retries) {
        return (statusCode >= 500 && statusCode < 600) && retries < MAX_RETRIES - 1;
    }

    /**
     * 执行重试操作
     *
     * @param retries
     * @return
     */
    private String attemptRetry(int retries) {
        retries++;
        log.info("正在重试 (第{}次/共{}次)", retries, MAX_RETRIES);
        return "搜索失败，达到最大重试次数";
    }

}
