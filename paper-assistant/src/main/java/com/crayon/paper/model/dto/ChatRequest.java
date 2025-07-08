package com.crayon.paper.model.dto;

import lombok.Data;

/**
 * 对话请求传输对象
 * 参考了DeepSeek、QWen做法
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/27
 */
@Data
public class ChatRequest {


    /**
     * 对话id
     */
    private String sessionId;

    /**
     * 上一条消息id
     * 采取DS极简方案
     */
    private Integer parentMsgId;

    /**
     * 用户输入内容
     */
    private String userMessage;

    /**
     * stream模式
     */
    private boolean stream;

    /**
     * 对话参数选项
     */
    private ChatReqOptions options;

}
