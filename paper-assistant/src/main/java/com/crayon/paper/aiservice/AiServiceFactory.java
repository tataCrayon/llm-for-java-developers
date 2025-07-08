package com.crayon.paper.aiservice;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态获取LangChain4j的 AIService服务，且维护ChatMemory<br>
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Slf4j
@Component
public class AiServiceFactory {


    private final ChatModel deepSeekChat;

    private final ChatModel deepSeekR1;

    private final OpenAiStreamingChatModel deepSeekChatStream;

    private final ChatMemoryProvider chatMemoryProvider;

    /**
     * 统一的缓存Map：key 是模型类型 + sessionId，value 是对应的 AiService 实例
     */
    private final Map<String, Object> aiServiceCache = new ConcurrentHashMap<>();


    public AiServiceFactory(@Qualifier("deepseek-chat") ChatModel deepSeekChat,
                            @Qualifier("deepseek-reasoner") ChatModel deepSeekR1,
                            @Qualifier("deepseek-chat-stream") OpenAiStreamingChatModel deepSeekChatStream,
                            ChatMemoryProvider chatMemoryProvider) {
        this.deepSeekChat = deepSeekChat;
        this.deepSeekR1 = deepSeekR1;
        this.deepSeekChatStream = deepSeekChatStream;
        this.chatMemoryProvider = chatMemoryProvider;
    }

    /**
     * 通用方法：根据会话ID和模型类型获取或创建 AIService 实例。
     *
     * @param sessionId    会话ID
     * @param modelType    模型类型
     * @param serviceClass 要构建的 AIService 接口类 (如 DSChatService.class, DSChatStreamService.class)
     * @param <T>          AIService 接口的类型
     * @return 对应的 AIService 实例
     */
    @SuppressWarnings("unchecked")
    public <T> T getAiService(String sessionId, ModelType modelType, Class<T> serviceClass) {
        // 构建缓存 Key：模型类型 + "_" + sessionId
        String cacheKey = modelType.getName() + "_" + sessionId;

        return (T) aiServiceCache.computeIfAbsent(cacheKey, key -> {
            log.info("Creating new AiService instance for sessionId: {}, modelType: {}", sessionId, modelType.getName());

            ChatMemory chatMemory = chatMemoryProvider.get(sessionId);

            // 根据模型类型选择合适的 ChatModel 或 StreamingChatModel
            if (modelType == ModelType.DEEPSEEK_CHAT) {
                return AiServices.builder(serviceClass)
                        .chatModel(deepSeekChat)
                        .chatMemory(chatMemory)
                        .build();
            } else if (modelType == ModelType.DEEPSEEK_R1) {
                return AiServices.builder(serviceClass)
                        .chatModel(deepSeekR1)
                        .chatMemory(chatMemory)
                        .build();
            } else if (modelType == ModelType.DEEPSEEK_CHAT_STREAM) {
                return AiServices.builder(serviceClass)
                        .streamingChatModel(deepSeekChatStream)
                        .chatMemory(chatMemory)
                        .build();
            } else {
                throw new IllegalArgumentException("Unsupported ModelType: " + modelType);
            }
        });
    }

    /**
     * 当会话结束或需要清理时，从缓存中移除对应的 AiService 实例及其记忆。
     *
     * @param sessionId 会话ID
     */
    public void removeAiService(String sessionId) {
        // 遍历所有可能的模型类型，移除对应 sessionId 的缓存
        for (ModelType modelType : ModelType.values()) {
            String cacheKey = modelType.getName() + "_" + sessionId;
            if (aiServiceCache.containsKey(cacheKey)) {
                log.info("Removing AiService instance from cache for sessionId: {}, modelType: {}", sessionId, modelType.getName());
                aiServiceCache.remove(cacheKey);
            }
        }
        // 同时清理持久化存储中的记忆
        chatMemoryProvider.get(sessionId).clear();
    }

    /**
     * 枚举所有支持的 AI 模型类型，便于统一管理。
     */
    public enum ModelType {
        DEEPSEEK_CHAT("deepseek-chat"),
        DEEPSEEK_R1("deepseek-reasoner"),
        DEEPSEEK_CHAT_STREAM("deepseek-chat-stream");

        private final String name;

        ModelType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
