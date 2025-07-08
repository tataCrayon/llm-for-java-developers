package com.crayon.paper.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 记忆管理<br>
 * 官方文档:https://docs.langchain4j.dev/tutorials/ai-services#chat-memory
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Slf4j
@Configuration
public class ChatMemoryConfig {


    private final ChatMemoryStore chatMemoryStore;

    public ChatMemoryConfig(ChatMemoryStore chatMemoryStore) {
        this.chatMemoryStore = chatMemoryStore;
    }

    /**
     * 获取
     *
     * @return
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10) // 可以根据需要配置最大消息数
                .chatMemoryStore(chatMemoryStore)
                .build();
    }

}
