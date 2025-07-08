package com.crayon.paper.model.dto;

import lombok.Data;

/**
 * 对话参数选项
 * 参考了Gemini设计，提供了部分
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/27
 */
@Data
public class ChatReqOptions {

    /**
     * 请求最大token数
     */
    private Integer maxTokens;

    /**
     * 温度值，取值范围0.0~1.0，值越大，生成的内容越随机，反之越确定
     */
    private Double temperature;

    /**
     * 另一种控制输出随机性的方法
     * 取值的概率阈值设定，0 到 1 之间。
     * return top_p
     */
    private Double topP;
}
