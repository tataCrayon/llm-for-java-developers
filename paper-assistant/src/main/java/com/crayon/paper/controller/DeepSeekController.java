package com.crayon.paper.controller;

import com.crayon.paper.model.dto.ChatRequest;
import com.crayon.paper.model.dto.ChatResponse;
import com.crayon.paper.model.dto.StreamResponse;
import com.crayon.paper.service.DeepSeekService;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DeepSeek对话相关接口控制器
 * 提供普通与流式对话API，支持SSE流式响应。
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Slf4j
@RestController
@RequestMapping("/ds")
public class DeepSeekController {

    private DeepSeekService deepSeekService;

    public DeepSeekController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    /**
     * 普通聊天接口
     *
     * @param message
     * @return
     */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest) {
        return deepSeekService.chat(chatRequest);
    }

    /**
     * 流式聊天接口
     *
     * @param message 聊天消息
     * @return SseEmitter对象，用于流式响应
     */
    @PostMapping("/stream/chat")
    public SseEmitter streamChat(@RequestBody ChatRequest chatRequest) {
        SseEmitter emitter = new SseEmitter(60_000L);
        TokenStream tokenStream = deepSeekService.streamChat(chatRequest);
        StringBuilder fullText = new StringBuilder();

        AtomicBoolean isError = new AtomicBoolean(false);

        // 发送初始帧
        StreamResponse initResponse = new StreamResponse();
        initResponse.setSessionId(chatRequest.getSessionId());
        initResponse.setMsgId(chatRequest.getParentMsgId() != null ? chatRequest.getParentMsgId() + 1 : 1);
        StreamResponse.Features features = new StreamResponse.Features();
        features.setCanShare(true);
        features.setCanFeedback(false);
        initResponse.setFeatures(features);
        try {
            emitter.send(SseEmitter.event().data(initResponse));
        } catch (IOException e) {
            handleError(emitter, e, "发送初始帧失败");
            return emitter;
        }

        // 流式响应处理
        tokenStream.onPartialResponse(partialResponse -> {
            fullText.append(partialResponse);
            try {
                //  增量帧：发送实时更新内容
                StreamResponse deltaResponse = new StreamResponse();
                deltaResponse.setDelta(partialResponse);
                deltaResponse.setStatus("generating");
                emitter.send(SseEmitter.event().data(deltaResponse));

                // 状态帧：发送 token 使用情况
                StreamResponse statusResponse = new StreamResponse();
                statusResponse.setStatus("generating");
                statusResponse.setTokenUsed(partialResponse.length());
                emitter.send(SseEmitter.event().data(statusResponse));
            } catch (IOException e) {
                isError.set(true);
                handleError(emitter, e, "发送增量帧或状态帧失败");
            }
        }).onError(error -> {
            isError.set(true);
            StreamResponse errorResponse = new StreamResponse();
            errorResponse.setStatus("error");
            errorResponse.setError(error.getMessage());
            try {
                emitter.send(SseEmitter.event().data(errorResponse));
            } catch (IOException e) {
                log.error("发送错误帧失败", e);
            } finally {
                emitter.complete();
            }
        }).onCompleteResponse(chatResponse -> {
            try {
                // 结束帧：发送完整响应内容
                StreamResponse endResponse = new StreamResponse();
                endResponse.setStatus("finished");
                endResponse.setFullText(fullText.toString());
                emitter.send(SseEmitter.event().data(endResponse));
            } catch (IOException e) {
                log.error("发送结束帧失败", e);
            } finally {
                emitter.complete();
            }
        }).start();

        return emitter;
    }

    private void handleError(SseEmitter emitter, Exception e, String message) {
        log.error(message, e);
        StreamResponse errorResponse = new StreamResponse();
        errorResponse.setStatus("error");
        errorResponse.setError(e.getMessage());
        try {
            emitter.send(SseEmitter.event().data(errorResponse));
        } catch (IOException ioException) {
            log.error("发送错误帧失败", ioException);
        }
        emitter.completeWithError(e);
    }

}
