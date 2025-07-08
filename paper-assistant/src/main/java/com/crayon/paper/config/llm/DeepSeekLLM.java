package com.crayon.paper.config.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek LLM实例封装<br>
 * <p>
 * 参数参考：https://docs.langchain4j.dev/tutorials/model-parameters
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/26
 */
@Slf4j
@Component
public class DeepSeekLLM {

    private static final Double TEMPERATURE = 0.7;

    private static final String DEEP_SEEK_CHAT_MODEL_NAME = "deepseek-chat";
    private static final String DEEP_SEEK_REASONER_MODEL_NAME = "deepseek-reasoner";
    private static final String DEEP_SEEK_API_KEY_NAME = "DEEPSEEK_API_KEY";
    private static final String DEEP_SEEK_BASE_URL = "https://api.deepseek.com/v1";

    private final ChatModelListener toolsCallListener;

    public DeepSeekLLM(@Qualifier("toolsCallBackListener") ChatModelListener toolsCallListener) {
        this.toolsCallListener = toolsCallListener;
    }

    @Bean("deepseek-chat")
    public ChatModel deepSeekChatModel() {
        log.debug("创建 deepseek-chat 实例");
        String apiKey = getDeepSeekApiKey();

        try {
            List<ChatModelListener> listeners = getListeners();
            log.debug("listeners of deepseek-chat: {}", listeners);

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(DEEP_SEEK_CHAT_MODEL_NAME)
                    .logRequests(true)
                    .logResponses(true)
                    .baseUrl(DEEP_SEEK_BASE_URL)
                    .temperature(TEMPERATURE)
                    .listeners(listeners)
                    .build();

            log.info("成功创建 deepseek-chat 实例");
            return model;
        } catch (Exception e) {
            log.error("创建 deepseek-chat 实例失败", e);
            throw new RuntimeException("Failed to create deepseek-chat model", e);
        }
    }

    @Bean("deepseek-reasoner")
    public ChatModel deepSeekR1Model() {
        log.debug("创建 deepseek-reasoner 实例");
        String apiKey = getDeepSeekApiKey();

        try {
            List<ChatModelListener> listeners = getListeners();
            log.debug("listeners of deepseek-reasoner: {}", listeners);
            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(DEEP_SEEK_REASONER_MODEL_NAME)
                    .logRequests(true)
                    .logResponses(true)
                    .baseUrl(DEEP_SEEK_BASE_URL)
                    .temperature(TEMPERATURE)
                    .listeners(listeners)
                    .build();

            log.info("成功创建 deepseek-reasoner 实例");
            return model;
        } catch (Exception e) {
            log.error("创建 deepseek-reasoner 实例失败", e);
            throw new RuntimeException("Failed to create deepseek-reasoner model", e);
        }
    }

    @Bean("deepseek-chat-stream")
    public OpenAiStreamingChatModel deepSeekChatStreamModel() {
        log.debug("创建 deepseek-chat-stream 实例");
        String apiKey = getDeepSeekApiKey();
        try {
            List<ChatModelListener> listeners = getListeners();
            log.debug("listeners of deepseek-chat-stream: {}", listeners);
            OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(DEEP_SEEK_CHAT_MODEL_NAME)
                    .logRequests(true)
                    .logResponses(true)
                    .baseUrl(DEEP_SEEK_BASE_URL)
                    .temperature(TEMPERATURE)
                    .listeners(listeners)
                    .build();

            log.info("成功创建 deepseek-chat-stream 实例");
            return model;
        } catch (Exception e) {
            log.error("创建 deepseek-chat-stream 实例失败", e);
            throw new RuntimeException("Failed to create deepseek-chat-stream model", e);
        }
    }

    private String getDeepSeekApiKey() {
        String apiKey = System.getenv(DEEP_SEEK_API_KEY_NAME);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("DEEPSEEK_API_KEY environment variable is not set or empty");
            throw new RuntimeException("DEEPSEEK_API_KEY environment variable is not set or empty");
        }
        return apiKey;
    }

    private List<ChatModelListener> getListeners() {
        List<ChatModelListener> listeners = new ArrayList<>();
        if (toolsCallListener != null) {
            listeners.add(toolsCallListener);
        }
        return listeners;
    }


}