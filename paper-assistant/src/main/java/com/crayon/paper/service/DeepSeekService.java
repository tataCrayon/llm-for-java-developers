package com.crayon.paper.service;

import com.crayon.paper.aiservice.AiServiceFactory;
import com.crayon.paper.aiservice.DSChatService;
import com.crayon.paper.aiservice.DSChatStreamService;
import com.crayon.paper.model.dto.ChatRequest;
import com.crayon.paper.model.dto.ChatResponse;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * DeepSeek服务类
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/2
 */
@Slf4j
@Service
public class DeepSeekService {

    private final AiServiceFactory aiServiceFactory;

    public DeepSeekService(AiServiceFactory aiServiceFactory) {
        this.aiServiceFactory = aiServiceFactory;
    }

    /**
     * 普通聊天接口
     *
     * @param chatRequest
     * @return
     */
    public ChatResponse chat(ChatRequest chatRequest) {
        // 校验sessionId
        String sessionId = chatRequest.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            log.error("sessionId为空");
            throw new RuntimeException("sessionId为空，请先获取session_id");
        }

        String userMessage = chatRequest.getUserMessage();
        log.info("使用deepseek-chat,回答用户问题：【{}】", userMessage);
        DSChatService dsChatService = aiServiceFactory.getAiService(sessionId, AiServiceFactory.ModelType.DEEPSEEK_CHAT, DSChatService.class);
        String response = dsChatService.chat(userMessage);
        if (response == null || response.isEmpty()) {
            log.error("请求DeepSeek服务失败");
            throw new RuntimeException("请求DeepSeek服务失败");
        }
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setMsgId(chatRequest.getParentMsgId() != null ? chatRequest.getParentMsgId() + 1 : 1);
        chatResponse.setParentMsgId(chatRequest.getParentMsgId());
        chatResponse.setSessionId(chatRequest.getSessionId());
        chatResponse.setContent(response);
        chatResponse.setStatus("FINISHED");
        return chatResponse;
    }

    public TokenStream streamChat(ChatRequest chatRequest) {
        // 校验sessionId
        String sessionId = chatRequest.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            log.error("sessionId为空");
            throw new RuntimeException("sessionId为空，请先获取session_id");
        }

        String userMessage = chatRequest.getUserMessage();
        log.info("使用deepseek-chat,流式响应用户问题：【{}】", userMessage);
        DSChatStreamService streamService = aiServiceFactory.getAiService(sessionId, AiServiceFactory.ModelType.DEEPSEEK_CHAT_STREAM, DSChatStreamService.class);
        TokenStream tokenStream = streamService.chat(userMessage);
        if (tokenStream == null) {
            log.error("请求DeepSeek服务失败");
            throw new RuntimeException("请求DeepSeek服务失败");
        }
        return tokenStream;
    }
}
