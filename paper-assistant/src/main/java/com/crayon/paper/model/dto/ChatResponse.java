package com.crayon.paper.model.dto;

import lombok.Data;

/**
 * 对话请求响应
 * 参考了DeepSeek、QWen做法
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/27
 */
@Data
public class ChatResponse {

    /**
     * 消息ID (参考DS设计，使用自增整数)。
     * 作为下一轮对话的 parentMsgId
     */
    private Integer msgId;

    /**
     * 父消息ID (用于对话树结构)
     */
    private Integer parentMsgId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息内容
     */
    private String content;


    /**
     * 是否为流式响应
     */
    private boolean stream;

    /**
     * 状态 (参考DS设计: WIP/FINISHED)
     */
    private String status;
}
