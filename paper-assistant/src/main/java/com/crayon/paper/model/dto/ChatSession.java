package com.crayon.paper.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储用户会话
 * <p>
 * 会话保存方案有两种：
 * 1、类DS与QW的自动保存
 * 2、类Gemini的手动保存
 *
 * <p>
 * 这里简单内存，TODO改造
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    String sessionId;

    long parentMsgId;

    StringBuilder fullText = new StringBuilder();

    public ChatSession(String sessionId, long parentMsgId) {
        this.sessionId = sessionId;
        this.parentMsgId = parentMsgId;
    }
}
