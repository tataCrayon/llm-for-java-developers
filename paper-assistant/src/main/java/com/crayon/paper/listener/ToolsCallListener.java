package com.crayon.paper.listener;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 为 ChatModel 或 StreamingChatModel 的 bean 启用可观测性。<br>
 * 类似python的AgentTraceCallback。<br>
 * 文档：https://docs.langchain4j.dev/tutorials/spring-boot-integration#spring-boot-starters <br>
 * https://docs.langchain4j.dev/tutorials/observability <br>
 * 文档搜索关键词：Observability
 * <p>
 * 另外的方式：<br>
 * 实现ToolExecutionRequestHandler
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/3
 */
@Slf4j
@Configuration
public class ToolsCallListener {

    /**
     * 用于记录每个会话的详细信息
     */
    private final ConcurrentHashMap<String, SessionInfo> sessionInfos = new ConcurrentHashMap<>();

    /**
     * 线程本地存储，用于在同一个请求-响应周期内保持会话ID一致
     */
    private final ThreadLocal<String> currentSessionId = new ThreadLocal<>();


    @Bean("toolsCallBackListener")
    ChatModelListener chatModelListener() {
        return new ChatModelListener() {

            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                ChatRequest chatRequest = requestContext.chatRequest();
                String sessionId = generateSessionId(requestContext);

                // 将会话ID存储在ThreadLocal中，确保在同一个请求-响应周期内保持一致
                currentSessionId.set(sessionId);

                log.info("onRequest() - Session: {}", sessionId);

                // 检查是否有工具执行请求
                boolean hasToolCall = chatRequest.messages().stream()
                        .anyMatch(msg -> msg instanceof AiMessage &&
                                ((AiMessage) msg).hasToolExecutionRequests());

                if (hasToolCall) {
                    SessionInfo sessionInfo = sessionInfos.computeIfAbsent(sessionId, k -> new SessionInfo());
                    int currentIteration = sessionInfo.incrementAndGet();
                    log.info("\n--- 🤔 思考轮次: {} (会话: {}) ---", currentIteration, sessionId);
                }
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                ChatResponse response = responseContext.chatResponse();
                String sessionId = currentSessionId.get();

                if (sessionId == null) {
                    // 如果ThreadLocal中没有，使用线程ID作为兜底
                    sessionId = Thread.currentThread().getName() + "-" + Thread.currentThread().getId();
                }

                log.info("onResponse() - Session: {}", sessionId);

                if (response.aiMessage().hasToolExecutionRequests()) {
                    SessionInfo sessionInfo = sessionInfos.get(sessionId);
                    int currentCount = sessionInfo != null ? sessionInfo.get() : 0;
                    log.info("AI 请求执行工具: {} 个 (当前轮次: {})",
                            response.aiMessage().toolExecutionRequests().size(), currentCount);
                } else {
                    // 思考结束，清理计数器
                    SessionInfo removed = sessionInfos.remove(sessionId);
                    if (removed != null) {
                        log.info("思考结束，清理会话 {} 的计数器，最终轮次: {}",
                                sessionId, removed.get());
                    }
                    // 清理ThreadLocal
                    currentSessionId.remove();
                }
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                String sessionId = currentSessionId.get();
                if (sessionId == null) {
                    sessionId = Thread.currentThread().getName() + "-" + Thread.currentThread().getId();
                }
                try {
                    // 获取错误信息，若为 null 则使用默认提示
                    String errorMessage = errorContext.error() != null ?
                            (errorContext.error().getMessage() != null ? errorContext.error().getMessage() : "未提供错误信息") :
                            "未捕获到异常对象";
                    log.error("onError() - Session: {}, Error: {}", sessionId, errorMessage);

                    SessionInfo removed = sessionInfos.remove(sessionId);
                    if (removed != null) {
                        log.info("出错后清理会话 {} 的计数器，最终轮次: {}",
                                sessionId, removed.get());
                    }
                } catch (Exception e) {
                    // 捕获并记录处理错误时发生的异常
                    log.error("处理 onError 事件时发生额外异常 - Session: {}, 额外错误信息: {}", sessionId, e.getMessage(), e);
                } finally {
                    // 确保 ThreadLocal 被清理
                    currentSessionId.remove();
                }
            }
        };
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId(ChatModelRequestContext context) {
        // 使用时间戳 + 线程ID + 请求特征生成相对稳定的会话ID
        String threadId = Thread.currentThread().getName() + "-" + Thread.currentThread().getId();
        long timestamp = System.currentTimeMillis();
        try {
            ChatRequest request = context.chatRequest();
            if (request != null && request.messages() != null && !request.messages().isEmpty()) {
                // 使用第一条和最后一条消息的内容生成特征
                String firstMsg = getMessageText(request.messages().get(0));
                String lastMsg = getMessageText(request.messages().get(request.messages().size() - 1));

                if (firstMsg != null && lastMsg != null) {
                    int contentHash = Objects.hash(firstMsg, lastMsg, request.messages().size());
                    return threadId + "-" + Math.abs(contentHash);
                }
            }
        } catch (Exception e) {
            log.debug("无法生成内容特征，使用默认会话ID", e);
        }

        return threadId + "-" + timestamp;
    }


    /**
     * 获取消息文本内容的辅助方法
     */
    private String getMessageText(ChatMessage message) {
        if (message == null) {
            return null;
        }

        // 根据消息类型获取内容
        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) message;
            return userMessage.singleText();
            // return userMessage.contents().get(0).text(); // 如果是多内容消息
        } else if (message instanceof AiMessage) {
            AiMessage aiMessage = (AiMessage) message;
            return aiMessage.text();
        } else if (message instanceof SystemMessage) {
            SystemMessage systemMessage = (SystemMessage) message;
            return systemMessage.text();
        }

        // 如果以上都不匹配，使用 toString() 作为最后的选择
        return message.toString();
    }


    /**
     * 定期清理过期会话
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        long expireTime = 10 * 60 * 1000; // 10分钟过期

        sessionInfos.entrySet().removeIf(entry -> {
            boolean expired = currentTime - entry.getValue().getLastAccessTime() > expireTime;
            if (expired) {
                log.debug("清理过期会话: {}, 最终轮次: {}",
                        entry.getKey(), entry.getValue().get());
            }
            return expired;
        });
    }


    /**
     * 会话信息，包含计数器和时间戳
     */
    private static class SessionInfo {
        private final AtomicInteger counter = new AtomicInteger(0);
        private volatile long lastAccessTime = System.currentTimeMillis();

        public int incrementAndGet() {
            lastAccessTime = System.currentTimeMillis();
            return counter.incrementAndGet();
        }

        public int get() {
            return counter.get();
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }


}
