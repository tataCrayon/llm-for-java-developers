package com.crayon.paper.model.dto;

import lombok.Data;

/**
 * 流式响应数据传输对象
 * 参考了DeepSeek、QWen做法
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/27
 */
@Data
public class StreamResponse {

    private String sessionId;

    private Integer msgId;


    /**
     * 功能特性开关配置
     */
    private Features features;

    /**
     * 存储增量内容的字段，仅在中间帧使用
     */
    private String delta;

    /**
     * 响应状态：
     * - generating: 生成中
     * - finished: 已完成
     * - error: 错误
     */
    private String status;


    /**
     * 已使用的token数量
     */
    private Integer tokenUsed;

    /**
     * 完整响应内容（仅在结束帧中使用）
     */
    private String fullText;

    private String error;

    /**
     * 功能特性配置类
     */
    @Data
    public static class Features {
        /**
         * 是否允许分享
         */
        private boolean canShare;

        /**
         * 是否允许反馈
         */
        private boolean canFeedback;
    }
}