package com.crayon.paper.listener;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 当 AI 服务在 Spring 上下文中注册时，该事件会被触发，允许你在运行时获取所有已注册 AI 服务及其工具的信息。<br>
 * 官方文档：https://docs.langchain4j.dev/tutorials/spring-boot-integration#listening-for-ai-service-registration-events
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/3
 */
@Slf4j
@Component
public class AIServiceRegisteredEventListener implements ApplicationListener<AiServiceRegisteredEvent> {
    @Override
    public void onApplicationEvent(AiServiceRegisteredEvent event) {
        Class<?> aiServiceClass = event.aiServiceClass();
        List<ToolSpecification> toolSpecifications = event.toolSpecifications();
        for (int i = 0; i < toolSpecifications.size(); i++) {
            log.info("[{}]: [Tool-{}]: {}", aiServiceClass.getSimpleName(), i + 1, toolSpecifications.get(i));
        }
    }
}
