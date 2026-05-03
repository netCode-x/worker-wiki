package com.codebit.api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;


    /**
     * 执行 Lua 脚本（单个 key）
     *
     * @param script Lua 脚本
     * @param key Redis key
     * @param args 脚本参数
     * @return 执行结果
     */
    public <T> T execute(RedisScript<T> script, String key, Object... args) {
        List<String> keys = Collections.singletonList(key);
        return redisTemplate.execute(script, keys, args);
    }

    /**
     * 执行 Lua 脚本（多个 key）
     *
     * @param script Lua 脚本
     * @param keys Redis keys
     * @param args 脚本参数
     * @return 执行结果
     */
    public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }

    /**
     *  SETNX  ----  仅当key 不存在时设置
     * @param key
     * @param value
     * @param timeout
     * @param unit
     * @return  true=设置成功，false=key 已存在
     */
    public Boolean setIfAbsent(String key, Object value,long timeout,TimeUnit unit){

        return redisTemplate.opsForValue().setIfAbsent(key,value,timeout ,unit);
    }

    /**
     * 设置值（永久有效）
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置值并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    public void set(String key, Object value, Duration timeout) {

        Assert.notNull(timeout, "Timeout must not be null");
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");

        if (TimeoutUtils.hasMillis(timeout)) {
            set(key, value, timeout.toMillis(), TimeUnit.MILLISECONDS);
        } else {
            set(key, value, timeout.getSeconds(), TimeUnit.SECONDS);
        }
    }

    /**
     * 获取值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取字符串值
     */
    public String getString(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 删除 key
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取剩余过期时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 递增（用于计数器）
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增指定步长
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
}