package com.erokin.mokaaicodemother.ai;

import com.erokin.mokaaicodemother.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private Cache<Long,AiCodeGeneratorService> aiCodeGeneratorServiceCache;

    @Resource
    private ChatHistoryService chatHistoryService;


    @Bean
    /**
     * 默认服务
     */
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return this.createAiCodeGeneratorService(0L);
    }

    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return aiCodeGeneratorServiceCache.get(appId, key -> createAiCodeGeneratorService(appId));
    }
    /**
     * 根据 appId 获取服务
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库中加载对话记录
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }



}
