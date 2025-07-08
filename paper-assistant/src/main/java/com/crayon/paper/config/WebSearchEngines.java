package com.crayon.paper.config;

import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Map;
import java.util.Optional;

/**
 * 不同的Engine组件
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/2
 */
@Slf4j
@Component
public class WebSearchEngines {

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

    /**
     * SerpAPI搜索Engine
     */
    @Bean
    public SearchApiWebSearchEngine serpEngine() {
        String serpapiApiKey = Optional.ofNullable(System.getenv("SERPAPI_API_KEY"))
                .orElseThrow(() -> new IllegalStateException("SERPAPI_API_KEY 环境变量未设置"));

        SearchApiWebSearchEngine searchEngine = SearchApiWebSearchEngine.builder()
                .apiKey(serpapiApiKey)
                .engine("google")
                .optionalParameters(defaultOptionalParameters)
                .build();
        return searchEngine;
    }


    /**
     * 学术搜索参数
     */
    @Bean
    public SearchApiWebSearchEngine academicSerpEngine() {
        String serpapiApiKey = Optional.ofNullable(System.getenv("SERPAPI_API_KEY"))
                .orElseThrow(() -> new IllegalStateException("SERPAPI_API_KEY 环境变量未设置"));

        SearchApiWebSearchEngine searchEngine = SearchApiWebSearchEngine.builder()
                .apiKey(serpapiApiKey)
                .engine("google")
                .optionalParameters(academicOptionalParameters)
                .build();
        return searchEngine;
    }


}
