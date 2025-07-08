package com.crayon.paper.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 持久化聊天记忆存储实现类。可以实现ChatMemoryStore来自定义ChatMessage持久化存储<br>
 * 官方文档：https://docs.langchain4j.dev/tutorials/chat-memory#persistence <br>
 * <p>
 * 暂且使用内存存储，后续改用数据库存储
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Slf4j
@Component
public class PersistentChatMemoryStore implements ChatMemoryStore {

    /**
     * 使用 ConcurrentHashMap 作为内存存储。
     * Key 是 memoryId (即 sessionId)，Value 是该会话的消息列表。
     * ConcurrentHashMap 是线程安全的，适合多线程环境。
     */
    private final Map<Object, List<ChatMessage>> inMemoryStore = new ConcurrentHashMap<>();


    /**
     * 检索指定聊天记忆的消息。
     *
     * @param memoryId 聊天记忆的唯一标识符
     * @return 消息列表（不为null），可通过{@link ChatMessageDeserializer}.从JSON反序列化
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        log.debug("从内存存储中检索聊天记忆, memoryId: {}", memoryId);
        return inMemoryStore.getOrDefault(memoryId, List.of());

        // other
        // String sessionId = (String) memoryId;
        // log.debug("从数据库中检索聊天记忆, sessionId: {}", sessionId);
        // 从数据库中检索记忆后转换成ChatMessage即可
    }

    /**
     * 更新指定聊天记忆的消息
     *
     * @param memoryId 聊天记忆的唯一标识符
     * @param messages 代表{@link ChatMemory}当前状态的消息列表，
     *                 可通过{@link ChatMessageSerializer}序列化为JSON
     */
    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {

        log.debug("更新聊天记忆到内存存储, memoryId: {}, 消息数: {}", memoryId, messages.size());
        inMemoryStore.put(memoryId, messages.stream().collect(Collectors.toList()));

        // other
        // String sessionId = (String) memoryId;
        //log.debug("更新聊天记忆到数据库, sessionId: {}, 消息数: {}", sessionId, messages.size());
        // 更新策略视情况而定，批量保存
    }

    /**
     * 删除指定聊天记忆的所有消息
     *
     * @param memoryId 聊天记忆的唯一标识符
     */
    @Override
    @Transactional
    public void deleteMessages(Object memoryId) {
        log.debug("从内存存储中删除聊天记忆, memoryId: {}", memoryId);
        inMemoryStore.remove(memoryId);

        // other
        // String sessionId = (String) memoryId;
        // log.debug("从数据库中删除聊天记忆, sessionId: {}", sessionId);
    }

    /**
     * 清理所有内存数据的便利方法，用于测试
     */
    public void clearAll() {
        log.info("清除所有内存中的聊天记忆。");
        inMemoryStore.clear();
    }
}
