package com.crayon.paper.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * LLM流式服务顶级抽象
 * <p>
 * 官方文档：https://docs.langchain4j.dev/tutorials/response-streaming
 * <p>
 * 可以使用 TokenStream 返回流式响应。<br>
 * 也可以导入 langchain4j-reactor 后使用 Flux<String> 代替 TokenStream
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
public interface DSChatStreamService {
    @SystemMessage("你是一个专业的AI助手，请用中文，简洁、友好的方式回答问题。")
    TokenStream chat(@UserMessage String question);
}
