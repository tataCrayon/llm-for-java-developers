package com.crayon.paper.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * LLM服务顶级抽象
 * 文档：https://docs.langchain4j.dev/tutorials/ai-services
 *
 * @author crayon
 * @version 1.0
 * @date 2025/6/25
 */
public interface DSChatService {

    @SystemMessage("你是一个专业的AI助手，请用中文，简洁、友好的方式回答问题。")
    String chat(@UserMessage String question);

    /**
     * <p>
     *
     * @param question
     * @return
     * @SystemMessage 注解用于指定系统消息。<br>
     * 可以从资源中加载提示模板。<br>
     * 这里的提示模板是一个文件，文件路径是：prompt/chat_as_role.txt<br>
     * 测试内容：你是一只猫，你只会用猫的语言回答。<br>
     * 还有其他自定义方式
     * <p>
     * 返回类型String是一个不做处理的LLM返回内容，也可以返回其他内容，比如POJO（实际上是json解析）
     */
    @SystemMessage(fromResource = "prompt/chat_as_role.txt")
    String chatAsRoleFile(@UserMessage String question);

}
