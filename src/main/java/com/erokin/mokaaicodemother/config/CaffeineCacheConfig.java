package com.erokin.mokaaicodemother.config;

import com.erokin.mokaaicodemother.ai.AiCodeGeneratorService;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
@Data
public class CaffeineCacheConfig {
    // 缓存配置:
    // 缓存大小
    private final int maximumSize = 10_000;
    // 缓存过期时间
    private final Duration expireAfterWrite = Duration.ofMinutes(30);
    // 缓存访问后过期时间
    private final Duration expireAfterAccess = Duration.ofMinutes(10);


    /**
     * 创建 AI 服务实例缓存 <id, AiCodeGeneratorService>
     * @return
     */
    @Bean
    public Cache<Long,AiCodeGeneratorService> aiCodeGeneratorServiceCache() {
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(expireAfterWrite)
                .expireAfterAccess(expireAfterAccess)
                .removalListener((key, value, cause) -> {
                    log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
                })
                .build();
    }
}
