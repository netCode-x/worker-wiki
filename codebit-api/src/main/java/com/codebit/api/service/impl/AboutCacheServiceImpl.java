package com.codebit.api.service.impl;

import com.codebit.api.dto.AboutResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class AboutCacheServiceImpl {


    private final RedisService redisService;

    private final ObjectMapper objectMapper;


    private static final String CACHE_KEY = "about:page";
    private static final long CACHE_TTL = 3600;

    public AboutResponse getAboutFromCache() {
        try {

            Object cached = redisService.get(CACHE_KEY);

            if (cached != null) {
                log.debug("从缓存获取关于页面信息记录");
                if (cached instanceof AboutResponse) {
                    return (AboutResponse) cached;
                }
                return objectMapper.convertValue(cached, AboutResponse.class);
            }
        } catch (Exception e) {
            log.warn("获取缓存失败: {}", e.getMessage());
        }
        return null;
    }

    public void putAboutToCache(AboutResponse response) {
        try {
            redisService.set(CACHE_KEY, response, Duration.ofSeconds(CACHE_TTL));
            log.debug("已缓存关于页面信息");
        } catch (Exception e) {
            log.warn("保存缓存失败: {}", e.getMessage());
        }
    }

    public void evictAboutCache() {
        try {
            redisService.delete(CACHE_KEY);
            log.info("已清除关于页面缓存");
        } catch (Exception e) {
            log.warn("清除缓存失败: {}", e.getMessage());
        }
    }

    public void refreshCache(AboutResponse response) {
        evictAboutCache();
        putAboutToCache(response);
    }
}